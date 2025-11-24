/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.billing;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.ISequentialReceiptNumberGeneratorService;
import org.openmrs.module.billing.api.SequentialReceiptNumberGenerator;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.api.model.SequentialReceiptNumberGeneratorModel;
import org.openmrs.patient.impl.LuhnIdentifierValidator;

public class SequentialReceiptNumberGeneratorTest {
	
	private ISequentialReceiptNumberGeneratorService service;
	
	private SequentialReceiptNumberGenerator generator;
	
	private MockedStatic<Context> contextMock;
	
	@Before
	public void before() {
		contextMock = mockStatic(Context.class);
		service = mock(ISequentialReceiptNumberGeneratorService.class);
		contextMock.when(() -> Context.getService(ISequentialReceiptNumberGeneratorService.class)).thenReturn(service);
		
		generator = new SequentialReceiptNumberGenerator();
	}
	
	@After
	public void tearDown() {
		contextMock.close();
	}
	
	/**
	 * @verifies Create a new receipt number by grouping type
	 * @see SequentialReceiptNumberGenerator#generateNumber(Bill)
	 */
	@Test
	public void generateNumber_shouldCreateANewReceiptNumberByGroupingType() throws Exception {
		SequentialReceiptNumberGeneratorModel model = new SequentialReceiptNumberGeneratorModel();
		model.setGroupingType(SequentialReceiptNumberGenerator.GroupingType.NONE);
		model.setSeparator("");
		model.setSequencePadding(4);
		model.setCashierPrefix(SequentialReceiptNumberGeneratorModel.DEFAULT_CASHIER_PREFIX);
		model.setCashPointPrefix(SequentialReceiptNumberGeneratorModel.DEFAULT_CASH_POINT_PREFIX);
		model.setSequenceType(SequentialReceiptNumberGenerator.SequenceType.COUNTER);
		model.setIncludeCheckDigit(false);
		
		when(service.getOnly()).thenReturn(model);
		when(service.reserveNextSequence("")).thenReturn(1);
		generator.load();
		
		// Test no grouping
		Bill bill = createBill(1, 3);
		String number = generator.generateNumber(bill);
		Assert.assertNotNull(number);
		Assert.assertEquals("0001", number);
		
		// Test cashier grouping
		model.setGroupingType(SequentialReceiptNumberGenerator.GroupingType.CASHIER);
		generator.load();
		when(service.reserveNextSequence("P1")).thenReturn(10);
		
		number = generator.generateNumber(bill);
		Assert.assertNotNull(number);
		Assert.assertEquals("P10010", number);
		
		// Test cash point grouping
		model.setGroupingType(SequentialReceiptNumberGenerator.GroupingType.CASH_POINT);
		generator.load();
		when(service.reserveNextSequence("CP3")).thenReturn(87);
		
		number = generator.generateNumber(bill);
		Assert.assertNotNull(number);
		Assert.assertEquals("CP30087", number);
		
		// Test cashier and cash point grouping
		model.setGroupingType(SequentialReceiptNumberGenerator.GroupingType.CASHIER_AND_CASH_POINT);
		generator.load();
		when(service.reserveNextSequence("P1CP3")).thenReturn(3);
		
		number = generator.generateNumber(bill);
		Assert.assertNotNull(number);
		Assert.assertEquals("P1CP30003", number);
	}
	
	/**
	 * @verifies Create a new receipt number by sequence type
	 * @see SequentialReceiptNumberGenerator#generateNumber(Bill)
	 */
	@Test
	public void generateNumber_shouldCreateANewReceiptNumberBySequenceType() throws Exception {
		SequentialReceiptNumberGeneratorModel model = new SequentialReceiptNumberGeneratorModel();
		model.setGroupingType(SequentialReceiptNumberGenerator.GroupingType.NONE);
		model.setSeparator("");
		model.setSequencePadding(4);
		model.setCashierPrefix(SequentialReceiptNumberGeneratorModel.DEFAULT_CASHIER_PREFIX);
		model.setCashPointPrefix(SequentialReceiptNumberGeneratorModel.DEFAULT_CASH_POINT_PREFIX);
		model.setSequenceType(SequentialReceiptNumberGenerator.SequenceType.COUNTER);
		model.setIncludeCheckDigit(false);
		
		when(service.getOnly()).thenReturn(model);
		when(service.reserveNextSequence("")).thenReturn(1);
		generator.load();
		
		// Test no grouping
		Bill bill = createBill(1, 3);
		String number = generator.generateNumber(bill);
		Assert.assertNotNull(number);
		Assert.assertEquals("0001", number);
		
		model.setSequenceType(SequentialReceiptNumberGenerator.SequenceType.DATE_COUNTER);
		generator.load();
		when(service.reserveNextSequence("")).thenReturn(52013);
		
		number = generator.generateNumber(bill);
		Assert.assertNotNull(number);
		// Should end with the sequence number
		Assert.assertTrue(number.endsWith("52013"));
		// Should be longer than just the sequence due to date prefix (yyMMdd = 6 chars + 5 digits)
		Assert.assertEquals(11, number.length());
		
		// Test DATE_TIME_COUNTER sequence type
		model.setSequenceType(SequentialReceiptNumberGenerator.SequenceType.DATE_TIME_COUNTER);
		generator.load();
		when(service.reserveNextSequence("")).thenReturn(15);
		
		number = generator.generateNumber(bill);
		Assert.assertNotNull(number);
		// Should end with the sequence number
		Assert.assertTrue(number.endsWith("0015"));
		// Should be longer than just the sequence due to date-time prefix (yyMMddHHmmss = 12 chars + 4 digits)
		Assert.assertEquals(16, number.length());
	}
	
