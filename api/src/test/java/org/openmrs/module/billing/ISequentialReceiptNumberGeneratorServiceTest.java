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

import java.util.List;
import java.util.Properties;

import org.hibernate.cfg.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.ISequentialReceiptNumberGeneratorService;
import org.openmrs.module.billing.api.SequentialReceiptNumberGenerator;
import org.openmrs.module.billing.api.model.GroupSequence;
import org.openmrs.module.billing.api.model.SequentialReceiptNumberGeneratorModel;
import org.openmrs.module.billing.base.entity.IObjectDataServiceTest;

public class ISequentialReceiptNumberGeneratorServiceTest extends IObjectDataServiceTest<ISequentialReceiptNumberGeneratorService, SequentialReceiptNumberGeneratorModel> {
	
	public static final String SEQUENTIAL_RECEIPT_NUMBER_GENERATOR_DATASET = TestConstants.BASE_DATASET_DIR
	        + "SequentialReceiptNumberGenerator.xml";
	
	@Override
	public Properties getRuntimeProperties() {
		Properties properties = super.getRuntimeProperties();
		
		// This is needed for proper locking in the in-memory database
		properties.setProperty(Environment.URL, "jdbc:h2:mem:openmrs;DB_CLOSE_DELAY=30");
		
		return properties;
	}
	
	@BeforeEach
	public void before() throws Exception {
		super.before();
		
		executeDataSet(SEQUENTIAL_RECEIPT_NUMBER_GENERATOR_DATASET);
	}
	
	@Override
	public SequentialReceiptNumberGeneratorModel createEntity(boolean valid) {
		SequentialReceiptNumberGeneratorModel model = new SequentialReceiptNumberGeneratorModel();
		model.setGroupingType(SequentialReceiptNumberGenerator.GroupingType.NONE);
		model.setSequenceType(SequentialReceiptNumberGenerator.SequenceType.COUNTER);
		model.setSeparator("-");
		model.setSequencePadding(4);
		model.setIncludeCheckDigit(true);
		
		if (valid) {
			model.setCashierPrefix(SequentialReceiptNumberGeneratorModel.DEFAULT_CASHIER_PREFIX);
			model.setCashPointPrefix(SequentialReceiptNumberGeneratorModel.DEFAULT_CASH_POINT_PREFIX);
		} else {
			model.setCashierPrefix(null);
			model.setCashPointPrefix(null);
		}
		
		return model;
	}
	
	@Override
	protected int getTestEntityCount() {
		return 1;
	}
	
	@Override
	protected void updateEntityFields(SequentialReceiptNumberGeneratorModel entity) {
		entity.setCashierPrefix("UP");
		entity.setCashPointPrefix("UCP");
		entity.setGroupingType(SequentialReceiptNumberGenerator.GroupingType.CASH_POINT);
		entity.setSequenceType(SequentialReceiptNumberGenerator.SequenceType.DATE_TIME_COUNTER);
		entity.setSeparator("_");
		entity.setSequencePadding(8);
		entity.setIncludeCheckDigit(!entity.getIncludeCheckDigit());
	}
	
	@Override
	protected void assertEntity(SequentialReceiptNumberGeneratorModel expected,
	        SequentialReceiptNumberGeneratorModel actual) {
		Assertions.assertEquals(expected.getCashierPrefix(), actual.getCashierPrefix());
		Assertions.assertEquals(expected.getCashPointPrefix(), actual.getCashPointPrefix());
		Assertions.assertEquals(expected.getGroupingType(), actual.getGroupingType());
		Assertions.assertEquals(expected.getSeparator(), actual.getSeparator());
		Assertions.assertEquals(expected.getSequencePadding(), actual.getSequencePadding());
		Assertions.assertEquals(expected.getSequenceType(), actual.getSequenceType());
		Assertions.assertEquals(expected.getIncludeCheckDigit(), actual.getIncludeCheckDigit());
	}
	
	/**
	 * @verifies Throw IllegalArgumentException if the group is null
	 * @see ISequentialReceiptNumberGeneratorService#reserveNextSequence(String)
	 */
	@Test
	public void reserveNextSequence_shouldThrowIllegalArgumentExceptionIfTheGroupIsNull() {
		assertThrows(IllegalArgumentException.class, () -> {
			service.reserveNextSequence(null);
		});
	}
	
	/**
	 * @verifies return all sequences
	 * @see ISequentialReceiptNumberGeneratorService#getSequences()
	 */
	@Test
	public void getSequences_shouldReturnAllSequences() {
		List<GroupSequence> sequences = service.getSequences();
		
		Assertions.assertNotNull(sequences);
		Assertions.assertEquals(4, sequences.size());
	}
	
	/**
	 * @verifies return an empty list if no sequences have been defined
	 * @see ISequentialReceiptNumberGeneratorService#getSequences()
	 */
	@Test
	public void getSequences_shouldReturnAnEmptyListIfNoSequencesHaveBeenDefined() {
		List<GroupSequence> sequences = service.getSequences();
		for (GroupSequence sequence : sequences) {
			service.purgeSequence(sequence);
		}
		
		Context.flushSession();
		
		sequences = service.getSequences();
		Assertions.assertNotNull(sequences);
		Assertions.assertEquals(0, sequences.size());
	}
	
	/**
	 * @verifies Throw a NullPointerException if sequence is null
	 * @see ISequentialReceiptNumberGeneratorService#saveSequence(GroupSequence)
	 */
	@Test
	public void saveSequence_shouldThrowANullPointerExceptionIfSequenceIsNull() {
		assertThrows(NullPointerException.class, () -> {
			service.saveSequence(null);
		});
	}
	
