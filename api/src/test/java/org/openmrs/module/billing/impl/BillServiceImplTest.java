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
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.IBillService;
import org.openmrs.module.billing.api.ICashPointService;
import org.openmrs.module.billing.api.IPaymentModeService;
import org.openmrs.module.billing.api.IReceiptNumberGenerator;
import org.openmrs.module.billing.api.ReceiptNumberGeneratorFactory;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.api.model.Payment;
import org.openmrs.module.billing.api.search.BillSearch;
import org.openmrs.module.billing.base.TestConstants;
import org.openmrs.module.stockmanagement.api.model.StockItem;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Concrete implementation of IBillServiceTest for testing BillServiceImpl
 */
@ExtendWith(MockitoExtension.class)
public class BillServiceImplTest extends BaseModuleContextSensitiveTest {
	
	@Mock
	private IReceiptNumberGenerator receiptNumberGenerator;
	
	private IBillService billService;
	
	private ProviderService providerService;
	
	private PatientService patientService;
	
	private ICashPointService cashPointService;

	private IPaymentModeService paymentModeService;
	
	private MockedStatic<ReceiptNumberGeneratorFactory> mockedFactory;

	@BeforeEach
	public void before() throws Exception {
		billService = Context.getService(IBillService.class);

		providerService = Context.getProviderService();
		patientService = Context.getPatientService();
		cashPointService = Context.getService(ICashPointService.class);
		paymentModeService = Context.getService(IPaymentModeService.class);
		
		// Mock static methods of ReceiptNumberGeneratorFactory
		mockedFactory = mockStatic(ReceiptNumberGeneratorFactory.class, CALLS_REAL_METHODS);
		when(ReceiptNumberGeneratorFactory.getGenerator()).thenReturn(receiptNumberGenerator);
		// Default behavior for tests that expect a generated number with standard prefix
		// Using lenient() so it doesn't fail if not used in all tests
		final int[] genCounter = { 1 };
		lenient().when(receiptNumberGenerator.generateNumber(any(Bill.class))).thenAnswer(invocation -> "TEST-RECEIPT-" + (genCounter[0]++));

		executeDataSet(TestConstants.BASE_DATASET_DIR + "CoreTest-2.0.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "StockOperationType.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "PaymentModeTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillTest.xml");
	}
	
	@org.junit.jupiter.api.AfterEach
	public void afterEach() {
		if (mockedFactory != null) {
			mockedFactory.close();
		}
	}
	
