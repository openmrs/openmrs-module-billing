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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
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
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#save(Bill)
	 */
	@Test
	public void save_shouldAllowAddingLineItemsToPendingBill() {
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
		Bill savedBill = billService.save(pendingBill);
		assertNotNull(savedBill);
		assertTrue(savedBill.getLineItems().size() > 0);
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#save(Bill)
	 */
	@Test
	public void save_shouldThrowExceptionWhenAddingLineItemsToPostedBill() {
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
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#save(Bill)
	 */
	@Test
	public void save_shouldThrowExceptionWhenAddingLineItemsToPaidBill() {
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
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#save(Bill)
	 */
	@Test
	public void save_shouldAllowRemovingLineItemsFromPendingBill() {
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
		Bill savedBill = billService.save(pendingBill);
		assertNotNull(savedBill);
		assertTrue(savedBill.getLineItems().size() < originalSize);
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#save(Bill)
	 */
	@Test
	public void save_shouldThrowExceptionWhenRemovingLineItemsFromPostedBill() {
		// Get the POSTED bill from test data (bill_id=0)
		Bill postedBill = billService.getById(0);
		assertNotNull(postedBill);
		assertEquals(BillStatus.POSTED, postedBill.getStatus());
		
		BillLineItem itemToRemove = postedBill.getLineItems().get(0);
		
		// Should throw exception
		assertThrows(IllegalStateException.class, () -> postedBill.removeLineItem(itemToRemove));
	}

	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#save(Bill)
	 */
	@Test
	public void save_shouldNotMergeWhenExistingBillIsPosted() {
		// Get a StockItem from existing bill for line items
		Bill templateBill = billService.getById(0);
		assertNotNull(templateBill, "Template bill should exist");
		assertNotNull(templateBill.getLineItems(), "Template bill should have line items");
		assertFalse(templateBill.getLineItems().isEmpty(), "Template bill should have at least one line item");
		StockItem stockItem = templateBill.getLineItems().get(0).getItem();
		
		Patient patient = patientService.getPatient(1);
		assertNotNull(patient, "Test patient should exist");
		
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
		assertNotNull(savedFirstBill, "First bill should be saved");
		assertNotNull(savedFirstBill.getId(), "First bill should have an ID");
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
		assertNotNull(savedSecondBill, "Second bill should be saved");
		assertNotNull(savedSecondBill.getId(), "Second bill should have an ID");
		
		// Should NOT merge - POSTED bills are not merged (searchBill only finds PENDING bills)
		assertNotEquals(savedFirstBill.getId(), savedSecondBill.getId(), "POSTED bills should not merge with new bills");
		
		List<Bill> patientBills = billService.getBillsByPatient(patient, null);
		assertNotNull(patientBills, "getBillsByPatient should return a non-null list");
		assertEquals(3, patientBills.size(), "Patient should have 3 bills total (1 from test data + 2 newly created)");
	}
}