	/**
	 * @verifies return the saved sequence
	 * @see ISequentialReceiptNumberGeneratorService#saveSequence(GroupSequence)
	 */
	@Test
	public void saveSequence_shouldReturnTheSavedSequence() {
		GroupSequence sequence = new GroupSequence();
		sequence.setGroup("New Group");
		sequence.setValue(50);
		
		sequence = service.saveSequence(sequence);
		
		Assertions.assertNotNull(sequence);
		Assertions.assertNotNull(sequence.getId());
		Assertions.assertEquals("New Group", sequence.getGroup());
		Assertions.assertEquals(50, sequence.getValue());
	}
	
	/**
	 * @verifies update the sequence successfully
	 * @see ISequentialReceiptNumberGeneratorService#saveSequence(GroupSequence)
	 */
	@Test
	public void saveSequence_shouldUpdateTheSequenceSuccessfully() {
		GroupSequence sequence = service.getSequence("Test Seq 1");
		int oldValue = sequence.getValue();
		sequence.setValue(oldValue + 10);
		
		service.saveSequence(sequence);
		
		Context.flushSession();
		
		sequence = service.getSequence(sequence.getGroup());
		Assertions.assertNotNull(sequence);
		Assertions.assertEquals(oldValue + 10, sequence.getValue());
	}
	
	/**
	 * @verifies create the sequence successfully
	 * @see ISequentialReceiptNumberGeneratorService#saveSequence(GroupSequence)
	 */
	@Test
	public void saveSequence_shouldCreateTheSequenceSuccessfully() {
		GroupSequence sequence = new GroupSequence();
		sequence.setGroup("New Group");
		sequence.setValue(50);
		
		Assertions.assertNull(sequence.getId());
		
		sequence = service.saveSequence(sequence);
		
		Assertions.assertNotNull(sequence);
		Assertions.assertNotNull(sequence.getId());
	}
	
	/**
	 * @verifies Throw a NullPointerException if the sequence is null
	 * @see ISequentialReceiptNumberGeneratorService#purgeSequence(GroupSequence)
	 */
	@Test
	public void purgeSequence_shouldThrowANullPointerExceptionIfTheSequenceIsNull() {
		assertThrows(NullPointerException.class, () -> {
			service.purgeSequence(null);
		});
	}
	
	/**
	 * @verifies delete the sequence from the database
	 * @see ISequentialReceiptNumberGeneratorService#purgeSequence(GroupSequence)
	 */
	@Test
	public void purgeSequence_shouldDeleteTheSequenceFromTheDatabase() {
		GroupSequence sequence = service.getSequence("Test Seq 1");
		service.purgeSequence(sequence);
		
		Context.flushSession();
		
		sequence = service.getSequence("Test Seq 1");
		Assertions.assertNull(sequence);
	}
	
	/**
	 * @verifies not throw an exception if the sequence is not in the database
	 * @see ISequentialReceiptNumberGeneratorService#purgeSequence(GroupSequence)
	 */
	@Test
	public void purgeSequence_shouldNotThrowAnExceptionIfTheSequenceIsNotInTheDatabase() {
		GroupSequence sequence = service.getSequence("Test Seq 1");
		service.purgeSequence(sequence);
		
		Context.flushSession();
		
		service.purgeSequence(sequence);
	}
	
	/**
	 * @verifies Throw an IllegalArgumentException if group is null
	 * @see ISequentialReceiptNumberGeneratorService#getSequence(String)
	 */
	@Test
	public void getSequence_shouldThrowAnIllegalArgumentExceptionIfGroupIsNull() {
		assertThrows(IllegalArgumentException.class, () -> {
			service.getSequence(null);
		});
	}
	
	/**
	 * @verifies return the sequence if group is empty
	 * @see ISequentialReceiptNumberGeneratorService#getSequence(String)
	 */
	@Test
	public void getSequence_shouldReturnTheSequenceIfGroupIsEmpty() {
		GroupSequence sequence = service.getSequence("");
		Assertions.assertNotNull(sequence);
		Assertions.assertEquals("", sequence.getGroup());
		Assertions.assertEquals(18, sequence.getValue());
	}
	
	/**
	 * @verifies return the specified sequence
	 * @see ISequentialReceiptNumberGeneratorService#getSequence(String)
	 */
	@Test
	public void getSequence_shouldReturnTheSpecifiedSequence() {
		GroupSequence sequence = service.getSequence("Test Seq 1");
		
		Assertions.assertNotNull(sequence);
		Assertions.assertEquals("Test Seq 1", sequence.getGroup());
		Assertions.assertEquals(10, sequence.getValue());
	}
	
	/**
	 * @verifies return null if the sequence cannot be found
	 * @see ISequentialReceiptNumberGeneratorService#getSequence(String)
	 */
	@Test
	public void getSequence_shouldReturnNullIfTheSequenceCannotBeFound() {
		GroupSequence sequence = service.getSequence("Not A Valid Sequence");
		
		Assertions.assertNull(sequence);
	}
	
	/**
	 * @verifies return the first model.
	 * @see ISequentialReceiptNumberGeneratorService#getOnly()
	 */
	@Test
	public void getOnly_shouldReturnTheFirstModel() {
		SequentialReceiptNumberGeneratorModel model = service.getOnly();
		
		Assertions.assertNotNull(model);
		Assertions.assertEquals((Integer) 0, model.getId());
	}
	
	/**
	 * @verifies return a new model if none has been defined.
	 * @see ISequentialReceiptNumberGeneratorService#getOnly()
	 */
	@Test
	public void getOnly_shouldReturnANewModelIfNoneHasBeenDefined() {
		SequentialReceiptNumberGeneratorModel model = service.getOnly();
		service.purge(model);
		
		model = service.getOnly();
		Assertions.assertNotNull(model);
		Assertions.assertNull(model.getId());
	}
}