	// region Argument / validation tests
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#save(Bill)
	 */
	@Test
	public void save_shouldThrowNullPointerExceptionIfBillIsNull() {
		assertThrows(NullPointerException.class, () -> {
			billService.save(null);
		});
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillByReceiptNumber(String)
	 */
	@Test
	public void getBillByReceiptNumber_shouldThrowIllegalArgumentExceptionIfReceiptNumberIsNull() {
		assertThrows(IllegalArgumentException.class, () -> {
			billService.getBillByReceiptNumber(null);
		});
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillByReceiptNumber(String)
	 */
	@Test
	public void getBillByReceiptNumber_shouldThrowIllegalArgumentExceptionIfReceiptNumberIsEmpty() {
		assertThrows(IllegalArgumentException.class, () -> {
			billService.getBillByReceiptNumber("");
		});
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillByReceiptNumber(String)
	 */
	@Test
	public void getBillByReceiptNumber_shouldThrowIllegalArgumentExceptionIfReceiptNumberIsTooLong() {
		String longReceiptNumber = RandomStringUtils.randomAlphanumeric(1999);
		assertThrows(IllegalArgumentException.class, () -> {
			billService.getBillByReceiptNumber(longReceiptNumber);
		});
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillsByPatient(Patient,
	 *      org.openmrs.module.billing.api.base.PagingInfo)
	 */
	@Test
	public void getBillsByPatient_shouldThrowNullPointerExceptionIfPatientIsNull() {
		assertThrows(NullPointerException.class, () -> {
			billService.getBillsByPatient(null, null);
		});
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillsByPatientId(int,
	 *      org.openmrs.module.billing.api.base.PagingInfo)
	 */
	@Test
	public void getBillsByPatientId_shouldThrowIllegalArgumentExceptionIfPatientIdIsNegative() {
		assertThrows(IllegalArgumentException.class, () -> {
			billService.getBillsByPatientId(-1, null);
		});
	}
	
	// region Basic retrieval and listing tests
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getAll()
	 */
	@Test
	public void getAll_shouldReturnAllBills() {
		List<Bill> bills = billService.getAll();
		assertNotNull(bills, "getAll() should return a non-null list");
		for (Bill bill : bills) {
			if (bill.getLineItems() != null) {
				for (Object item : bill.getLineItems()) {
					assertNotNull(item, "Line items should not contain null values");
				}
			}
		}
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getById(int)
	 */
	@Test
	public void getById_shouldReturnBillWithSpecifiedId() {
		Bill bill = billService.getById(1);
		
		assertNotNull(bill, "getById(1) should return a non-null bill");
		assertEquals(1, bill.getId(), "Returned bill should have ID 1");
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillByReceiptNumber(String)
	 */
	@Test
	public void getBillByReceiptNumber_shouldReturnBillWithSpecifiedReceiptNumber() {
		Bill bill = billService.getBillByReceiptNumber("test 1 receipt number");
		assertNotNull(bill, "getBillByReceiptNumber should return a non-null bill");
		assertEquals("test 1 receipt number", bill.getReceiptNumber(), "Returned bill should have the expected receipt number");
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillByReceiptNumber(String)
	 */
	@Test
	public void getBillByReceiptNumber_shouldReturnNullIfReceiptNumberNotFound() {
		Bill bill = billService.getBillByReceiptNumber("nonexistent receipt number");
		assertNull(bill, "getBillByReceiptNumber should return null for non-existent receipt number");
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillsByPatientId(int,
	 *      org.openmrs.module.billing.api.base.PagingInfo)
	 */
	@Test
	public void getBillsByPatientId_shouldReturnBillsForPatient() {
		List<Bill> bills = billService.getBillsByPatientId(0, null);
		assertNotNull(bills, "getBillsByPatientId should return a non-null list");
		assertFalse(bills.isEmpty(), "getBillsByPatientId should return a non-empty list for patient 0");
		assertEquals(1, bills.size(), "getBillsByPatientId should return 1 bill for patient 0");
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillsByPatientId(int,
	 *      org.openmrs.module.billing.api.base.PagingInfo)
	 */
	@Test
	public void getBillsByPatientId_shouldReturnEmptyListWhenPatientHasNoBills() {
		List<Bill> bills = billService.getBillsByPatientId(999, null);
		assertNotNull(bills, "getBillsByPatientId should return a non-null list even when patient has no bills");
		assertEquals(0, bills.size(), "getBillsByPatientId should return an empty list for patient with no bills");
	}
	
	// region save(Bill): creation, merging, and receipt number generation
	
	/**
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
		
		assertNotNull(savedBill, "Bill should be saved");
		assertNotNull(savedBill.getId(), "Bill should have an ID");
		
		// Verify the bill can be retrieved and matches what was saved
		Bill retrievedBill = billService.getById(savedBill.getId());
		assertNotNull(retrievedBill, "Retrieved bill should not be null");
		assertEquals(savedBill.getId(), retrievedBill.getId(), "Retrieved bill should have the same ID");
		assertEquals(savedBill.getStatus(), retrievedBill.getStatus(), "Retrieved bill should have the same status");
		assertEquals(savedBill.getLineItems().size(), retrievedBill.getLineItems().size(), "Retrieved bill should have the same number of line items");
		assertEquals(savedBill.getTotal(), retrievedBill.getTotal(), "Retrieved bill should have the same total");
		assertEquals(patient.getId(), retrievedBill.getPatient().getId(), "Retrieved bill should have the same patient");
	}
	
	/**
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
		
		assertNotNull(saved.getId(), "After being saved, bill should have an id");
		assertEquals(receiptNumber, saved.getReceiptNumber(), "Saved bill should have the generated receipt number");
		
		verify(receiptNumberGenerator, times(1)).generateNumber(bill);
	}
	
	/**
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
		
		assertNotNull(saved.getId(), "After being saved, bill should have an id");
		assertEquals(receiptNumber, saved.getReceiptNumber(), "Saved bill should preserve the existing receipt number");
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#save(Bill)
	 */
	@Test
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
		
		assertThrows(APIException.class, () -> {
			billService.save(bill);
		});
	}
	
	// region save(Bill): receipt number generation and status based on payments
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
		
		// Should merge into the first bill
		assertEquals(savedFirstBill.getId(), mergedBill.getId(), "Second bill should merge into the first bill (same ID)");
		assertEquals(2, mergedBill.getLineItems().size(), "Merged bill should have 2 line items");
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
		
		// Status should be PAID when multiple payments total to full amount
		assertEquals(BillStatus.PAID, savedBill.getStatus(), "Bill status should be PAID when multiple payments total to full amount");
		assertEquals(2, savedBill.getPayments().size(), "Bill should have 2 payments");
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
		
		// Status should be PAID when fully paid
		assertEquals(BillStatus.PAID, savedBill.getStatus(), "Bill status should be PAID when fully paid");
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
		
		// Status should be PAID when overpaid
		assertEquals(BillStatus.PAID, savedBill.getStatus(), "Bill status should be PAID when overpaid");
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
		
		// Status should remain PENDING when partially paid
		assertEquals(BillStatus.POSTED, savedBill.getStatus(), "Bill status should be POSTED when partially paid");
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
		
		// Status should remain PENDING when no payments
		assertEquals(BillStatus.PENDING, savedBill.getStatus(), "Bill status should remain PENDING when no payments");
		assertTrue(savedBill.getPayments() == null || savedBill.getPayments().isEmpty(), "Bill should have no payments");
	}
	
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
		
		assertNotNull(savedBill.getReceiptNumber(), "Saved bill should have a generated receipt number");
		assertFalse(savedBill.getReceiptNumber().isEmpty(), "Generated receipt number should not be empty");
		assertTrue(savedBill.getReceiptNumber().startsWith("TEST-RECEIPT-"), "Generated receipt number should start with TEST-RECEIPT-");
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
		
		assertEquals(existingReceiptNumber, savedBill.getReceiptNumber(), "Saved bill should preserve the existing receipt number");
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
		
		// Create first bill (no payments initially)
		Bill firstBill = new Bill();
		firstBill.setPatient(patient);
		firstBill.setCashier(providerService.getProvider(0));
		firstBill.setCashPoint(cashPointService.getById(0));
		firstBill.setReceiptNumber("FIRST-" + UUID.randomUUID());
		firstBill.setStatus(BillStatus.PENDING);
		BillLineItem firstLineItem = firstBill.addLineItem(stockItem, BigDecimal.valueOf(100), "First item", 1);
		firstLineItem.setPaymentStatus(BillStatus.PENDING);
		
		Bill savedFirstBill = billService.save(firstBill);
		assertEquals(BillStatus.PENDING, savedFirstBill.getStatus(), "First bill should have PENDING status when created");
		
		// Add partial payment to the first bill (becomes POSTED)
		Payment firstPayment = new Payment();
		firstPayment.setAmount(BigDecimal.valueOf(50));
		firstPayment.setAmountTendered(BigDecimal.valueOf(50));
		firstPayment.setInstanceType(paymentModeService.getById(0));
		savedFirstBill.addPayment(firstPayment);
		savedFirstBill = billService.save(savedFirstBill);
		assertEquals(BillStatus.POSTED, savedFirstBill.getStatus(), "First bill should have POSTED status after partial payment");
		
		// Create second bill for same patient (no payments initially)
		Bill secondBill = new Bill();
		secondBill.setPatient(patient);
		secondBill.setCashier(providerService.getProvider(0));
		secondBill.setCashPoint(cashPointService.getById(0));
		secondBill.setReceiptNumber("SECOND-" + UUID.randomUUID());
		secondBill.setStatus(BillStatus.PENDING);
		BillLineItem secondLineItem = secondBill.addLineItem(stockItem, BigDecimal.valueOf(50), "Second item", 1);
		secondLineItem.setPaymentStatus(BillStatus.PENDING);
		
		Bill savedSecondBill = billService.save(secondBill);
		
		// Should NOT merge - POSTED bills are not merged (searchBill only finds PENDING bills)
		assertNotEquals(savedFirstBill.getId(), savedSecondBill.getId(), "POSTED bills should not merge with new bills");
		
		assertEquals(3, billService.getBillsByPatient(patient, null).size(), "Patient should have 3 bills total");
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
		
		// Status should be PAID because amountTendered (100) >= total (100)
		// Not amount (80)
		assertEquals(BillStatus.PAID, savedBill.getStatus(), "Bill status should be PAID because amountTendered (100) >= total (100)");
		assertEquals(BigDecimal.valueOf(100), savedBill.getTotalPayments(), "Total payments should use amountTendered (100), not amount (80)");
	}
	
	// region getBillsByPatient(...)
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillsByPatient(Patient, PagingInfo)
	 */
	@Test
	public void getBillsByPatient_shouldReturnBillsForPatient() {
		Patient patient = patientService.getPatient(1);
		assertNotNull(patient, "Test patient should exist");
		
		List<Bill> bills = billService.getBillsByPatient(patient, null);
		
		assertNotNull(bills, "getBillsByPatient should return a non-null list");
		for (Bill bill : bills) {
			assertEquals(patient.getId(), bill.getPatient().getId(), "All returned bills should belong to the specified patient");
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
		
		assertNotNull(bills, "getBillsByPatient should return a non-null list even when patient has no bills");
		assertTrue(bills.isEmpty(), "getBillsByPatient should return an empty list for patient with no bills");
	}
	
	// region getBills(BillSearch ...)
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
		
		assertNotNull(bills, "getBills should return a non-null list");
		for (Bill bill : bills) {
			assertEquals(patient.getId(), bill.getPatient().getId(), "All returned bills should match the patient filter");
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
		
		assertNotNull(bills, "getBills should return a non-null list");
		for (Bill bill : bills) {
			assertEquals(BillStatus.PENDING, bill.getStatus(), "All returned bills should have PENDING status");
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
		
		assertNotNull(bills, "getBills should return a non-null list");
		for (Bill bill : bills) {
			assertEquals(cashPoint.getId(), bill.getCashPoint().getId(), "All returned bills should match the cash point filter");
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
		
		assertNotNull(bills, "getBills should return a non-null list");
		assertTrue(bills.size() <= 5, "Paged results should not exceed page size of 5");
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBills(BillSearch, PagingInfo)
	 */
	@Test
	public void getBills_shouldReturnAllBillsWhenPagingInfoIsNull() {
		Bill searchTemplate = new Bill();
		BillSearch billSearch = new BillSearch(searchTemplate, false);
		
		List<Bill> bills = billService.getBills(billSearch, null);
		
		assertNotNull(bills, "getBills should return a non-null list when paging is null");
		// Should return all bills without pagination
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBills(BillSearch)
	 */
	@Test
	public void getBills_shouldThrowNullPointerExceptionIfBillSearchIsNull() {
		assertThrows(NullPointerException.class, () -> {
			billService.getBills((BillSearch) null);
		});
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBills(BillSearch)
	 */
	@Test
	public void getBills_shouldThrowNullPointerExceptionIfBillSearchTemplateIsNull() {
		BillSearch billSearch = new BillSearch();
		billSearch.setTemplate(null);
		
		assertThrows(NullPointerException.class, () -> {
			billService.getBills(billSearch);
		});
	}
	
	// region getAll(...)
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getAll(boolean, PagingInfo)
	 */
	@Test
	public void getAll_shouldReturnAllBillsIncludingVoided() {
		PagingInfo pagingInfo = null;
		List<Bill> bills = billService.getAll(true, pagingInfo);
		
		assertNotNull(bills, "getAll should return a non-null list");
		// Should include voided bills
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getAll(boolean, PagingInfo)
	 */
	@Test
	public void getAll_shouldReturnAllBillsExcludingVoided() {
		PagingInfo pagingInfo = null;
		List<Bill> bills = billService.getAll(false, pagingInfo);
		
		assertNotNull(bills, "getAll should return a non-null list");
		// Should exclude voided bills
		for (Bill bill : bills) {
			assertFalse(bill.getVoided(), "getAll with includeVoided=false should not return voided bills");
		}
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getAll()
	 */
	@Test
	public void getAll_shouldReturnAllBillsWithoutParameters() {
		List<Bill> bills = billService.getAll();
		
		assertNotNull(bills, "getAll() should return a non-null list");
		// Should return all bills
	}
	
	// region getByUuid(...)
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getByUuid(String)
	 */
	@Test
	public void getByUuid_shouldReturnBillWithSpecifiedUuid() {
		Bill existingBill = billService.getById(0);
		assertNotNull(existingBill, "Test bill with ID 0 should exist");
		assertNotNull(existingBill.getUuid(), "Test bill should have a UUID");
		
		Bill bill = billService.getByUuid(existingBill.getUuid());
		
		assertNotNull(bill, "getByUuid should return a non-null bill");
		assertEquals(existingBill.getUuid(), bill.getUuid(), "Returned bill should have the expected UUID");
		assertEquals(existingBill.getId(), bill.getId(), "Returned bill should have the expected ID");
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getByUuid(String)
	 */
	@Test
	public void getByUuid_shouldReturnNullIfUuidNotFound() {
		Bill bill = billService.getByUuid("nonexistent-uuid-12345");
		
		assertNull(bill, "getByUuid should return null for non-existent UUID");
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getByUuid(String)
	 */
	@Test
	public void getByUuid_shouldRemoveNullLineItems() {
		Bill existingBill = billService.getById(0);
		if (existingBill != null && existingBill.getUuid() != null) {
			Bill bill = billService.getByUuid(existingBill.getUuid());
			
			assertNotNull(bill, "getByUuid should return a non-null bill");
			if (bill.getLineItems() != null) {
				for (Object item : bill.getLineItems()) {
					assertNotNull(item, "Line items should not contain null values");
				}
			}
		}
	}
	
}
