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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.UnchangeableObjectException;
import org.openmrs.api.ValidationException;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.CashPointService;
import org.openmrs.module.billing.api.PaymentModeService;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.Payment;
import org.openmrs.module.billing.api.model.PaymentMode;
import org.openmrs.module.billing.api.search.BillSearch;
import org.openmrs.module.stockmanagement.api.model.StockItem;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BillServiceImplTest extends BaseModuleContextSensitiveTest {

	private BillService billService;

	private ProviderService providerService;

	private PatientService patientService;

	private PaymentModeService paymentModeService;

	private CashPointService cashPointService;

	private VisitService visitService;

	@BeforeEach
	public void setup() {
		billService = Context.getService(BillService.class);
		providerService = Context.getProviderService();
		patientService = Context.getPatientService();
		paymentModeService = Context.getService(PaymentModeService.class);
		cashPointService = Context.getService(CashPointService.class);
		visitService = Context.getVisitService();

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
	public void saveBill_shouldThrowNullPointerExceptionIfBillIsNull() {
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
	public void saveBill_shouldCreateNewBillWithNewItem() {
		Patient patient = patientService.getPatient(1);
		assertNotNull(patient);

		Bill templateBill = billService.getBill(0);
		assertNotNull(templateBill);
		assertFalse(templateBill.getLineItems().isEmpty());

		Bill newBill = new Bill();
		newBill.setCashier(providerService.getProvider(0));
		newBill.setPatient(patient);
		newBill.setCashPoint(cashPointService.getCashPoint(0));
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
	public void saveBill_shouldUpdateExistingBillWithUpdatedBillItem() {
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
	public void saveBill_shouldAllowAddingLineItemsToPendingBill() {
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
	public void saveBill_shouldThrowExceptionWhenAddingLineItemsToPaidBill() {
		// Get the PAID bill from test data (bill_id=1)
		Bill paidBill = billService.getBill(1);
		assertNotNull(paidBill);
		assertEquals(BillStatus.PAID, paidBill.getStatus());

		// Try to add a new line item
		BillLineItem newLineItem = new BillLineItem();
		newLineItem.setPrice(BigDecimal.valueOf(25.50));
		newLineItem.setQuantity(2);
		newLineItem.setPaymentStatus(BillStatus.PENDING);
		paidBill.addLineItem(newLineItem);

		// Should throw exception when saving (BillValidator catches line item
		// additions)
		assertThrows(ValidationException.class, () -> billService.saveBill(paidBill));
	}

	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#saveBill(Bill)
	 */
	@Test
	public void saveBill_shouldAllowRemovingLineItemsFromPendingBill() {
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
	public void saveBill_shouldThrowExceptionWhenRemovingLineItemsFromPaidBill() {
		// Get the POSTED bill from test data (bill_id=1)
		Bill postedBill = billService.getBill(1);
		assertNotNull(postedBill);
		assertEquals(BillStatus.PAID, postedBill.getStatus());

		BillLineItem itemToRemove = postedBill.getLineItems().get(0);
		postedBill.removeLineItem(itemToRemove);

		// Should throw exception when saving (BillValidator catches line item removals)
		assertThrows(ValidationException.class, () -> billService.saveBill(postedBill));
	}

	@Test
	public void saveBill_shouldNotThrowExceptionForPendingBill() {
		Bill pendingBill = billService.getBill(2);
		assertNotNull(pendingBill);
		assertEquals(BillStatus.PENDING, pendingBill.getStatus());

		pendingBill.setReceiptNumber("ABV");

		billService.saveBill(pendingBill);
		assertDoesNotThrow(Context::flushSession);
	}

	@Test
	public void saveBill_shouldNotAllowChangesForPostedBill() {
		Bill postedBill = billService.getBill(0);
		assertNotNull(postedBill);
		assertEquals(BillStatus.POSTED, postedBill.getStatus());

		postedBill.setCashier(providerService.getProvider(1));
		billService.saveBill(postedBill);

		assertThrows(UnchangeableObjectException.class, Context::flushSession);
	}

	@Test
	public void saveBill_shouldAllowPaymentsForPostedBill() {
		Bill postedBill = billService.getBill(0);
		assertNotNull(postedBill);
		assertEquals(BillStatus.POSTED, postedBill.getStatus());

		PaymentMode paymentMode = paymentModeService.getPaymentMode(0);

		Payment payment = Payment.builder().amount(BigDecimal.valueOf(10.0)).amountTendered(BigDecimal.valueOf(10.0))
		        .build();
		payment.setInstanceType(paymentMode);

		postedBill.addPayment(payment);
		billService.saveBill(postedBill);

		assertDoesNotThrow(Context::flushSession);
	}

	@Test
	public void saveBill_shouldNotAllowModifyingLineItemPropertiesOnPaidBill() {
		Bill paidBill = billService.getBill(1);
		assertNotNull(paidBill);
		assertEquals(BillStatus.PAID, paidBill.getStatus());
		assertFalse(paidBill.getLineItems().isEmpty());

		// Try to modify the price of an existing line item
		BillLineItem lineItem = paidBill.getLineItems().get(0);
		lineItem.setPrice(lineItem.getPrice().add(BigDecimal.TEN));

		billService.saveBill(paidBill);

		// Should throw exception when flushing (ImmutableBillLineItemInterceptor
		// catches modifications)
		assertThrows(UnchangeableObjectException.class, Context::flushSession);
	}

	@Test
	public void saveBill_shouldAllowModifyingLineItemPropertiesOnPendingBill() {
		Bill pendingBill = billService.getBill(2);
		assertNotNull(pendingBill);
		assertEquals(BillStatus.PENDING, pendingBill.getStatus());
		assertFalse(pendingBill.getLineItems().isEmpty());

		// Modify the price of an existing line item
		BillLineItem lineItem = pendingBill.getLineItems().get(0);
		BigDecimal newPrice = lineItem.getPrice().add(BigDecimal.TEN);
		lineItem.setPrice(newPrice);

		billService.saveBill(pendingBill);

		// Should not throw exception
		assertDoesNotThrow(Context::flushSession);
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

	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#saveBill(Bill)
	 */
	@Test
	public void saveBill_shouldGenerateReceiptNumberWhenNotProvided() {
		Context.getAdministrationService().setGlobalProperty("billing.systemReceiptNumberGenerator",
		    "org.openmrs.module.billing.api.SequentialReceiptNumberGenerator");

		Patient patient = patientService.getPatient(1);
		assertNotNull(patient);

		// Create a new bill WITHOUT a receipt number
		Bill newBill = new Bill();
		newBill.setCashier(providerService.getProvider(0));
		newBill.setPatient(patient);
		newBill.setCashPoint(cashPointService.getCashPoint(0));
		newBill.setStatus(BillStatus.PENDING);

		Bill templateBill = billService.getBill(0);
		BillLineItem existingItem = templateBill.getLineItems().get(0);
		BillLineItem lineItem = newBill.addLineItem(existingItem.getItem(), BigDecimal.valueOf(100), "Test price", 1);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		lineItem.setUuid(UUID.randomUUID().toString());

		Bill savedBill = billService.saveBill(newBill);

		assertNotNull(savedBill);
		assertNotNull(savedBill.getReceiptNumber());
		assertFalse(savedBill.getReceiptNumber().isEmpty());
	}

	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#saveBill(Bill)
	 */
	@Test
	public void saveBill_shouldSaveBillWithVisit() {
		Patient patient = patientService.getPatient(0);
		assertNotNull(patient);

		Visit visit = visitService.getVisit(1);
		assertNotNull(visit);
		assertEquals(patient.getId(), visit.getPatient().getId());

		Bill newBill = new Bill();
		newBill.setCashier(providerService.getProvider(0));
		newBill.setPatient(patient);
		newBill.setCashPoint(cashPointService.getCashPoint(0));
		newBill.setVisit(visit);
		newBill.setReceiptNumber("TEST-VISIT-" + UUID.randomUUID());
		newBill.setStatus(BillStatus.PENDING);

		Bill templateBill = billService.getBill(0);
		BillLineItem existingItem = templateBill.getLineItems().get(0);
		BillLineItem lineItem = newBill.addLineItem(existingItem.getItem(), BigDecimal.valueOf(100), "Test price", 1);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		lineItem.setUuid(UUID.randomUUID().toString());

		Bill savedBill = billService.saveBill(newBill);
		Context.flushSession();

		assertNotNull(savedBill);
		assertNotNull(savedBill.getId());
		assertNotNull(savedBill.getVisit());
		assertEquals(visit.getId(), savedBill.getVisit().getId());

		// Verify persistence by retrieving the bill
		Bill retrievedBill = billService.getBill(savedBill.getId());
		assertNotNull(retrievedBill);
		assertNotNull(retrievedBill.getVisit());
		assertEquals(visit.getId(), retrievedBill.getVisit().getId());
	}

	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#saveBill(Bill)
	 */
	@Test
	public void saveBill_shouldSaveBillWithoutVisit() {
		Patient patient = patientService.getPatient(1);
		assertNotNull(patient);

		Bill newBill = new Bill();
		newBill.setCashier(providerService.getProvider(0));
		newBill.setPatient(patient);
		newBill.setCashPoint(cashPointService.getCashPoint(0));
		newBill.setVisit(null); // Explicitly set to null
		newBill.setReceiptNumber("TEST-NO-VISIT-" + UUID.randomUUID());
		newBill.setStatus(BillStatus.PENDING);

		Bill templateBill = billService.getBill(0);
		BillLineItem existingItem = templateBill.getLineItems().get(0);
		BillLineItem lineItem = newBill.addLineItem(existingItem.getItem(), BigDecimal.valueOf(100), "Test price", 1);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		lineItem.setUuid(UUID.randomUUID().toString());

		Bill savedBill = billService.saveBill(newBill);
		Context.flushSession();

		assertNotNull(savedBill);
		assertNotNull(savedBill.getId());
		assertNull(savedBill.getVisit());

		// Verify persistence
		Bill retrievedBill = billService.getBill(savedBill.getId());
		assertNotNull(retrievedBill);
		assertNull(retrievedBill.getVisit());
	}

	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#saveBill(Bill)
	 */
	@Test
	public void saveBill_shouldUpdateBillVisit() {
		Bill existingBill = billService.getBill(2); // PENDING bill
		assertNotNull(existingBill);
		assertEquals(BillStatus.PENDING, existingBill.getStatus());

		Visit visit = visitService.getVisit(3);
		assertNotNull(visit);

		existingBill.setVisit(visit);
		billService.saveBill(existingBill);
		Context.flushSession();
		Context.clearSession();

		Bill updatedBill = billService.getBill(2);
		assertNotNull(updatedBill);
		assertNotNull(updatedBill.getVisit());
		assertEquals(visit.getId(), updatedBill.getVisit().getId());
	}

	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBill(Integer)
	 */
	@Test
	public void getBill_shouldLoadVisitCorrectly() {
		// First save a bill with a visit
		Patient patient = patientService.getPatient(1);
		Visit visit = visitService.getVisit(2);

		Bill newBill = new Bill();
		newBill.setCashier(providerService.getProvider(0));
		newBill.setPatient(patient);
		newBill.setCashPoint(cashPointService.getCashPoint(0));
		newBill.setVisit(visit);
		newBill.setReceiptNumber("TEST-LOAD-" + UUID.randomUUID());
		newBill.setStatus(BillStatus.PENDING);

		Bill templateBill = billService.getBill(0);
		BillLineItem lineItem = newBill.addLineItem(templateBill.getLineItems().get(0).getItem(), BigDecimal.valueOf(100),
		    "Test", 1);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		lineItem.setUuid(UUID.randomUUID().toString());

		Bill savedBill = billService.saveBill(newBill);
		Integer billId = savedBill.getId();
		Context.flushSession();
		Context.clearSession();

		// Now retrieve and verify
		Bill loadedBill = billService.getBill(billId);
		assertNotNull(loadedBill);
		assertNotNull(loadedBill.getVisit());
		assertEquals(visit.getId(), loadedBill.getVisit().getId());
		assertEquals(patient.getId(), loadedBill.getVisit().getPatient().getId());
	}

	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#saveBill(Bill)
	 */
	@Test
	public void saveBill_shouldAllowUpdatingVisitOnPendingBill() {
		Bill pendingBill = billService.getBill(2); // PENDING bill
		assertNotNull(pendingBill);
		assertEquals(BillStatus.PENDING, pendingBill.getStatus());

		Visit visit = visitService.getVisit(3);
		assertNotNull(visit);

		// Should be able to set visit on PENDING bill
		pendingBill.setVisit(visit);
		billService.saveBill(pendingBill);

		// Should not throw exception
		assertDoesNotThrow(Context::flushSession);

		Context.clearSession();
		Bill updatedBill = billService.getBill(2);
		assertNotNull(updatedBill.getVisit());
		assertEquals(visit.getId(), updatedBill.getVisit().getId());
	}

	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#saveBill(Bill)
	 */
	@Test
	public void saveBill_shouldNotAllowUpdatingVisitOnPostedBill() {
		Bill postedBill = billService.getBill(0); // POSTED bill
		assertNotNull(postedBill);
		assertEquals(BillStatus.POSTED, postedBill.getStatus());

		Visit visit = visitService.getVisit(1);
		assertNotNull(visit);

		// Try to change visit on POSTED bill
		postedBill.setVisit(visit);
		billService.saveBill(postedBill);

		// Should throw UnchangeableObjectException when flushing
		assertThrows(UnchangeableObjectException.class, Context::flushSession);
	}

	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#saveBill(Bill)
	 */
	@Test
	public void saveBill_shouldNotAllowUpdatingVisitOnPaidBill() {
		Bill paidBill = billService.getBill(1); // PAID bill
		assertNotNull(paidBill);
		assertEquals(BillStatus.PAID, paidBill.getStatus());

		Visit visit = visitService.getVisit(2);
		assertNotNull(visit);

		// Try to change visit on PAID bill
		paidBill.setVisit(visit);
		billService.saveBill(paidBill);

		// Should throw UnchangeableObjectException when flushing
		assertThrows(UnchangeableObjectException.class, Context::flushSession);
	}
}