	/**
	 * @verifies Create a new receipt number using the specified separator
	 * @see SequentialReceiptNumberGenerator#generateNumber(Bill)
	 */
	@Test
	public void generateNumber_shouldCreateANewReceiptNumberUsingTheSpecifiedSeparator() throws Exception {
		SequentialReceiptNumberGeneratorModel model = new SequentialReceiptNumberGeneratorModel();
		model.setGroupingType(SequentialReceiptNumberGenerator.GroupingType.NONE);
		model.setSeparator("-");
		model.setSequencePadding(4);
		model.setCashierPrefix(SequentialReceiptNumberGeneratorModel.DEFAULT_CASHIER_PREFIX);
		model.setCashPointPrefix(SequentialReceiptNumberGeneratorModel.DEFAULT_CASH_POINT_PREFIX);
		model.setSequenceType(SequentialReceiptNumberGenerator.SequenceType.COUNTER);
		model.setIncludeCheckDigit(false);
		
		when(service.getOnly()).thenReturn(model);
		when(service.reserveNextSequence("")).thenReturn(1);
		generator.load();
		
		Bill bill = createBill(1, 3);
		String number = generator.generateNumber(bill);
		Assert.assertNotNull(number);
		Assert.assertEquals("0001", number);
		
		// Test separator with DATE_TIME_COUNTER
		model.setGroupingType(SequentialReceiptNumberGenerator.GroupingType.CASHIER_AND_CASH_POINT);
		model.setSequenceType(SequentialReceiptNumberGenerator.SequenceType.DATE_TIME_COUNTER);
		generator.load();
		when(service.reserveNextSequence("P1CP3")).thenReturn(52013);
		
		number = generator.generateNumber(bill);
		Assert.assertNotNull(number);
		// Should start with grouping and separator
		Assert.assertTrue(number.startsWith("P1-CP3-"));
		// Should end with the sequence number
		Assert.assertTrue(number.endsWith("52013"));
		
		model.setIncludeCheckDigit(true);
		generator.load();
		
		number = generator.generateNumber(bill);
		Assert.assertNotNull(number);
		// Should start with grouping and separator
		Assert.assertTrue(number.startsWith("P1-CP3-"));
		// Should contain the sequence number before the check digit
		Assert.assertTrue(number.contains("52013"));
		// Should end with a check digit (single digit after final separator)
		String[] parts = number.split("-");
		Assert.assertEquals(1, parts[parts.length - 1].length());
	}
	
