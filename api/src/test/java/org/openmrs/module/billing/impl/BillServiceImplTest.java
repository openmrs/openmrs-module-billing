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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.ICashPointService;
import org.openmrs.module.billing.api.IPaymentModeService;
import org.openmrs.module.billing.api.model.Payment;
import org.openmrs.module.billing.api.model.PaymentMode;

import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.search.BillSearch;

import org.openmrs.module.stockmanagement.api.model.StockItem;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

public class BillServiceImplTest extends BaseModuleContextSensitiveTest {
	
	private BillService billService;
	
	private ProviderService providerService;
	
	private PatientService patientService;
	
	private ICashPointService cashPointService;
	
	private IPaymentModeService paymentModeService;
	
	@BeforeEach
	public void setup() {
		billService = Context.getService(BillService.class);
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
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillsByPatientUuid(String,
	 *      PagingInfo)
	 */
	@Test
	public void getBillsByPatientUuid_shouldReturnBillsForPatient() {
		List<Bill> bills = billService.getBillsByPatientUuid("5631b434-78aa-102b-91a0-001e378eb67e", null);
		assertNotNull(bills);
		assertFalse(bills.isEmpty());
		assertEquals(1, bills.size());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillsByPatientUuid(String,
	 *      PagingInfo)
	 */
	@Test
	public void getBillsByPatientId_shouldReturnEmptyListWhenPatientHasNoBills() {
		List<Bill> bills = billService.getBillsByPatientUuid("abc", null);
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
		
		Bill templateBill = billService.getBill(0);
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
		
		Bill retrievedBill = billService.getBill(savedBill.getId());
		assertNotNull(retrievedBill);
		assertEquals(patient.getId(), retrievedBill.getPatient().getId());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#saveBill(Bill)
	 */
	@Test
	public void save_Bill_shouldUpdateExistingBillWithUpdatedBillItem() {
		Bill pendingBill = billService.getBill(2);
		assertNotNull(pendingBill);
		assertEquals(BillStatus.PENDING, pendingBill.getStatus());
		assertFalse(pendingBill.getLineItems().isEmpty());
		
		BillLineItem firstItem = pendingBill.getLineItems().get(0);
		BigDecimal updatedPrice = firstItem.getPrice().add(BigDecimal.TEN);
		firstItem.setPrice(updatedPrice);
		
		billService.saveBill(pendingBill);
		Context.flushSession();
		Context.clearSession();
		
		Bill updatedBill = billService.getBill(2);
		
		assertEquals(pendingBill, updatedBill);
		assertEquals(updatedPrice, updatedBill.getLineItems().get(0).getPrice());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBill(Integer)
	 */
	@Test
	public void getById_shouldReturnBillWithSpecifiedId() {
		Bill bill = billService.getBill(1);
		assertNotNull(bill);
		assertEquals(1, bill.getId());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBill(Integer)
	 */
	@Test
	public void getById_shouldRemoveNullLineItems() {
		Bill bill = billService.getBill(1);
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
		Bill pendingBill = billService.getBill(2);
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
		assertFalse(savedBill.getLineItems().isEmpty());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#saveBill(Bill)
	 */
	@Test
	public void save_Bill_shouldThrowExceptionWhenAddingLineItemsToPaidBill() {
		// Get the PAID bill from test data (bill_id=1)
		Bill paidBill = billService.getBill(1);
		assertNotNull(paidBill);
		assertEquals(BillStatus.PAID, paidBill.getStatus());
		
		// Try to add a new line item
		BillLineItem newLineItem = new BillLineItem();
		newLineItem.setPrice(BigDecimal.valueOf(25.50));
		newLineItem.setQuantity(2);
		paidBill.addLineItem(newLineItem);
		// Should throw exception
		
		assertThrows(IllegalArgumentException.class, () -> billService.saveBill(paidBill));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#saveBill(Bill)
	 */
	@Test
	public void save_Bill_shouldAllowRemovingLineItemsFromPendingBill() {
		// Get the PENDING bill from test data (bill_id=2)
		Bill pendingBill = billService.getBill(2);
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
	public void save_Bill_shouldThrowExceptionWhenRemovingLineItemsFromPaidBill() {
		// Get the POSTED bill from test data (bill_id=1)
		Bill postedBill = billService.getBill(1);
		assertNotNull(postedBill);
		assertEquals(BillStatus.PAID, postedBill.getStatus());
		
		BillLineItem itemToRemove = postedBill.getLineItems().get(0);
		postedBill.removeLineItem(itemToRemove);
		
		// Should throw exception
		assertThrows(IllegalArgumentException.class, () -> billService.saveBill(postedBill));
	}
	
	@Test
	public void save_Bill_shouldNotThrowExceptionForPendingBill() {
		Bill pendingBill = billService.getBill(2);
		assertNotNull(pendingBill);
		assertEquals(BillStatus.PENDING, pendingBill.getStatus());
		pendingBill.setReceiptNumber("ABV");
		assertDoesNotThrow(() -> billService.saveBill(pendingBill));
	}
	
	@Test
	public void save_Bill_shouldThrowIllegalStateExceptionForPostedBill() {
		Bill postedBill = billService.getBill(0);
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

	/** 
   * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillByUuid(String)
	 */
	@Test
	public void getBillByUuid_shouldReturnBillWithSpecifiedUuid() {
		Bill bill = billService.getBill(0);
		assertNotNull(bill);
		String uuid = bill.getUuid();
		
		Bill foundBill = billService.getBillByUuid(uuid);
		assertNotNull(foundBill);
		assertEquals(uuid, foundBill.getUuid());
		assertEquals(0, foundBill.getId());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillByUuid(String)
	 */
	@Test
	public void getBillByUuid_shouldReturnNullIfUuidNotFound() {
		Bill bill = billService.getBillByUuid("nonexistent-uuid");
		assertNull(bill);
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBills(BillSearch, PagingInfo)
	 */
	@Test
	public void getBills_shouldReturnAllBillsWhenSearchIsEmpty() {
		BillSearch billSearch = new BillSearch();
		List<Bill> bills = billService.getBills(billSearch, null);
		
		assertNotNull(bills);
		assertFalse(bills.isEmpty());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBills(BillSearch, PagingInfo)
	 */
	@Test
	public void getBills_shouldFilterByPatientUuid() {
		Patient patient = patientService.getPatient(0);
		assertNotNull(patient);
		
		BillSearch billSearch = new BillSearch();
		billSearch.setPatientUuid(patient.getUuid());
		
		List<Bill> bills = billService.getBills(billSearch, null);
		assertNotNull(bills);
		assertFalse(bills.isEmpty());
		
		for (Bill bill : bills) {
			assertEquals(patient.getUuid(), bill.getPatient().getUuid());
		}
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBills(BillSearch, PagingInfo)
	 */
	@Test
	public void getBills_shouldReturnEmptyListWhenSearchReturnsNoResults() {
		BillSearch billSearch = new BillSearch();
		billSearch.setPatientUuid("nonexistent-uuid");
		
		List<Bill> bills = billService.getBills(billSearch, null);
		assertNotNull(bills);
		assertTrue(bills.isEmpty());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBills(BillSearch, PagingInfo)
	 */
	@Test
	public void getBills_shouldApplyPagingCorrectly() {
		BillSearch billSearch = new BillSearch();
		PagingInfo pagingInfo = new PagingInfo(1, 2);
		
		List<Bill> bills = billService.getBills(billSearch, pagingInfo);
		assertNotNull(bills);
		assertTrue(bills.size() <= 2);
		assertNotNull(pagingInfo.getTotalRecordCount());
	}
}
