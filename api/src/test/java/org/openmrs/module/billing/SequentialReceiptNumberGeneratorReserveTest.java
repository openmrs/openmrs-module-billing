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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
	
	@BeforeEach
	public void before() {
		service = Context.getService(ISequentialReceiptNumberGeneratorService.class);
	}
	
	@AfterEach
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
		Assertions.assertNotNull(sequence, "Expected a persisted sequence for group '" + group + "'");
		
		return sequence.getValue();
	}
	
	@Test
	public void reserveNextSequence_shouldReturnOneAndPersistAFullBlockForANewGroup() {
		int result = service.reserveNextSequence("reserve-new-group");
		
		Assertions.assertEquals(1, result);
		Assertions.assertEquals(BLOCK_SIZE, persistedValue("reserve-new-group"));
	}
	
	@Test
	public void reserveNextSequence_shouldHandOutConsecutiveValuesFromThePool() {
		Assertions.assertEquals(1, service.reserveNextSequence("reserve-consecutive"));
		Assertions.assertEquals(2, service.reserveNextSequence("reserve-consecutive"));
		Assertions.assertEquals(3, service.reserveNextSequence("reserve-consecutive"));
		
		Assertions.assertEquals(BLOCK_SIZE, persistedValue("reserve-consecutive"));
	}
	
	@Test
	public void reserveNextSequence_shouldContinueFromThePersistedValueForAnExistingGroup() {
		service.saveSequence(createSequence("reserve-existing", 10));
		
		int result = service.reserveNextSequence("reserve-existing");
		
		Assertions.assertEquals(11, result);
		Assertions.assertEquals(10 + BLOCK_SIZE, persistedValue("reserve-existing"));
	}
	
	@Test
	public void reserveNextSequence_shouldReserveANewBlockWhenThePoolIsDrained() {
		for (int i = 1; i <= BLOCK_SIZE; i++) {
			Assertions.assertEquals(i, service.reserveNextSequence("reserve-drain"));
		}
		Assertions.assertEquals(BLOCK_SIZE, persistedValue("reserve-drain"));
		
		Assertions.assertEquals(BLOCK_SIZE + 1, service.reserveNextSequence("reserve-drain"));
		Assertions.assertEquals(2 * BLOCK_SIZE, persistedValue("reserve-drain"));
	}
	
	@Test
	public void saveSequence_shouldInvalidateThePoolForTheGroup() {
		Assertions.assertEquals(1, service.reserveNextSequence("reserve-save-invalidate"));
		
		Context.clearSession();
		GroupSequence sequence = service.getSequence("reserve-save-invalidate");
		sequence.setValue(500);
		service.saveSequence(sequence);
		
		Assertions.assertEquals(501, service.reserveNextSequence("reserve-save-invalidate"));
		Assertions.assertEquals(500 + BLOCK_SIZE, persistedValue("reserve-save-invalidate"));
	}
	
	@Test
	public void purgeSequence_shouldInvalidateThePoolForTheGroup() {
		Assertions.assertEquals(1, service.reserveNextSequence("reserve-purge-invalidate"));
		
		Context.clearSession();
		service.purgeSequence(service.getSequence("reserve-purge-invalidate"));
		
		Assertions.assertEquals(1, service.reserveNextSequence("reserve-purge-invalidate"));
		Assertions.assertEquals(BLOCK_SIZE, persistedValue("reserve-purge-invalidate"));
	}
	
	@Test
	public void reserveSequenceBlock_shouldReserveNonOverlappingBlocks() {
		Assertions.assertEquals(1, service.reserveSequenceBlock("reserve-block", BLOCK_SIZE));
		Assertions.assertEquals(BLOCK_SIZE, persistedValue("reserve-block"));
		
		Assertions.assertEquals(BLOCK_SIZE + 1, service.reserveSequenceBlock("reserve-block", BLOCK_SIZE));
		Assertions.assertEquals(2 * BLOCK_SIZE, persistedValue("reserve-block"));
		
		Assertions.assertEquals(2 * BLOCK_SIZE + 1, service.reserveSequenceBlock("reserve-block", 5));
		Assertions.assertEquals(2 * BLOCK_SIZE + 5, persistedValue("reserve-block"));
	}
	
	@Test
	public void reserveNextSequence_shouldUseTheBlockSizeFromTheGlobalProperty() {
		Context.getAdministrationService().setGlobalProperty(ModuleSettings.SEQUENCE_BLOCK_SIZE_PROPERTY, "10");
		
		Assertions.assertEquals(1, service.reserveNextSequence("reserve-gp-block-size"));
		Assertions.assertEquals(10, persistedValue("reserve-gp-block-size"));
	}
	
	@Test
	public void reserveSequenceBlock_shouldCommitIndependentlyOfAnEnclosingTransaction() {
		Integer first = newTransactionTemplate().execute(status -> {
			int result = service.reserveSequenceBlock("reserve-outer-rollback", BLOCK_SIZE);
			status.setRollbackOnly();
			return result;
		});
		
		Assertions.assertEquals((Integer) 1, first);
		Assertions.assertEquals(BLOCK_SIZE, persistedValue("reserve-outer-rollback"));
	}
	
	@Test
	public void saveSequence_shouldNotInvalidateThePoolWhenTheTransactionRollsBack() {
		Assertions.assertEquals(1, service.reserveNextSequence("reserve-rollback-save"));
		
		newTransactionTemplate().execute(status -> {
			Context.clearSession();
			GroupSequence sequence = service.getSequence("reserve-rollback-save");
			sequence.setValue(500);
			service.saveSequence(sequence);
			status.setRollbackOnly();
			return null;
		});
		
		Assertions.assertEquals(2, service.reserveNextSequence("reserve-rollback-save"));
		Assertions.assertEquals(BLOCK_SIZE, persistedValue("reserve-rollback-save"));
	}
	
	private TransactionTemplate newTransactionTemplate() {
		return new TransactionTemplate(applicationContext.getBean("transactionManager", PlatformTransactionManager.class));
	}
	
	@Test
	public void reserveSequenceBlock_shouldNotHandOutOverlappingBlocksUnderConcurrency() throws Exception {
		service.reserveSequenceBlock("reserve-lock", 1);
		
		final ConcurrentLinkedQueue<Integer> firsts = new ConcurrentLinkedQueue<>();
		final ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();
		final CountDownLatch start = new CountDownLatch(1);
		
		List<Thread> threads = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			Thread thread = new Thread(() -> {
				Context.openSession();
				try {
					start.await();
					ISequentialReceiptNumberGeneratorService threadService = Context
					        .getService(ISequentialReceiptNumberGeneratorService.class);
					for (int call = 0; call < 25; call++) {
						firsts.add(threadService.reserveSequenceBlock("reserve-lock", 10));
					}
				}
				catch (Throwable t) {
					failures.add(t);
				}
				finally {
					Context.closeSession();
				}
			});
			threads.add(thread);
			thread.start();
		}
		
		start.countDown();
		for (Thread thread : threads) {
			thread.join(180000);
		}
		
		Assertions.assertTrue(failures.isEmpty(), "Worker threads failed: " + failures);
		Assertions.assertEquals(100, firsts.size());
		Assertions.assertEquals(100, new HashSet<>(firsts).size(), "Overlapping blocks were handed out");
	}
	
	@Test
	public void reserveSequenceBlock_shouldThrowIllegalArgumentExceptionIfTheGroupIsNull() {
		assertThrows(IllegalArgumentException.class, () -> {
			service.reserveSequenceBlock(null, BLOCK_SIZE);
		});
	}
	
	@Test
	public void reserveSequenceBlock_shouldThrowIllegalArgumentExceptionIfBlockSizeIsLessThanOne() {
		assertThrows(IllegalArgumentException.class, () -> {
			service.reserveSequenceBlock("reserve-block-invalid", 0);
		});
	}
}
