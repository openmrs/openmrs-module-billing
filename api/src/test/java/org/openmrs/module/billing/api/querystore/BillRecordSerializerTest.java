/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.querystore;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillDiscount;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.api.model.DiscountStatus;
import org.openmrs.module.billing.api.model.DiscountType;
import org.openmrs.module.querystore.model.QueryDocument;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BillRecordSerializerTest {
	
	private static final String BILL_UUID = "bill-uuid-1";
	
	private static final String PATIENT_UUID = "patient-uuid-1";
	
	private final BillRecordSerializer serializer = new BillRecordSerializer();
	
	@Test
	public void serialize_shouldSetCoreFieldsFromBill() {
		Bill bill = postedBillWithLineItem("R-001", new BigDecimal("100"), 1);
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNotNull(doc);
		assertEquals(BillingQueryStoreConstants.RESOURCE_TYPE_BILL, doc.getResourceType());
		assertEquals(BILL_UUID, doc.getResourceUuid());
		assertEquals(PATIENT_UUID, doc.getPatientUuid());
		assertNotNull(doc.getDate(), "date must be set so the document is queryable by clinical date");
	}
	
	@Test
	public void serialize_shouldDeriveBalanceFromAmountAfterDiscount() {
		// total=100, approved discount=30, paid=50 → amount_after_discount=70, balance=20.
		// If balance were derived from gross total instead, it would report 50 — overstating
		// what's still owed and breaking dashboards built on the balance field.
		Bill bill = postedBillWithLineItem("R-002", new BigDecimal("100"), 1);
		addApprovedDiscount(bill, new BigDecimal("30"));
		addPayment(bill, new BigDecimal("50"));
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNotNull(doc);
		assertEquals(new BigDecimal("100"), doc.getMetadata().get(BillingQueryStoreConstants.FIELD_TOTAL));
		assertEquals(new BigDecimal("70"), doc.getMetadata().get(BillingQueryStoreConstants.FIELD_AMOUNT_AFTER_DISCOUNT));
		assertEquals(new BigDecimal("50"), doc.getMetadata().get(BillingQueryStoreConstants.FIELD_TOTAL_PAID));
		assertEquals(new BigDecimal("20"), doc.getMetadata().get(BillingQueryStoreConstants.FIELD_BALANCE));
	}
	
	@Test
	public void serialize_shouldClampBalanceWhenAmountAfterDiscountIsZero() {
		// Approved discount exceeds total. getAmountAfterDiscount() clamps to zero, so balance =
		// 0 - payments. A negative balance means the bill is over-credited; consumers can detect
		// drift by checking balance < 0.
		Bill bill = postedBillWithLineItem("R-003", new BigDecimal("100"), 1);
		addApprovedDiscount(bill, new BigDecimal("150"));
		addPayment(bill, new BigDecimal("0"));
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNotNull(doc);
		assertEquals(BigDecimal.ZERO, doc.getMetadata().get(BillingQueryStoreConstants.FIELD_AMOUNT_AFTER_DISCOUNT));
	}
	
	@Test
	public void serialize_shouldIncludeCashierCashPointAndVisitWhenPresent() {
		Bill bill = postedBillWithLineItem("R-004", new BigDecimal("50"), 1);
		Provider cashier = new Provider();
		cashier.setUuid("cashier-uuid");
		bill.setCashier(cashier);
		CashPoint cashPoint = new CashPoint();
		cashPoint.setUuid("cashpoint-uuid");
		cashPoint.setName("Main Counter");
		bill.setCashPoint(cashPoint);
		Visit visit = new Visit();
		visit.setUuid("visit-uuid");
		bill.setVisit(visit);
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNotNull(doc);
		assertEquals("cashier-uuid", doc.getMetadata().get(BillingQueryStoreConstants.FIELD_CASHIER_UUID));
		assertEquals("cashpoint-uuid", doc.getMetadata().get(BillingQueryStoreConstants.FIELD_CASH_POINT_UUID));
		assertEquals("Main Counter", doc.getMetadata().get(BillingQueryStoreConstants.FIELD_CASH_POINT_NAME));
		assertEquals("visit-uuid", doc.getMetadata().get(BillingQueryStoreConstants.FIELD_VISIT_UUID));
	}
	
	@Test
	public void serialize_shouldOmitOptionalReferencesWhenAbsent() {
		Bill bill = postedBillWithLineItem("R-005", new BigDecimal("50"), 1);
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNotNull(doc);
		assertFalse(doc.getMetadata().containsKey(BillingQueryStoreConstants.FIELD_CASHIER_UUID));
		assertFalse(doc.getMetadata().containsKey(BillingQueryStoreConstants.FIELD_CASH_POINT_UUID));
		assertFalse(doc.getMetadata().containsKey(BillingQueryStoreConstants.FIELD_CASH_POINT_NAME));
		assertFalse(doc.getMetadata().containsKey(BillingQueryStoreConstants.FIELD_VISIT_UUID));
	}
	
	@Test
	public void serialize_shouldFallBackToUuidInTextWhenReceiptNumberAbsent() {
		Bill bill = postedBillWithLineItem(null, new BigDecimal("50"), 1);
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNotNull(doc);
		// receipt-number metadata is null; the searchable text falls back to the bill UUID so
		// the document still has a stable label for indexing.
		assertNull(doc.getMetadata().get(BillingQueryStoreConstants.FIELD_RECEIPT_NUMBER));
		assertTrue(doc.getText().contains(BILL_UUID),
		    "text must include the bill UUID when receipt number is absent: " + doc.getText());
	}
	
	@Test
	public void serialize_shouldReturnNullWhenPatientAbsent() {
		// Without a patient, the serializer cannot produce a document keyed to a patient. Returning
		// null short-circuits indexing for this record (per the AbstractRecordSerializer contract:
		// empty text → null document).
		Bill bill = postedBillWithLineItem("R-006", new BigDecimal("50"), 1);
		bill.setPatient(null);
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNull(doc);
	}
	
	private Bill postedBillWithLineItem(String receiptNumber, BigDecimal price, int quantity) {
		Bill bill = new Bill();
		bill.setUuid(BILL_UUID);
		bill.setReceiptNumber(receiptNumber);
		Patient patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		bill.setPatient(patient);
		bill.setStatus(BillStatus.POSTED);
		bill.setDateCreated(new Date());
		bill.setLineItems(new ArrayList<>());
		BillLineItem lineItem = new BillLineItem();
		lineItem.setPrice(price);
		lineItem.setQuantity(quantity);
		lineItem.setVoided(false);
		bill.getLineItems().add(lineItem);
		return bill;
	}
	
	private void addApprovedDiscount(Bill bill, BigDecimal amount) {
		if (bill.getDiscounts() == null) {
			bill.setDiscounts(new HashSet<>());
		}
		// getDiscountAmount() derives from discountValue + discountType, not a stored column —
		// FIXED_AMOUNT means amount == value.
		BillDiscount discount = new BillDiscount();
		discount.setDiscountType(DiscountType.FIXED_AMOUNT);
		discount.setDiscountValue(amount);
		discount.setStatus(DiscountStatus.APPROVED);
		discount.setVoided(false);
		bill.getDiscounts().add(discount);
	}
	
	private void addPayment(Bill bill, BigDecimal amount) {
		if (bill.getPayments() == null) {
			bill.setPayments(new HashSet<>());
		}
		org.openmrs.module.billing.api.model.Payment payment = new org.openmrs.module.billing.api.model.Payment();
		payment.setAmount(amount);
		payment.setAmountTendered(amount);
		payment.setVoided(false);
		bill.getPayments().add(payment);
	}
}
