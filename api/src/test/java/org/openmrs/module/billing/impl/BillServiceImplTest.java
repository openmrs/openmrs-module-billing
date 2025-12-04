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

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.IBillService;
import org.openmrs.module.billing.api.ICashPointService;
import org.openmrs.module.billing.api.IPaymentModeService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.Payment;
import org.openmrs.module.billing.api.model.PaymentMode;
import org.openmrs.module.stockmanagement.api.model.StockItem;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

public class BillServiceImplTest extends BaseModuleContextSensitiveTest {
	
	private IBillService billService;
	
	private ProviderService providerService;
	
	private PatientService patientService;
	
	private ICashPointService cashPointService;
	
	private IPaymentModeService paymentModeService;
	
	@BeforeEach
	public void setup() {
		billService = Context.getService(IBillService.class);
		providerService = Context.getProviderService();
		patientService = Context.getPatientService();
		cashPointService = Context.getService(ICashPointService.class);
		paymentModeService = Context.getService(IPaymentModeService.class);
		
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "StockOperationType.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "PaymentModeTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillTest.xml");
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#saveBill(Bill)
	 */
	@Test
	public void save_Bill_shouldThrowNullPointerExceptionIfBillIsNull() {
		assertThrows(NullPointerException.class, () -> billService.saveBill(null));
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
					assertNotNull(item, "Line items should not contain null values");
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
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#saveBill(Bill)
	 */
	@Test
	public void save_Bill_shouldCreateNewBillWithNewItem() {
		Patient patient = patientService.getPatient(1);
		assertNotNull(patient);
		
		Bill templateBill = billService.getById(0);
		assertNotNull(templateBill);
		assertFalse(templateBill.getLineItems().isEmpty());
		
		Bill newBill = new Bill();
		newBill.setCashier(providerService.getProvider(0));
		newBill.setPatient(patient);
		newBill.setCashPoint(cashPointService.getById(0));
		newBill.setReceiptNumber("TEST-" + UUID.randomUUID());
		newBill.setStatus(BillStatus.PENDING);
		
		BillLineItem existingItem = templateBill.getLineItems().get(0);
		StockItem stockItem = existingItem.getItem();
		
		BillLineItem lineItem = newBill.addLineItem(stockItem, BigDecimal.valueOf(150), "New price", 2);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		lineItem.setUuid(UUID.randomUUID().toString());
		
		Bill savedBill = billService.saveBill(newBill);
		Context.flushSession();
		
		assertNotNull(savedBill);
		assertNotNull(savedBill.getId());
		assertEquals(BillStatus.PENDING, savedBill.getStatus());
		assertEquals(1, savedBill.getLineItems().size());
		assertEquals(BigDecimal.valueOf(300), savedBill.getTotal());
		
		Bill retrievedBill = billService.getById(savedBill.getId());
		assertNotNull(retrievedBill);
		assertEquals(patient.getId(), retrievedBill.getPatient().getId());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#saveBill(Bill)
	 */
	@Test
	public void save_Bill_shouldUpdateExistingBillWithUpdatedBillItem() {
		Bill pendingBill = billService.getById(2);
		assertNotNull(pendingBill);
		assertEquals(BillStatus.PENDING, pendingBill.getStatus());
		assertFalse(pendingBill.getLineItems().isEmpty());
		
		BillLineItem firstItem = pendingBill.getLineItems().get(0);
		BigDecimal updatedPrice = firstItem.getPrice().add(BigDecimal.TEN);
		firstItem.setPrice(updatedPrice);
		
		billService.saveBill(pendingBill);
		Context.flushSession();
		Context.clearSession();
		
		Bill updatedBill = billService.getById(2);
		
		assertEquals(pendingBill, updatedBill);
		assertEquals(updatedPrice, updatedBill.getLineItems().get(0).getPrice());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getById(int)
	 */
	@Test
	public void getById_shouldReturnBillWithSpecifiedId() {
		Bill bill = billService.getById(1);
		assertNotNull(bill);
		assertEquals(1, bill.getId());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getById(int)
	 */
	@Test
	public void getById_shouldRemoveNullLineItems() {
		Bill bill = billService.getById(1);
		assertNotNull(bill);
		if (bill.getLineItems() != null) {
			for (Object item : bill.getLineItems()) {
				assertNotNull(item, "Line items should not contain null values");
			}
		}
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#saveBill(Bill)
	 */
	@Test
	public void save_Bill_shouldAllowAddingLineItemsToPendingBill() {
		// Get the PENDING bill from test data (bill_id=2)
		Bill pendingBill = billService.getById(2);
		assertNotNull(pendingBill);
		assertEquals(BillStatus.PENDING, pendingBill.getStatus());
		
		// Add a new line item
		BillLineItem newLineItem = new BillLineItem();
		newLineItem.setPrice(BigDecimal.valueOf(25.50));
		newLineItem.setQuantity(2);
		newLineItem.setPaymentStatus(BillStatus.PENDING);
		newLineItem.setLineItemOrder(pendingBill.getLineItems().size());
		pendingBill.addLineItem(newLineItem);
		
		// Should not throw exception
		Bill savedBill = billService.saveBill(pendingBill);
		assertNotNull(savedBill);
		assertTrue(savedBill.getLineItems().size() > 0);
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#saveBill(Bill)
	 */
	@Test
	public void save_Bill_shouldThrowExceptionWhenAddingLineItemsToPostedBill() {
		// Get the POSTED bill from test data (bill_id=0)
		Bill postedBill = billService.getById(0);
		assertNotNull(postedBill);
		assertEquals(BillStatus.POSTED, postedBill.getStatus());
		
		// Try to add a new line item
		BillLineItem newLineItem = new BillLineItem();
		newLineItem.setPrice(BigDecimal.valueOf(25.50));
		newLineItem.setQuantity(2);
		
		// Should throw exception
		assertThrows(IllegalStateException.class, () -> postedBill.addLineItem(newLineItem));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#saveBill(Bill)
	 */
	@Test
	public void save_Bill_shouldThrowExceptionWhenAddingLineItemsToPaidBill() {
		// Get the PAID bill from test data (bill_id=1)
		Bill paidBill = billService.getById(1);
		assertNotNull(paidBill);
		assertEquals(BillStatus.PAID, paidBill.getStatus());
		
		// Try to add a new line item
		BillLineItem newLineItem = new BillLineItem();
		newLineItem.setPrice(BigDecimal.valueOf(25.50));
		newLineItem.setQuantity(2);
		
		// Should throw exception
		assertThrows(IllegalStateException.class, () -> paidBill.addLineItem(newLineItem));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#saveBill(Bill)
	 */
	@Test
	public void save_Bill_shouldAllowRemovingLineItemsFromPendingBill() {
		// Get the PENDING bill from test data (bill_id=2)
		Bill pendingBill = billService.getById(2);
		assertNotNull(pendingBill);
		assertEquals(BillStatus.PENDING, pendingBill.getStatus());
		
		int originalSize = pendingBill.getLineItems().size();
		assertTrue(originalSize > 0);
		
		// Remove a line item
		BillLineItem itemToRemove = pendingBill.getLineItems().get(0);
		pendingBill.removeLineItem(itemToRemove);
		
		// Should not throw exception
		Bill savedBill = billService.saveBill(pendingBill);
		assertNotNull(savedBill);
		assertTrue(savedBill.getLineItems().size() < originalSize);
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#saveBill(Bill)
	 */
	@Test
	public void save_Bill_shouldThrowExceptionWhenRemovingLineItemsFromPostedBill() {
		// Get the POSTED bill from test data (bill_id=0)
		Bill postedBill = billService.getById(0);
		assertNotNull(postedBill);
		assertEquals(BillStatus.POSTED, postedBill.getStatus());
		
		BillLineItem itemToRemove = postedBill.getLineItems().get(0);
		
		// Should throw exception
		assertThrows(IllegalStateException.class, () -> postedBill.removeLineItem(itemToRemove));
	}
	
	@Test
	public void save_Bill_shouldNotThrowExceptionForPendingBill() {
		Bill pendingBill = billService.getById(2);
		assertNotNull(pendingBill);
		assertEquals(BillStatus.PENDING, pendingBill.getStatus());
		pendingBill.setReceiptNumber("ABV");
		assertDoesNotThrow(() -> billService.saveBill(pendingBill));
	}
	
	@Test
	public void save_Bill_shouldThrowIllegalStateExceptionForPostedBill() {
		Bill postedBill = billService.getById(0);
		assertNotNull(postedBill);
		assertEquals(BillStatus.POSTED, postedBill.getStatus());
		
		postedBill.setReceiptNumber("ABV");
		assertThrows(IllegalArgumentException.class, () -> billService.saveBill(postedBill));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#saveBill(Bill)
	 */
	@Test
	public void save_Bill_shouldChangeStatusToPostedWhenPartialPaymentIsAdded() {
		Patient patient = patientService.getPatient(1);
		assertNotNull(patient);
		
		Bill templateBill = billService.getById(0);
		assertNotNull(templateBill);
		assertFalse(templateBill.getLineItems().isEmpty());
		
		// Create a new bill with one line item
		Bill newBill = new Bill();
		newBill.setCashier(providerService.getProvider(0));
		newBill.setPatient(patient);
		newBill.setCashPoint(cashPointService.getById(0));
		newBill.setReceiptNumber("TEST-" + UUID.randomUUID());
		newBill.setStatus(BillStatus.PENDING);
		
		BillLineItem existingItem = templateBill.getLineItems().get(0);
		StockItem stockItem = existingItem.getItem();
		
		BillLineItem lineItem = newBill.addLineItem(stockItem, BigDecimal.valueOf(150), "New price", 2);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		lineItem.setUuid(UUID.randomUUID().toString());
		
		// Save the bill - status should be PENDING
		Bill savedBill = billService.saveBill(newBill);
		Context.flushSession();
		
		assertNotNull(savedBill);
		assertNotNull(savedBill.getId());
		assertEquals(BillStatus.PENDING, savedBill.getStatus());
		assertEquals(1, savedBill.getLineItems().size());
		assertEquals(BigDecimal.valueOf(300), savedBill.getTotal());
		
		// Add partial payment (less than total)
		PaymentMode paymentMode = paymentModeService.getById(0);
		assertNotNull(paymentMode);
		
		BigDecimal partialPaymentAmount = BigDecimal.valueOf(100);
		Payment payment = new Payment();
		payment.setInstanceType(paymentMode);
		payment.setAmount(partialPaymentAmount);
		payment.setAmountTendered(partialPaymentAmount);
		payment.setCreator(Context.getAuthenticatedUser());
		payment.setDateCreated(new Date());
		payment.setUuid(UUID.randomUUID().toString());
		savedBill.addPayment(payment);
		
		// Save the bill again - status should change to POSTED
		Bill billWithPayment = billService.saveBill(savedBill);
		Context.flushSession();
		
		assertNotNull(billWithPayment);
		assertEquals(BillStatus.POSTED, billWithPayment.getStatus(), "Bill status should be POSTED when partial payment is added");
		assertEquals(partialPaymentAmount, billWithPayment.getTotalPayments(), "Total payments should equal partial payment amount");
		assertEquals(BigDecimal.valueOf(300), billWithPayment.getTotal(), "Bill total should remain unchanged");
		
		// Verify the status persists after retrieval
		Bill retrievedBill = billService.getById(billWithPayment.getId());
		assertNotNull(retrievedBill);
		assertEquals(BillStatus.POSTED, retrievedBill.getStatus(), "Retrieved bill should have POSTED status");
		assertEquals(partialPaymentAmount, retrievedBill.getTotalPayments(), "Retrieved bill should have correct payment amount");
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#saveBill(Bill)
	 */
	@Test
	public void save_Bill_shouldThrowExceptionWhenModifyingLineItemQuantityAfterPartialPayment() {
		Patient patient = patientService.getPatient(1);
		assertNotNull(patient);
		
		Bill templateBill = billService.getById(0);
		assertNotNull(templateBill);
		assertFalse(templateBill.getLineItems().isEmpty());
		
		// Create a new bill with one line item
		Bill newBill = new Bill();
		newBill.setCashier(providerService.getProvider(0));
		newBill.setPatient(patient);
		newBill.setCashPoint(cashPointService.getById(0));
		newBill.setReceiptNumber("TEST-" + UUID.randomUUID());
		newBill.setStatus(BillStatus.PENDING);
		
		BillLineItem existingItem = templateBill.getLineItems().get(0);
		StockItem stockItem = existingItem.getItem();
		
		BillLineItem lineItem = newBill.addLineItem(stockItem, BigDecimal.valueOf(150), "New price", 2);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		lineItem.setUuid(UUID.randomUUID().toString());
		
		// Save the bill - status should be PENDING
		Bill savedBill = billService.saveBill(newBill);
		Context.flushSession();
		
		assertNotNull(savedBill);
		assertEquals(BillStatus.PENDING, savedBill.getStatus());
		assertEquals(2, savedBill.getLineItems().get(0).getQuantity(), "Initial quantity should be 2");
		
		// Add partial payment (less than total) - status changes to POSTED
		PaymentMode paymentMode = paymentModeService.getById(0);
		assertNotNull(paymentMode);
		
		BigDecimal partialPaymentAmount = BigDecimal.valueOf(100);
		Payment payment = new Payment();
		payment.setInstanceType(paymentMode);
		payment.setAmount(partialPaymentAmount);
		payment.setAmountTendered(partialPaymentAmount);
		savedBill.addPayment(payment);
		
		Bill billWithPayment = billService.saveBill(savedBill);
		Context.flushSession();
		
		assertEquals(BillStatus.POSTED, billWithPayment.getStatus(), "Bill status should be POSTED after partial payment");
		
		// Try to modify line item quantity by removing and re-adding with different quantity
		// This should throw IllegalStateException because line items can only be modified when bill is PENDING
		BillLineItem firstItem = billWithPayment.getLineItems().get(0);
		
		// Attempt to remove line item to change quantity - should throw exception
		assertThrows(IllegalStateException.class, () -> {
			billWithPayment.removeLineItem(firstItem);
		}, "Should throw IllegalStateException when trying to remove line item from POSTED bill");
		
		// Attempt to add a new line item - should also throw exception
		assertThrows(IllegalStateException.class, () -> {
			billWithPayment.addLineItem(stockItem, BigDecimal.valueOf(100), "Another item", 1);
		}, "Should throw IllegalStateException when trying to add line item to POSTED bill");
	}
}