	/**
	 * @verifies Generate and set the correct check digit
	 * @see SequentialReceiptNumberGenerator#generateNumber(Bill)
	 */
	@Test
	public void generateNumber_shouldGenerateAndSetTheCorrectCheckDigit() throws Exception {
		SequentialReceiptNumberGeneratorModel model = new SequentialReceiptNumberGeneratorModel();
		model.setGroupingType(SequentialReceiptNumberGenerator.GroupingType.NONE);
		model.setSeparator("");
		model.setCashierPrefix(SequentialReceiptNumberGeneratorModel.DEFAULT_CASHIER_PREFIX);
		model.setCashPointPrefix(SequentialReceiptNumberGeneratorModel.DEFAULT_CASH_POINT_PREFIX);
		model.setSequencePadding(4);
		model.setSequenceType(SequentialReceiptNumberGenerator.SequenceType.COUNTER);
		model.setIncludeCheckDigit(true);
		
		when(service.getOnly()).thenReturn(model);
		when(service.reserveNextSequence("")).thenReturn(1);
		generator.load();
		
		Bill bill = createBill(1, 3);
		
		String number = generator.generateNumber(bill);
		Assert.assertNotNull(number);
		Assert.assertEquals("00018", number);
		
		model.setSeparator("-");
		generator.load();
		
		number = generator.generateNumber(bill);
		Assert.assertNotNull(number);
		Assert.assertEquals("0001-8", number);
		
		model.setGroupingType(SequentialReceiptNumberGenerator.GroupingType.CASHIER_AND_CASH_POINT);
		generator.load();
		when(service.reserveNextSequence("P1CP3")).thenReturn(25);
		
		number = generator.generateNumber(bill);
		Assert.assertNotNull(number);
		
		LuhnIdentifierValidator validator = new LuhnIdentifierValidator();
		String idWithCheckDigit = validator.getValidIdentifier("P1CP30025");
		Assert.assertEquals("P1-CP3-0025-" + idWithCheckDigit.substring(idWithCheckDigit.length() - 1), number);
	}
	
	/**
	 * @verifies Update sequence table with the new sequence by group
	 * @see SequentialReceiptNumberGenerator#generateNumber(Bill)
	 */
	@Test
	public void generateNumber_shouldUpdateSequenceTableWithTheNewSequenceByGroup() throws Exception {
		SequentialReceiptNumberGeneratorModel model = new SequentialReceiptNumberGeneratorModel();
		model.setGroupingType(SequentialReceiptNumberGenerator.GroupingType.NONE);
		model.setSeparator("");
		model.setCashierPrefix(SequentialReceiptNumberGeneratorModel.DEFAULT_CASHIER_PREFIX);
		model.setCashPointPrefix(SequentialReceiptNumberGeneratorModel.DEFAULT_CASH_POINT_PREFIX);
		model.setSequencePadding(4);
		model.setSequenceType(SequentialReceiptNumberGenerator.SequenceType.COUNTER);
		model.setIncludeCheckDigit(true);
		
		when(service.getOnly()).thenReturn(model);
		when(service.reserveNextSequence("")).thenReturn(1);
		generator.load();
		
		Bill bill = createBill(1, 3);
		
		generator.generateNumber(bill);
		verify(service, times(1)).reserveNextSequence("");
		
		model.setSequenceType(SequentialReceiptNumberGenerator.SequenceType.DATE_TIME_COUNTER);
		generator.load();
		generator.generateNumber(bill);
		verify(service, times(2)).reserveNextSequence("");
		
		model.setGroupingType(SequentialReceiptNumberGenerator.GroupingType.CASHIER);
		generator.load();
		when(service.reserveNextSequence("P1")).thenReturn(1);
		generator.generateNumber(bill);
		verify(service, times(1)).reserveNextSequence("P1");
		generator.generateNumber(bill);
		verify(service, times(2)).reserveNextSequence("P1");
		
		model.setGroupingType(SequentialReceiptNumberGenerator.GroupingType.CASH_POINT);
		generator.load();
		when(service.reserveNextSequence("CP3")).thenReturn(1);
		generator.generateNumber(bill);
		verify(service, times(1)).reserveNextSequence("CP3");
		generator.generateNumber(bill);
		verify(service, times(2)).reserveNextSequence("CP3");
		
		model.setGroupingType(SequentialReceiptNumberGenerator.GroupingType.CASHIER_AND_CASH_POINT);
		generator.load();
		when(service.reserveNextSequence("P1CP3")).thenReturn(1);
		generator.generateNumber(bill);
		verify(service, times(1)).reserveNextSequence("P1CP3");
		generator.generateNumber(bill);
		verify(service, times(2)).reserveNextSequence("P1CP3");
		
		model.setSeparator("-");
		generator.load();
		generator.generateNumber(bill);
		verify(service, times(3)).reserveNextSequence("P1CP3");
	}
	
	/**
	 * @verifies Throw NullPointerException if bill is null.
	 * @see SequentialReceiptNumberGenerator#generateNumber(Bill)
	 */
	@Test(expected = NullPointerException.class)
	public void generateNumber_shouldThrowNullPointerExceptionIfBillIsNull() throws Exception {
		generator.generateNumber(null);
	}
	
	protected Bill createBill(int cashierId, int cashPointId) {
		Provider provider = new Provider(cashierId);
		CashPoint cashPoint = new CashPoint();
		cashPoint.setId(cashPointId);
		Bill bill = new Bill();
		bill.setCashier(provider);
		bill.setCashPoint(cashPoint);
		
		return bill;
	}
}
