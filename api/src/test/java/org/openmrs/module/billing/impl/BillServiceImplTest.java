/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitType;
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
	
	@BeforeEach
	public void setup() {
		billService = Context.getService(BillService.class);
		providerService = Context.getProviderService();
		patientService = Context.getPatientService();
		paymentModeService = Context.getService(PaymentModeService.class);
		cashPointService = Context.getService(CashPointService.class);
		
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
		payment.setCashier(providerService.getProvider(0));
		
		postedBill.addPayment(payment);
		billService.saveBill(postedBill);
		
		assertDoesNotThrow(Context::flushSession);
	}
	
	@Test
	public void saveBill_shouldThrowValidationExceptionWhenPaymentHasNoCashier() {
		Bill postedBill = billService.getBill(0);
		assertNotNull(postedBill);
		assertEquals(BillStatus.POSTED, postedBill.getStatus());
		
		PaymentMode paymentMode = paymentModeService.getPaymentMode(0);
		
		Payment payment = Payment.builder().amount(BigDecimal.valueOf(10.0)).amountTendered(BigDecimal.valueOf(10.0))
		        .build();
		payment.setInstanceType(paymentMode);
		// cashier intentionally NOT set — BillValidator must reject this
		
		postedBill.addPayment(payment);
		
		assertThrows(ValidationException.class, () -> billService.saveBill(postedBill));
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
	public void getBills_shouldFilterByVisitUuid() {
		VisitService visitService = Context.getVisitService();
		Patient patient = patientService.getPatient(1);
		
		org.openmrs.VisitType visitType;
		if (visitService.getAllVisitTypes().isEmpty()) {
			visitType = visitService.saveVisitType(new org.openmrs.VisitType("Test", "Test visit type"));
		} else {
			visitType = visitService.getAllVisitTypes().get(0);
		}
		
		Visit visitA = new Visit();
		visitA.setPatient(patient);
		visitA.setVisitType(visitType);
		visitA.setStartDatetime(new java.util.Date());
		visitA = visitService.saveVisit(visitA);
		
		Visit visitB = new Visit();
		visitB.setPatient(patient);
		visitB.setVisitType(visitType);
		visitB.setStartDatetime(new java.util.Date());
		visitB = visitService.saveVisit(visitB);
		
		Bill templateBill = billService.getBill(0);
		assertNotNull(templateBill);
		assertFalse(templateBill.getLineItems().isEmpty());
		BillLineItem existingItem = templateBill.getLineItems().get(0);
		StockItem stockItem = existingItem.getItem();
		
		Bill billA = new Bill();
		billA.setCashier(providerService.getProvider(0));
		billA.setPatient(patient);
		billA.setCashPoint(cashPointService.getCashPoint(0));
		billA.setReceiptNumber("TEST-VISIT-A-" + UUID.randomUUID());
		billA.setStatus(BillStatus.PENDING);
		billA.setVisit(visitA);
		BillLineItem lineItemA = billA.addLineItem(stockItem, BigDecimal.valueOf(100), "Test price", 1);
		lineItemA.setPaymentStatus(BillStatus.PENDING);
		lineItemA.setUuid(UUID.randomUUID().toString());
		billService.saveBill(billA);
		
		Bill billB = new Bill();
		billB.setCashier(providerService.getProvider(0));
		billB.setPatient(patient);
		billB.setCashPoint(cashPointService.getCashPoint(0));
		billB.setReceiptNumber("TEST-VISIT-B-" + UUID.randomUUID());
		billB.setStatus(BillStatus.PENDING);
		billB.setVisit(visitB);
		BillLineItem lineItemB = billB.addLineItem(stockItem, BigDecimal.valueOf(100), "Test price", 1);
		lineItemB.setPaymentStatus(BillStatus.PENDING);
		lineItemB.setUuid(UUID.randomUUID().toString());
		billService.saveBill(billB);
		
		Context.flushSession();
		
		BillSearch search = new BillSearch();
		search.setVisitUuid(visitA.getUuid());
		java.util.List<Bill> results = billService.getBills(search, new PagingInfo());
		
		assertEquals(1, results.size());
		assertEquals(visitA.getUuid(), results.get(0).getVisit().getUuid());
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
	public void saveBill_shouldPersistVisitAssociation() {
		Patient patient = patientService.getPatient(1);
		assertNotNull(patient);
		
		// Obtain a VisitType from the test dataset; create one if none exist
		VisitService visitService = Context.getVisitService();
		List<VisitType> visitTypes = visitService.getAllVisitTypes();
		VisitType visitType;
		if (visitTypes.isEmpty()) {
			visitType = visitService.saveVisitType(new VisitType("Test", "Test visit type"));
		} else {
			visitType = visitTypes.get(0);
		}
		
		Visit visit = new Visit();
		visit.setPatient(patient);
		visit.setVisitType(visitType);
		visit.setStartDatetime(new java.util.Date());
		Visit savedVisit = visitService.saveVisit(visit);
		assertNotNull(savedVisit);
		assertNotNull(savedVisit.getUuid());
		
		// Build a bill the same way saveBill_shouldCreateNewBillWithNewItem does
		Bill templateBill = billService.getBill(0);
		assertNotNull(templateBill);
		assertFalse(templateBill.getLineItems().isEmpty());
		
		BillLineItem existingItem = templateBill.getLineItems().get(0);
		StockItem stockItem = existingItem.getItem();
		
		Bill newBill = new Bill();
		newBill.setCashier(providerService.getProvider(0));
		newBill.setPatient(patient);
		newBill.setCashPoint(cashPointService.getCashPoint(0));
		newBill.setReceiptNumber("TEST-VISIT-" + UUID.randomUUID());
		newBill.setStatus(BillStatus.PENDING);
		newBill.setVisit(savedVisit);
		
		BillLineItem lineItem = newBill.addLineItem(stockItem, BigDecimal.valueOf(100), "Test price", 1);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		lineItem.setUuid(UUID.randomUUID().toString());
		
		Bill saved = billService.saveBill(newBill);
		assertNotNull(saved);
		assertNotNull(saved.getId());
		
		Context.flushSession();
		Context.clearSession();
		
		Bill reloaded = billService.getBill(saved.getId());
		assertNotNull(reloaded);
		assertNotNull(reloaded.getVisit());
		assertEquals(savedVisit.getUuid(), reloaded.getVisit().getUuid());
	}
}
