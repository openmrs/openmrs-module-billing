/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.ISequentialReceiptNumberGeneratorService;
import org.openmrs.module.billing.api.impl.SequentialReceiptNumberGeneratorServiceImpl;
import org.openmrs.module.billing.api.model.GroupSequence;
import org.openmrs.module.billing.base.BaseModuleContextTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Tests for sequence reservation. These run without a test-managed transaction because
 * {@link ISequentialReceiptNumberGeneratorService#reserveSequenceBlock(String, int)} commits in a
 * REQUIRES_NEW transaction on a separate connection, which would block on this class's uncommitted
 * data if the tests were transactional. Every service call commits immediately, so fixtures are
 * created through the service and all rows are purged after each test.
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SequentialReceiptNumberGeneratorReserveTest extends BaseModuleContextTest {
	
	private static final int BLOCK_SIZE = SequentialReceiptNumberGeneratorServiceImpl.DEFAULT_SEQUENCE_BLOCK_SIZE;
	
	private ISequentialReceiptNumberGeneratorService service;
	
	@Before
	public void before() {
		service = Context.getService(ISequentialReceiptNumberGeneratorService.class);
	}
	
	@After
	public void purgeAllSequences() {
		Context.clearSession();
		for (GroupSequence sequence : service.getSequences()) {
			service.purgeSequence(sequence);
		}
		Context.getAdministrationService().setGlobalProperty(ModuleSettings.SEQUENCE_BLOCK_SIZE_PROPERTY, "");
	}
	
	private GroupSequence createSequence(String group, int value) {
		GroupSequence sequence = new GroupSequence();
		sequence.setGroup(group);
		sequence.setValue(value);
		
		return sequence;
	}
	
	private int persistedValue(String group) {
		Context.clearSession();
		GroupSequence sequence = service.getSequence(group);
		Assert.assertNotNull("Expected a persisted sequence for group '" + group + "'", sequence);
		
		return sequence.getValue();
	}
	
	@Test
	public void reserveNextSequence_shouldReturnOneAndPersistAFullBlockForANewGroup() {
		int result = service.reserveNextSequence("reserve-new-group");
		
		Assert.assertEquals(1, result);
		Assert.assertEquals(BLOCK_SIZE, persistedValue("reserve-new-group"));
	}
	
	@Test
	public void reserveNextSequence_shouldHandOutConsecutiveValuesFromThePool() {
		Assert.assertEquals(1, service.reserveNextSequence("reserve-consecutive"));
		Assert.assertEquals(2, service.reserveNextSequence("reserve-consecutive"));
		Assert.assertEquals(3, service.reserveNextSequence("reserve-consecutive"));
		
		Assert.assertEquals(BLOCK_SIZE, persistedValue("reserve-consecutive"));
	}
	
	@Test
	public void reserveNextSequence_shouldContinueFromThePersistedValueForAnExistingGroup() {
		service.saveSequence(createSequence("reserve-existing", 10));
		
		int result = service.reserveNextSequence("reserve-existing");
		
		Assert.assertEquals(11, result);
		Assert.assertEquals(10 + BLOCK_SIZE, persistedValue("reserve-existing"));
	}
	
	@Test
	public void reserveNextSequence_shouldReserveANewBlockWhenThePoolIsDrained() {
		for (int i = 1; i <= BLOCK_SIZE; i++) {
			Assert.assertEquals(i, service.reserveNextSequence("reserve-drain"));
		}
		Assert.assertEquals(BLOCK_SIZE, persistedValue("reserve-drain"));
		
		Assert.assertEquals(BLOCK_SIZE + 1, service.reserveNextSequence("reserve-drain"));
		Assert.assertEquals(2 * BLOCK_SIZE, persistedValue("reserve-drain"));
	}
	
	@Test
	public void saveSequence_shouldInvalidateThePoolForTheGroup() {
		Assert.assertEquals(1, service.reserveNextSequence("reserve-save-invalidate"));
		
		Context.clearSession();
		GroupSequence sequence = service.getSequence("reserve-save-invalidate");
		sequence.setValue(500);
		service.saveSequence(sequence);
		
		Assert.assertEquals(501, service.reserveNextSequence("reserve-save-invalidate"));
		Assert.assertEquals(500 + BLOCK_SIZE, persistedValue("reserve-save-invalidate"));
	}
	
	@Test
	public void purgeSequence_shouldInvalidateThePoolForTheGroup() {
		Assert.assertEquals(1, service.reserveNextSequence("reserve-purge-invalidate"));
		
		Context.clearSession();
		service.purgeSequence(service.getSequence("reserve-purge-invalidate"));
		
		Assert.assertEquals(1, service.reserveNextSequence("reserve-purge-invalidate"));
		Assert.assertEquals(BLOCK_SIZE, persistedValue("reserve-purge-invalidate"));
	}
	
	@Test
	public void reserveSequenceBlock_shouldReserveNonOverlappingBlocks() {
		Assert.assertEquals(1, service.reserveSequenceBlock("reserve-block", BLOCK_SIZE));
		Assert.assertEquals(BLOCK_SIZE, persistedValue("reserve-block"));
		
		Assert.assertEquals(BLOCK_SIZE + 1, service.reserveSequenceBlock("reserve-block", BLOCK_SIZE));
		Assert.assertEquals(2 * BLOCK_SIZE, persistedValue("reserve-block"));
		
		Assert.assertEquals(2 * BLOCK_SIZE + 1, service.reserveSequenceBlock("reserve-block", 5));
		Assert.assertEquals(2 * BLOCK_SIZE + 5, persistedValue("reserve-block"));
	}
	
	@Test
	public void reserveNextSequence_shouldUseTheBlockSizeFromTheGlobalProperty() {
		Context.getAdministrationService().setGlobalProperty(ModuleSettings.SEQUENCE_BLOCK_SIZE_PROPERTY, "10");
		
		Assert.assertEquals(1, service.reserveNextSequence("reserve-gp-block-size"));
		Assert.assertEquals(10, persistedValue("reserve-gp-block-size"));
	}
	
	@Test
	public void reserveSequenceBlock_shouldCommitIndependentlyOfAnEnclosingTransaction() {
		Integer first = newTransactionTemplate().execute(status -> {
			int result = service.reserveSequenceBlock("reserve-outer-rollback", BLOCK_SIZE);
			status.setRollbackOnly();
			return result;
		});
		
		Assert.assertEquals((Integer) 1, first);
		Assert.assertEquals(BLOCK_SIZE, persistedValue("reserve-outer-rollback"));
	}
	
	@Test
	public void saveSequence_shouldNotInvalidateThePoolWhenTheTransactionRollsBack() {
		Assert.assertEquals(1, service.reserveNextSequence("reserve-rollback-save"));
		
		newTransactionTemplate().execute(status -> {
			Context.clearSession();
			GroupSequence sequence = service.getSequence("reserve-rollback-save");
			sequence.setValue(500);
			service.saveSequence(sequence);
			status.setRollbackOnly();
			return null;
		});
		
		Assert.assertEquals(2, service.reserveNextSequence("reserve-rollback-save"));
		Assert.assertEquals(BLOCK_SIZE, persistedValue("reserve-rollback-save"));
	}
	
	private TransactionTemplate newTransactionTemplate() {
		return new TransactionTemplate(applicationContext.getBean("transactionManager", PlatformTransactionManager.class));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void reserveSequenceBlock_shouldThrowIllegalArgumentExceptionIfTheGroupIsNull() {
		service.reserveSequenceBlock(null, BLOCK_SIZE);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void reserveSequenceBlock_shouldThrowIllegalArgumentExceptionIfBlockSizeIsLessThanOne() {
		service.reserveSequenceBlock("reserve-block-invalid", 0);
	}
}
