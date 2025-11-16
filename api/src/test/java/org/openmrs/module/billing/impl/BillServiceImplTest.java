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
package org.openmrs.module.billing.impl;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.module.billing.api.IBillService;
import org.openmrs.module.billing.api.IBillableItemsService;
import org.openmrs.module.billing.api.ICashPointService;

import org.openmrs.module.billing.api.IPaymentModeService;

import org.openmrs.module.billing.api.IReceiptNumberGenerator;
import org.openmrs.module.billing.api.ReceiptNumberGeneratorFactory;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.api.model.Payment;
import org.openmrs.module.billing.api.search.BillSearch;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.stockmanagement.api.model.StockItem;
import org.apache.commons.lang3.RandomStringUtils;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import org.openmrs.module.billing.base.TestConstants;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.mockito.ArgumentMatchers.any;

/**
 * Concrete implementation of IBillServiceTest for testing BillServiceImpl
 */
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@PowerMockIgnore({ "javax.management.*", "javax.net.ssl.*" })
@PrepareForTest({ ReceiptNumberGeneratorFactory.class })
public class BillServiceImplTest extends BaseModuleContextSensitiveTest {
	
	private IReceiptNumberGenerator receiptNumberGenerator;
	
	private IBillService billService;
	
	private ProviderService providerService;
	
	private PatientService patientService;
	
	private IBillableItemsService billableItemsService;
	
	private ICashPointService cashPointService;

	private IPaymentModeService paymentModeService;
	

	@Before
	public void before() throws Exception {
		billService = Context.getService(IBillService.class);

		providerService = Context.getProviderService();
		patientService = Context.getPatientService();
		billableItemsService = Context.getService(IBillableItemsService.class);
		cashPointService = Context.getService(ICashPointService.class);
		paymentModeService = Context.getService(IPaymentModeService.class);
		

		mockStatic(ReceiptNumberGeneratorFactory.class);
		receiptNumberGenerator = mock(IReceiptNumberGenerator.class);
		when(ReceiptNumberGeneratorFactory.getGenerator()).thenReturn(receiptNumberGenerator);
		// Default behavior for tests that expect a generated number with standard prefix
		final int[] genCounter = { 1 };
		when(receiptNumberGenerator.generateNumber(any(Bill.class))).thenAnswer(invocation -> "TEST-RECEIPT-" + (genCounter[0]++));

		executeDataSet(TestConstants.BASE_DATASET_DIR + "CoreTest-2.0.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "StockOperationType.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "PaymentModeTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillTest.xml");
		
		// Set up a test receipt number generator
		ReceiptNumberGeneratorFactory.setGenerator(new IReceiptNumberGenerator() {
			private int counter = 1;
			
			@Override
			public String getName() {
				return "Test Generator";
			}
			
			@Override
			public String getDescription() {
				return "Test receipt number generator";
			}
			
			@Override
			public void load() {
				// No-op
			}
			
			@Override
			public String generateNumber(Bill bill) {
				return "TEST-RECEIPT-" + (counter++);
			}
			
			@Override
			public String getConfigurationPage() {
				return null;
			}
			
			@Override
			public boolean isLoaded() {
				return true;
			}
		});
	}
	
	// region Argument / validation tests
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#save(Bill)
	 */
	@Test
	public void save_shouldThrowNullPointerExceptionIfBillIsNull() {
		assertThrows(NullPointerException.class, () -> billService.save(null));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillByReceiptNumber(String)
	 */
	@Test
	public void getBillByReceiptNumber_shouldThrowIllegalArgumentExceptionIfReceiptNumberIsNull() {
		assertThrows(IllegalArgumentException.class, () -> billService.getBillByReceiptNumber(null));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillByReceiptNumber(String)
	 */
	@Test
	public void getBillByReceiptNumber_shouldThrowIllegalArgumentExceptionIfReceiptNumberIsEmpty() {
		assertThrows(IllegalArgumentException.class, () -> billService.getBillByReceiptNumber(""));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillByReceiptNumber(String)
	 */
	@Test
	public void getBillByReceiptNumber_shouldThrowIllegalArgumentExceptionIfReceiptNumberIsTooLong() {
		String longReceiptNumber = RandomStringUtils.randomAlphanumeric(1999);
		assertThrows(IllegalArgumentException.class, () -> billService.getBillByReceiptNumber(longReceiptNumber));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillsByPatient(Patient,
	 *      org.openmrs.module.billing.api.base.PagingInfo)
	 */
	@Test
	public void getBillsByPatient_shouldThrowNullPointerExceptionIfPatientIsNull() {
		assertThrows(NullPointerException.class, () -> billService.getBillsByPatient(null, null));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillsByPatientId(int,
	 *      org.openmrs.module.billing.api.base.PagingInfo)
	 */
	@Test
	public void getBillsByPatientId_shouldThrowIllegalArgumentExceptionIfPatientIdIsNegative() {
		assertThrows(IllegalArgumentException.class, () -> billService.getBillsByPatientId(-1, null));
	}
	
	// region Basic retrieval and listing tests
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getAll()
	 */
	@Test
	public void getAll_shouldReturnAllBills() {
		List<Bill> bills = billService.getAll();
		assertNotNull(bills);
		for (Bill bill : bills) {
			if (bill.getLineItems() != null) {
				for (Object item : bill.getLineItems()) {
					assertNotNull("Line items should not contain null values", item);
				}
			}
		}
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillByReceiptNumber(String)
	 */
	@Test
	public void getBillByReceiptNumber_shouldReturnBillWithSpecifiedReceiptNumber() {
		Bill bill = billService.getBillByReceiptNumber("test 1 receipt number");
		assertNotNull(bill);
		assertEquals("test 1 receipt number", bill.getReceiptNumber());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillByReceiptNumber(String)
	 */
	@Test
	public void getBillByReceiptNumber_shouldReturnNullIfReceiptNumberNotFound() {
		Bill bill = billService.getBillByReceiptNumber("nonexistent receipt number");
		assertNull(bill);
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillsByPatientId(int,
	 *      org.openmrs.module.billing.api.base.PagingInfo)
	 */
	@Test
	public void getBillsByPatientId_shouldReturnBillsForPatient() {
		List<Bill> bills = billService.getBillsByPatientId(0, null);
		assertNotNull(bills);
		assertFalse(bills.isEmpty());
		assertEquals(1, bills.size());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillsByPatientId(int,
	 *      org.openmrs.module.billing.api.base.PagingInfo)
	 */
	@Test
	public void getBillsByPatientId_shouldReturnEmptyListWhenPatientHasNoBills() {
		List<Bill> bills = billService.getBillsByPatientId(999, null);
		assertNotNull(bills);
		assertEquals(0, bills.size());
	}
	
	// region save(Bill): creation, merging, and receipt number generation
	
	/**
	 * @verifies Generate a new receipt number if one has not been defined.
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#save(Bill)
	 */
	@Test
	public void save_shouldGenerateANewReceiptNumberIfOneHasNotBeenDefined() throws Exception {
		Bill bill = new Bill();
		bill.setPatient(patientService.getPatient(1));
		bill.setCashier(providerService.getProvider(0));
		bill.setCashPoint(cashPointService.getById(0));
		bill.setStatus(BillStatus.PENDING);
		bill.setReceiptNumber(null);
		// add one line item to make it valid
		Bill template = billService.getById(0);
		StockItem stockItem = template.getLineItems().get(0).getItem();
		bill.addLineItem(stockItem, BigDecimal.valueOf(10), "Test", 1).setPaymentStatus(BillStatus.PENDING);
		
		String receiptNumber = "Test Number";
		when(receiptNumberGenerator.generateNumber(bill)).thenReturn(receiptNumber);
		
		Bill saved = billService.save(bill);
		Context.flushSession();
		
		Assert.assertNotNull(saved.getId());
		Assert.assertEquals(receiptNumber, saved.getReceiptNumber());
		
		verify(receiptNumberGenerator, times(1)).generateNumber(bill);
	}
	
	/**
	 * @verifies Not generate a receipt number if one has already been defined.
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#save(Bill)
	 */
	@Test
	public void save_shouldNotGenerateAReceiptNumberIfOneHasAlreadyBeenDefined() throws Exception {
		String receiptNumber = "Test Number";
		Bill bill = new Bill();
		bill.setPatient(patientService.getPatient(1));
		bill.setCashier(providerService.getProvider(0));
		bill.setCashPoint(cashPointService.getById(0));
		bill.setStatus(BillStatus.PENDING);
		bill.setReceiptNumber(receiptNumber);
		Bill template = billService.getById(0);
		StockItem stockItem = template.getLineItems().get(0).getItem();
		bill.addLineItem(stockItem, BigDecimal.valueOf(10), "Test", 1).setPaymentStatus(BillStatus.PENDING);
		
		Bill saved = billService.save(bill);
		Context.flushSession();
		
		Assert.assertNotNull(saved.getId());
		Assert.assertEquals(receiptNumber, saved.getReceiptNumber());
		
		verify(receiptNumberGenerator, times(0)).generateNumber(bill);
	}
	
	/**
	 * @verifies Throw APIException if receipt number cannot be generated.
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#save(Bill)
	 */
	@Test(expected = APIException.class)
	public void save_shouldThrowAPIExceptionIfReceiptNumberCannotBeGenerated() throws Exception {
		Bill bill = new Bill();
		bill.setPatient(patientService.getPatient(1));
		bill.setCashier(providerService.getProvider(0));
		bill.setCashPoint(cashPointService.getById(0));
		bill.setStatus(BillStatus.PENDING);
		bill.setReceiptNumber(null);
		Bill template = billService.getById(0);
		StockItem stockItem = template.getLineItems().get(0).getItem();
		bill.addLineItem(stockItem, BigDecimal.valueOf(10), "Test", 1).setPaymentStatus(BillStatus.PENDING);
		
		when(receiptNumberGenerator.generateNumber(bill)).thenThrow(new APIException("Test exception"));
		
		billService.save(bill);
	}
	
	/**
	 * @verifies save a new bill when no existing pending bill is found for the patient
	 * @see IBillService#save(Bill)
	 */
	@Test
	public void save_shouldSaveNewBillWhenNoExistingPendingBillFound() throws Exception {
		// Create a bill for a patient that has no existing pending bills
		Patient patient = patientService.getPatient(1); // Patient 1 has no bills in test data
		
		Bill newBill = new Bill();
		newBill.setCashier(providerService.getProvider(0));
		newBill.setPatient(patient);
		newBill.setCashPoint(cashPointService.getById(0));
		newBill.setStatus(BillStatus.PENDING);
		
		// Add a valid line item using a stock item from an existing bill
		Bill template = billService.getById(0);
		StockItem stockItem = template.getLineItems().get(0).getItem();
		BillLineItem lineItem1 = newBill.addLineItem(stockItem, BigDecimal.valueOf(150), "Test item", 2);
		lineItem1.setPaymentStatus(BillStatus.PENDING);
		
		// Save the bill - should create a new bill since no pending bill exists
		Bill savedBill = billService.save(newBill);
		Context.flushSession();
		
		Assert.assertNotNull("Bill should be saved", savedBill);
		Assert.assertNotNull("Bill should have an ID", savedBill.getId());
		Assert.assertEquals("Bill should have PENDING status", BillStatus.PENDING, savedBill.getStatus());
		Assert.assertEquals("Bill should have 1 line item", 1, savedBill.getLineItems().size());
		Assert.assertEquals("Bill total should be 300", BigDecimal.valueOf(300), savedBill.getTotal());
		
		// Verify the bill can be retrieved
		Bill retrievedBill = billService.getById(savedBill.getId());
		Assert.assertNotNull("Retrieved bill should not be null", retrievedBill);
		Assert.assertEquals("Patient should match", patient.getId(), retrievedBill.getPatient().getId());
	}
	
	// region save(Bill): receipt number generation and status based on payments
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#save(Bill)
	 */
	@Test
	public void save_shouldGenerateReceiptNumberIfNotDefined() {
		// Get a StockItem from existing bill for line items
		Bill templateBill = billService.getById(0);
		StockItem stockItem = templateBill.getLineItems().get(0).getItem();
		
		Bill bill = new Bill();
		bill.setPatient(patientService.getPatient(1));
		bill.setCashier(providerService.getProvider(0));
		bill.setCashPoint(cashPointService.getById(0));
		bill.setReceiptNumber(null);
		bill.setStatus(BillStatus.PENDING);
		
		// Add a line item so the bill is valid
		BillLineItem lineItem = bill.addLineItem(stockItem, BigDecimal.valueOf(100), "Test item", 1);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		
		Bill savedBill = billService.save(bill);
		Context.flushSession();
		
		assertNotNull(savedBill.getReceiptNumber());
		assertFalse(savedBill.getReceiptNumber().isEmpty());
		assertTrue(savedBill.getReceiptNumber().startsWith("TEST-RECEIPT-"));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#save(Bill)
	 */
	@Test
	public void save_shouldNotGenerateReceiptNumberIfAlreadyDefined() {
		String existingReceiptNumber = "EXISTING-RECEIPT-123";
		Bill bill = new Bill();
		bill.setPatient(patientService.getPatient(1));
		bill.setCashier(providerService.getProvider(0));
		bill.setCashPoint(cashPointService.getById(0));
		bill.setReceiptNumber(existingReceiptNumber);
		bill.setStatus(BillStatus.PENDING);
		
		Bill savedBill = billService.save(bill);
		Context.flushSession();
		
		assertEquals(existingReceiptNumber, savedBill.getReceiptNumber());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#save(Bill)
	 */
	@Test
	public void save_shouldMergeWithExistingPendingBillForSamePatient() {
		// Get a StockItem from existing bill for line items
		Bill templateBill = billService.getById(0);
		StockItem stockItem = templateBill.getLineItems().get(0).getItem();
		
		Patient patient = patientService.getPatient(1);
		
		// Create first pending bill
		Bill firstBill = new Bill();
		firstBill.setPatient(patient);
		firstBill.setCashier(providerService.getProvider(0));
		firstBill.setCashPoint(cashPointService.getById(0));
		firstBill.setReceiptNumber("FIRST-" + UUID.randomUUID());
		firstBill.setStatus(BillStatus.PENDING);
		BillLineItem firstLineItem = firstBill.addLineItem(stockItem, BigDecimal.valueOf(50), "First item", 1);
		firstLineItem.setPaymentStatus(BillStatus.PENDING);
		Bill savedFirstBill = billService.save(firstBill);
		Context.flushSession();
		
		// Create second bill for same patient on same day
		Bill secondBill = new Bill();
		secondBill.setPatient(patient);
		secondBill.setCashier(providerService.getProvider(0));
		secondBill.setCashPoint(cashPointService.getById(0));
		secondBill.setReceiptNumber("SECOND-" + UUID.randomUUID());
		secondBill.setStatus(BillStatus.PENDING);
		BillLineItem secondLineItem = secondBill.addLineItem(stockItem, BigDecimal.valueOf(75), "Second item", 1);
		secondLineItem.setPaymentStatus(BillStatus.PENDING);
		
		Bill mergedBill = billService.save(secondBill);
		Context.flushSession();
		
		// Should merge into the first bill
		assertEquals(savedFirstBill.getId(), mergedBill.getId());
		assertEquals(2, mergedBill.getLineItems().size());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#save(Bill)
	 */
	@Test
	public void save_shouldSetStatusToPaidWhenFullyPaid() {
		// Get a StockItem from existing bill for line items
		Bill templateBill = billService.getById(0);
		StockItem stockItem = templateBill.getLineItems().get(0).getItem();
		
		Bill bill = new Bill();
		bill.setPatient(patientService.getPatient(1));
		bill.setCashier(providerService.getProvider(0));
		bill.setCashPoint(cashPointService.getById(0));
		bill.setReceiptNumber("TEST-" + UUID.randomUUID());
		bill.setStatus(BillStatus.PENDING);
		BillLineItem lineItem = bill.addLineItem(stockItem, BigDecimal.valueOf(100), "Test item", 1);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		
		Payment payment = new Payment();
		payment.setAmount(BigDecimal.valueOf(100));
		payment.setAmountTendered(BigDecimal.valueOf(100));
		payment.setInstanceType(paymentModeService.getById(0));
		bill.addPayment(payment);
		
		Bill savedBill = billService.save(bill);
		Context.flushSession();
		
		// Status should be PAID when fully paid
		assertEquals(BillStatus.PAID, savedBill.getStatus());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#save(Bill)
	 */
	@Test
	public void save_shouldSetStatusToPendingWhenPartiallyPaid() {
		// Get a StockItem from existing bill for line items
		Bill templateBill = billService.getById(0);
		StockItem stockItem = templateBill.getLineItems().get(0).getItem();
		
		Bill bill = new Bill();
		bill.setPatient(patientService.getPatient(1));
		bill.setCashier(providerService.getProvider(0));
		bill.setCashPoint(cashPointService.getById(0));
		bill.setReceiptNumber("TEST-" + UUID.randomUUID());
		bill.setStatus(BillStatus.PENDING);
		BillLineItem lineItem = bill.addLineItem(stockItem, BigDecimal.valueOf(100), "Test item", 1);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		
		Payment payment = new Payment();
		payment.setAmount(BigDecimal.valueOf(50));
		payment.setAmountTendered(BigDecimal.valueOf(50)); // Partial payment
		payment.setInstanceType(paymentModeService.getById(0));
		bill.addPayment(payment);
		
		Bill savedBill = billService.save(bill);
		Context.flushSession();
		
		// Status should remain PENDING when partially paid
		assertEquals(BillStatus.POSTED, savedBill.getStatus());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#save(Bill)
	 */
	@Test
	public void save_shouldSetStatusToPaidWhenOverpaid() {
		// Get a StockItem from existing bill for line items
		Bill templateBill = billService.getById(0);
		StockItem stockItem = templateBill.getLineItems().get(0).getItem();
		
		Bill bill = new Bill();
		bill.setPatient(patientService.getPatient(1));
		bill.setCashier(providerService.getProvider(0));
		bill.setCashPoint(cashPointService.getById(0));
		bill.setReceiptNumber("TEST-" + UUID.randomUUID());
		bill.setStatus(BillStatus.PENDING);
		BillLineItem lineItem = bill.addLineItem(stockItem, BigDecimal.valueOf(100), "Test item", 1);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		
		Payment payment = new Payment();
		payment.setAmount(BigDecimal.valueOf(150));
		payment.setAmountTendered(BigDecimal.valueOf(150)); // Overpayment
		payment.setInstanceType(paymentModeService.getById(0));
		bill.addPayment(payment);
		
		Bill savedBill = billService.save(bill);
		Context.flushSession();
		
		// Status should be PAID when overpaid
		assertEquals(BillStatus.PAID, savedBill.getStatus());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#save(Bill)
	 */
	@Test
	public void save_shouldSetStatusToPaidWithMultiplePayments() {
		// Get a StockItem from existing bill for line items
		Bill templateBill = billService.getById(0);
		StockItem stockItem = templateBill.getLineItems().get(0).getItem();
		
		Bill bill = new Bill();
		bill.setPatient(patientService.getPatient(1));
		bill.setCashier(providerService.getProvider(0));
		bill.setCashPoint(cashPointService.getById(0));
		bill.setReceiptNumber("TEST-" + UUID.randomUUID());
		bill.setStatus(BillStatus.PENDING);
		BillLineItem lineItem = bill.addLineItem(stockItem, BigDecimal.valueOf(100), "Test item", 1);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		
		Payment payment1 = new Payment();
		payment1.setAmount(BigDecimal.valueOf(40));
		payment1.setAmountTendered(BigDecimal.valueOf(40));
		payment1.setInstanceType(paymentModeService.getById(0));
		bill.addPayment(payment1);
		
		Payment payment2 = new Payment();
		payment2.setAmount(BigDecimal.valueOf(60));
		payment2.setAmountTendered(BigDecimal.valueOf(60));
		payment2.setInstanceType(paymentModeService.getById(1));
		bill.addPayment(payment2);
		
		Bill savedBill = billService.save(bill);
		Context.flushSession();
		
		// Status should be PAID when multiple payments total to full amount
		assertEquals(BillStatus.PAID, savedBill.getStatus());
		assertEquals(2, savedBill.getPayments().size());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#save(Bill)
	 */
	@Test
	public void save_shouldSetStatusToPendingWhenNoPayments() {
		// Get a StockItem from existing bill for line items
		Bill templateBill = billService.getById(0);
		StockItem stockItem = templateBill.getLineItems().get(0).getItem();
		
		Bill bill = new Bill();
		bill.setPatient(patientService.getPatient(1));
		bill.setCashier(providerService.getProvider(0));
		bill.setCashPoint(cashPointService.getById(0));
		bill.setReceiptNumber("TEST-" + UUID.randomUUID());
		bill.setStatus(BillStatus.PENDING);
		BillLineItem lineItem = bill.addLineItem(stockItem, BigDecimal.valueOf(100), "Test item", 1);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		// No payments added
		
		Bill savedBill = billService.save(bill);
		Context.flushSession();
		
		// Status should remain PENDING when no payments
		assertEquals(BillStatus.PENDING, savedBill.getStatus());
		assertTrue(savedBill.getPayments() == null || savedBill.getPayments().isEmpty());
	}
	
	// region Merging behaviour with different statuses and payments
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#save(Bill)
	 */
	@Test
	public void save_shouldNotMergeWhenExistingBillIsPosted() {
		// Get a StockItem from existing bill for line items
		Bill templateBill = billService.getById(0);
		StockItem stockItem = templateBill.getLineItems().get(0).getItem();
		
		Patient patient = patientService.getPatient(1);
		
		// Create first bill with partial payment (becomes POSTED)
		Bill firstBill = new Bill();
		firstBill.setPatient(patient);
		firstBill.setCashier(providerService.getProvider(0));
		firstBill.setCashPoint(cashPointService.getById(0));
		firstBill.setReceiptNumber("FIRST-" + UUID.randomUUID());
		firstBill.setStatus(BillStatus.PENDING);
		BillLineItem firstLineItem = firstBill.addLineItem(stockItem, BigDecimal.valueOf(100), "First item", 1);
		firstLineItem.setPaymentStatus(BillStatus.PENDING);
		
		Payment firstPayment = new Payment();
		firstPayment.setAmount(BigDecimal.valueOf(50));
		firstPayment.setAmountTendered(BigDecimal.valueOf(50));
		firstPayment.setInstanceType(paymentModeService.getById(0));
		firstBill.addPayment(firstPayment);
		
		Bill savedFirstBill = billService.save(firstBill);
		Context.flushSession();
		assertEquals(BillStatus.POSTED, savedFirstBill.getStatus());
		
		// Create second bill for same patient
		Bill secondBill = new Bill();
		secondBill.setPatient(patient);
		secondBill.setCashier(providerService.getProvider(0));
		secondBill.setCashPoint(cashPointService.getById(0));
		secondBill.setReceiptNumber("SECOND-" + UUID.randomUUID());
		secondBill.setStatus(BillStatus.PENDING);
		BillLineItem secondLineItem = secondBill.addLineItem(stockItem, BigDecimal.valueOf(50), "Second item", 1);
		secondLineItem.setPaymentStatus(BillStatus.PENDING);
		
		Payment secondPayment = new Payment();
		secondPayment.setAmount(BigDecimal.valueOf(50));
		secondPayment.setAmountTendered(BigDecimal.valueOf(50));
		secondPayment.setInstanceType(paymentModeService.getById(0));
		secondBill.addPayment(secondPayment);
		
		Bill savedSecondBill = billService.save(secondBill);
		Context.flushSession();
		
		// Should NOT merge - POSTED bills are not merged (searchBill only finds PENDING bills)
		assertNotEquals(savedFirstBill.getId(), savedSecondBill.getId());
		
		assertEquals(3, billService.getBillsByPatient(patient, null).size());
	}
	
	// region save(Bill): payment calculation semantics
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#save(Bill)
	 */
	@Test
	public void save_shouldUseAmountTenderedForPaymentCalculation() {
		// Get a StockItem from existing bill for line items
		Bill templateBill = billService.getById(0);
		StockItem stockItem = templateBill.getLineItems().get(0).getItem();
		
		Bill bill = new Bill();
		bill.setPatient(patientService.getPatient(1));
		bill.setCashier(providerService.getProvider(0));
		bill.setCashPoint(cashPointService.getById(0));
		bill.setReceiptNumber("TEST-" + UUID.randomUUID());
		bill.setStatus(BillStatus.PENDING);
		BillLineItem lineItem = bill.addLineItem(stockItem, BigDecimal.valueOf(100), "Test item", 1);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		
		Payment payment = new Payment();
		payment.setAmount(BigDecimal.valueOf(80)); // Amount
		payment.setAmountTendered(BigDecimal.valueOf(100)); // Amount tendered (what actually counts)
		payment.setInstanceType(paymentModeService.getById(0));
		bill.addPayment(payment);
		
		Bill savedBill = billService.save(bill);
		Context.flushSession();
		
		// Status should be PAID because amountTendered (100) >= total (100)
		// Not amount (80)
		assertEquals(BillStatus.PAID, savedBill.getStatus());
		assertEquals(BigDecimal.valueOf(100), savedBill.getTotalPayments());
	}
	
	// region getBillsByPatient(...)
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillsByPatient(Patient, PagingInfo)
	 */
	@Test
	public void getBillsByPatient_shouldReturnBillsForPatient() {
		Patient patient = patientService.getPatient(1);
		assertNotNull(patient);
		
		List<Bill> bills = billService.getBillsByPatient(patient, null);
		
		assertNotNull(bills);
		for (Bill bill : bills) {
			assertEquals(patient.getId(), bill.getPatient().getId());
		}
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillsByPatient(Patient, PagingInfo)
	 */
	@Test
	public void getBillsByPatient_shouldReturnEmptyListWhenPatientHasNoBills() {
		Patient patient = patientService.getPatient(999);
		if (patient == null) {
			// Create a test patient if needed
			patient = new Patient();
			patient.setId(999);
		}
		
		List<Bill> bills = billService.getBillsByPatient(patient, null);
		
		assertNotNull(bills);
		assertTrue(bills.isEmpty());
	}
	
	// region getBills(BillSearch ...)
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBills(BillSearch)
	 */
	@Test
	public void getBills_shouldThrowNullPointerExceptionIfBillSearchIsNull() {
		assertThrows(NullPointerException.class, () -> billService.getBills((BillSearch) null));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBills(BillSearch)
	 */
	@Test
	public void getBills_shouldThrowNullPointerExceptionIfBillSearchTemplateIsNull() {
		BillSearch billSearch = new BillSearch();
		billSearch.setTemplate(null);
		
		assertThrows(NullPointerException.class, () -> billService.getBills(billSearch));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBills(BillSearch)
	 */
	@Test
	public void getBills_shouldReturnBillsFilteredByPatient() {
		Patient patient = patientService.getPatient(1);
		Bill searchTemplate = new Bill();
		searchTemplate.setPatient(patient);
		BillSearch billSearch = new BillSearch(searchTemplate, false);
		
		List<Bill> bills = billService.getBills(billSearch);
		
		assertNotNull(bills);
		for (Bill bill : bills) {
			assertEquals(patient.getId(), bill.getPatient().getId());
		}
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBills(BillSearch)
	 */
	@Test
	public void getBills_shouldReturnBillsFilteredByStatus() {
		Bill searchTemplate = new Bill();
		searchTemplate.setStatus(BillStatus.PENDING);
		BillSearch billSearch = new BillSearch(searchTemplate, false);
		
		List<Bill> bills = billService.getBills(billSearch);
		
		assertNotNull(bills);
		for (Bill bill : bills) {
			assertEquals(BillStatus.PENDING, bill.getStatus());
		}
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBills(BillSearch)
	 */
	@Test
	public void getBills_shouldReturnBillsFilteredByCashPoint() {
		CashPoint cashPoint = cashPointService.getById(0);
		Bill searchTemplate = new Bill();
		searchTemplate.setCashPoint(cashPoint);
		BillSearch billSearch = new BillSearch(searchTemplate, false);
		
		List<Bill> bills = billService.getBills(billSearch);
		
		assertNotNull(bills);
		for (Bill bill : bills) {
			assertEquals(cashPoint.getId(), bill.getCashPoint().getId());
		}
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBills(BillSearch, PagingInfo)
	 */
	@Test
	public void getBills_shouldReturnPagedBillsWhenPagingInfoSpecified() {
		Bill searchTemplate = new Bill();
		BillSearch billSearch = new BillSearch(searchTemplate, false);
		PagingInfo pagingInfo = new PagingInfo();
		pagingInfo.setPage(1);
		pagingInfo.setPageSize(5);
		
		List<Bill> bills = billService.getBills(billSearch, pagingInfo);
		
		assertNotNull(bills);
		assertTrue(bills.size() <= 5);
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBills(BillSearch, PagingInfo)
	 */
	@Test
	public void getBills_shouldReturnAllBillsWhenPagingInfoIsNull() {
		Bill searchTemplate = new Bill();
		BillSearch billSearch = new BillSearch(searchTemplate, false);
		
		List<Bill> bills = billService.getBills(billSearch, null);
		
		assertNotNull(bills);
		// Should return all bills without pagination
	}
	
	// region getAll(...)
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getAll(boolean, PagingInfo)
	 */
	@Test
	public void getAll_shouldReturnAllBillsIncludingVoided() {
		PagingInfo pagingInfo = null;
		List<Bill> bills = billService.getAll(true, pagingInfo);
		
		assertNotNull(bills);
		// Should include voided bills
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getAll(boolean, PagingInfo)
	 */
	@Test
	public void getAll_shouldReturnAllBillsExcludingVoided() {
		PagingInfo pagingInfo = null;
		List<Bill> bills = billService.getAll(false, pagingInfo);
		
		assertNotNull(bills);
		// Should exclude voided bills
		for (Bill bill : bills) {
			assertFalse(bill.getVoided());
		}
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getAll()
	 */
	@Test
	public void getAll_shouldReturnAllBillsWithoutParameters() {
		List<Bill> bills = billService.getAll();
		
		assertNotNull(bills);
		// Should return all bills
	}
	
	// region getByUuid(...)
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getByUuid(String)
	 */
	@Test
	public void getByUuid_shouldReturnBillWithSpecifiedUuid() {
		Bill existingBill = billService.getById(0);
		assertNotNull(existingBill);
		assertNotNull(existingBill.getUuid());
		
		Bill bill = billService.getByUuid(existingBill.getUuid());
		
		assertNotNull(bill);
		assertEquals(existingBill.getUuid(), bill.getUuid());
		assertEquals(existingBill.getId(), bill.getId());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getByUuid(String)
	 */
	@Test
	public void getByUuid_shouldReturnNullIfUuidNotFound() {
		Bill bill = billService.getByUuid("nonexistent-uuid-12345");
		
		assertNull(bill);
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getByUuid(String)
	 */
	@Test
	public void getByUuid_shouldRemoveNullLineItems() {
		Bill existingBill = billService.getById(0);
		if (existingBill != null && existingBill.getUuid() != null) {
			Bill bill = billService.getByUuid(existingBill.getUuid());
			
			assertNotNull(bill);
			if (bill.getLineItems() != null) {
				for (Object item : bill.getLineItems()) {
					assertNotNull("Line items should not contain null values", item);
				}
			}
		}
	}
	
}
