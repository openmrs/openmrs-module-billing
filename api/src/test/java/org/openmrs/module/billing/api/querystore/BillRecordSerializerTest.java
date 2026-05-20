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
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillDiscount;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.api.model.DiscountStatus;
import org.openmrs.module.billing.api.model.DiscountType;
import org.openmrs.module.billing.api.model.Payment;
import org.openmrs.module.billing.api.model.PaymentMode;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.stockmanagement.api.model.StockItem;

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
	public void serialize_shouldIncludeBillableServiceNameInTextAndLineItemNamesMetadata() {
		// "Find every bill that includes service X" is the core query this slice enables; if the
		// name does not make it into either the text blob or the structured list, that query has
		// no signal to match against.
		Bill bill = postedBillWithLineItem("R-100", new BigDecimal("50"), 1);
		setBillableService(bill.getLineItems().get(0), "Consultation");
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNotNull(doc);
		assertEquals(java.util.Collections.singletonList("Consultation"),
		    doc.getMetadata().get(BillingQueryStoreConstants.FIELD_LINE_ITEM_NAMES));
		assertTrue(doc.getText().contains("Items: Consultation."),
		    "text must surface the line item name for full-text search: " + doc.getText());
	}
	
	@Test
	public void serialize_shouldFallBackToStockItemCommonNameWhenBillableServiceAbsent() {
		Bill bill = postedBillWithLineItem("R-101", new BigDecimal("50"), 1);
		setStockItem(bill.getLineItems().get(0), "Paracetamol 500mg");
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNotNull(doc);
		assertEquals(java.util.Collections.singletonList("Paracetamol 500mg"),
		    doc.getMetadata().get(BillingQueryStoreConstants.FIELD_LINE_ITEM_NAMES));
	}
	
	@Test
	public void serialize_shouldPreferBillableServiceNameOverStockItemCommonName() {
		// Both populated → service wins. A line item with both fields set is rare but possible;
		// pinning the precedence here prevents future refactors from silently flipping it.
		Bill bill = postedBillWithLineItem("R-102", new BigDecimal("50"), 1);
		setBillableService(bill.getLineItems().get(0), "Consultation");
		setStockItem(bill.getLineItems().get(0), "Paracetamol 500mg");
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNotNull(doc);
		assertEquals(java.util.Collections.singletonList("Consultation"),
		    doc.getMetadata().get(BillingQueryStoreConstants.FIELD_LINE_ITEM_NAMES));
	}
	
	@Test
	public void serialize_shouldSkipVoidedLineItemsFromLineItemNames() {
		// Voided line items are excluded from total/balance computations, so they must also be
		// excluded from the indexed names — otherwise a voided item leaves a phantom hit in the
		// index that contradicts the bill's effective state.
		Bill bill = postedBillWithLineItem("R-103", new BigDecimal("50"), 1);
		setBillableService(bill.getLineItems().get(0), "ActiveService");
		BillLineItem voided = newLineItem(new BigDecimal("10"), 1);
		voided.setVoided(true);
		setBillableService(voided, "VoidedService");
		bill.getLineItems().add(voided);
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNotNull(doc);
		assertEquals(java.util.Collections.singletonList("ActiveService"),
		    doc.getMetadata().get(BillingQueryStoreConstants.FIELD_LINE_ITEM_NAMES));
		assertFalse(doc.getText().contains("VoidedService"),
		    "voided line item names must not appear in the indexed text: " + doc.getText());
	}
	
	@Test
	public void serialize_shouldOmitLineItemNamesMetadataWhenAllLineItemsVoided() {
		Bill bill = postedBillWithLineItem("R-104", new BigDecimal("50"), 1);
		bill.getLineItems().get(0).setVoided(true);
		setBillableService(bill.getLineItems().get(0), "VoidedOnly");
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNotNull(doc);
		assertFalse(doc.getMetadata().containsKey(BillingQueryStoreConstants.FIELD_LINE_ITEM_NAMES),
		    "metadata key must be absent (not empty list) when no names are eligible");
		assertFalse(doc.getText().contains("Items:"));
	}
	
	@Test
	public void serialize_shouldJoinMultipleLineItemNamesInOrder() {
		Bill bill = postedBillWithLineItem("R-105", new BigDecimal("10"), 1);
		setBillableService(bill.getLineItems().get(0), "First");
		BillLineItem second = newLineItem(new BigDecimal("20"), 1);
		setBillableService(second, "Second");
		bill.getLineItems().add(second);
		BillLineItem third = newLineItem(new BigDecimal("30"), 1);
		setStockItem(third, "Third");
		bill.getLineItems().add(third);
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNotNull(doc);
		List<String> expected = Arrays.asList("First", "Second", "Third");
		assertEquals(expected, doc.getMetadata().get(BillingQueryStoreConstants.FIELD_LINE_ITEM_NAMES));
		assertTrue(doc.getText().contains("Items: First, Second, Third."),
		    "text must preserve insertion order and join with ', ': " + doc.getText());
	}
	
	@Test
	public void serialize_shouldSkipLineItemWithNeitherServiceNorStockItem() {
		// Rounding rows (e.g., RoundingUtil.findRoundingLineItem) have neither field set — they
		// should not pollute the search text with a blank/null entry.
		Bill bill = postedBillWithLineItem("R-106", new BigDecimal("50"), 1);
		setBillableService(bill.getLineItems().get(0), "RealItem");
		BillLineItem bareRounding = newLineItem(new BigDecimal("0.01"), 1);
		bill.getLineItems().add(bareRounding);
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNotNull(doc);
		assertEquals(java.util.Collections.singletonList("RealItem"),
		    doc.getMetadata().get(BillingQueryStoreConstants.FIELD_LINE_ITEM_NAMES));
	}
	
	@Test
	public void serialize_shouldHandleNullLineItemsCollection() {
		// Bills loaded through certain paths can have an unset lineItems collection; collectLineItemNames
		// must not NPE in that case. The indexing advice swallows RuntimeException per-entity, so an NPE
		// here would silently drop the bill from the index with only a warn-level log.
		Bill bill = postedBillWithLineItem("R-107", new BigDecimal("50"), 1);
		bill.setLineItems(null);
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNotNull(doc);
		assertFalse(doc.getMetadata().containsKey(BillingQueryStoreConstants.FIELD_LINE_ITEM_NAMES));
	}
	
	@Test
	public void serialize_shouldEmitDistinctSortedPaymentModes() {
		// "Settlements by tender type" is an ops/finance query. The list must be distinct (two
		// Mobile Money payments don't double-count) and exact-match-queryable (List<String>, not
		// comma-joined). Sorted because the underlying HashSet<Payment> has no stable iteration
		// order — sorting gives consumers bytewise-identical docs across reindexes.
		Bill bill = postedBillWithLineItem("R-200", new BigDecimal("100"), 1);
		addPayment(bill, new BigDecimal("30"), paymentMode("Mobile Money"));
		addPayment(bill, new BigDecimal("40"), paymentMode("Cash"));
		addPayment(bill, new BigDecimal("30"), paymentMode("Mobile Money"));
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNotNull(doc);
		assertEquals(Arrays.asList("Cash", "Mobile Money"),
		    doc.getMetadata().get(BillingQueryStoreConstants.FIELD_PAYMENT_MODES));
	}
	
	@Test
	public void serialize_shouldSkipPaymentsWithMissingMode() {
		// A payment row can lack a useful mode (legacy data, half-constructed, blank name). Without
		// the guards the serializer would NPE or emit a blank/whitespace entry, polluting the text
		// blob and the metadata list — a tender mode literally named "   " would otherwise show up
		// between Cash and Mobile Money on every ops dashboard.
		Bill bill = postedBillWithLineItem("R-212", new BigDecimal("100"), 1);
		Payment noMode = new Payment();
		noMode.setAmount(new BigDecimal("10"));
		noMode.setAmountTendered(new BigDecimal("10"));
		noMode.setVoided(false);
		bill.setPayments(new HashSet<>());
		bill.getPayments().add(noMode);
		PaymentMode blank = new PaymentMode();
		blank.setName("");
		addPayment(bill, new BigDecimal("5"), blank);
		PaymentMode whitespace = new PaymentMode();
		whitespace.setName("   ");
		addPayment(bill, new BigDecimal("5"), whitespace);
		addPayment(bill, new BigDecimal("20"), paymentMode("Cash"));
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNotNull(doc);
		assertEquals(Arrays.asList("Cash"), doc.getMetadata().get(BillingQueryStoreConstants.FIELD_PAYMENT_MODES));
	}
	
	@Test
	public void serialize_shouldExcludeVoidedPaymentsFromPaymentModes() {
		Bill bill = postedBillWithLineItem("R-201", new BigDecimal("100"), 1);
		addPayment(bill, new BigDecimal("30"), paymentMode("Cash"));
		Payment voided = new Payment();
		voided.setAmount(new BigDecimal("20"));
		voided.setAmountTendered(new BigDecimal("20"));
		voided.setVoided(true);
		voided.setInstanceType(paymentMode("Mobile Money"));
		bill.getPayments().add(voided);
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNotNull(doc);
		assertEquals(Arrays.asList("Cash"), doc.getMetadata().get(BillingQueryStoreConstants.FIELD_PAYMENT_MODES));
	}
	
	@Test
	public void serialize_shouldOmitPaymentModesMetadataWhenNoNonVoidedPayments() {
		Bill bill = postedBillWithLineItem("R-202", new BigDecimal("100"), 1);
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNotNull(doc);
		assertFalse(doc.getMetadata().containsKey(BillingQueryStoreConstants.FIELD_PAYMENT_MODES));
	}
	
	@Test
	public void serialize_shouldEmitDistinctDiscountStatuses() {
		// "Which bills have a pending discount?" is the workflow-queue question this enables.
		// Voided discounts must not pollute the field — a rejected then re-issued discount
		// shouldn't show its old rejected status.
		Bill bill = postedBillWithLineItem("R-203", new BigDecimal("100"), 1);
		addDiscount(bill, DiscountStatus.PENDING, false);
		addDiscount(bill, DiscountStatus.APPROVED, false);
		addDiscount(bill, DiscountStatus.PENDING, false);
		addDiscount(bill, DiscountStatus.REJECTED, true);
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNotNull(doc);
		@SuppressWarnings("unchecked")
		List<String> statuses = (List<String>) doc.getMetadata().get(BillingQueryStoreConstants.FIELD_DISCOUNT_STATUSES);
		assertNotNull(statuses);
		assertEquals(2, statuses.size());
		assertTrue(statuses.contains("PENDING"));
		assertTrue(statuses.contains("APPROVED"));
		assertFalse(statuses.contains("REJECTED"));
	}
	
	@Test
	public void serialize_shouldSkipDiscountsWithNullStatus() {
		// A half-constructed discount may reach the serializer with status still null. Indexing
		// the literal string "null" or NPE'ing inside the loop would silently drop the bill from
		// the index via the indexing advice's per-entity exception swallow.
		Bill bill = postedBillWithLineItem("R-220", new BigDecimal("100"), 1);
		addDiscount(bill, null, false);
		addDiscount(bill, DiscountStatus.PENDING, false);
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNotNull(doc);
		assertEquals(Arrays.asList("PENDING"), doc.getMetadata().get(BillingQueryStoreConstants.FIELD_DISCOUNT_STATUSES));
	}
	
	@Test
	public void serialize_shouldOmitDiscountStatusesMetadataWhenNoNonVoidedDiscounts() {
		Bill bill = postedBillWithLineItem("R-204", new BigDecimal("100"), 1);
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNotNull(doc);
		assertFalse(doc.getMetadata().containsKey(BillingQueryStoreConstants.FIELD_DISCOUNT_STATUSES));
	}
	
	@Test
	public void serialize_shouldIncludeBillAdjustedUuidWhenBillAdjustsAnother() {
		Bill original = new Bill();
		original.setUuid("original-bill-uuid");
		Bill bill = postedBillWithLineItem("R-205", new BigDecimal("50"), 1);
		bill.setBillAdjusted(original);
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNotNull(doc);
		assertEquals("original-bill-uuid", doc.getMetadata().get(BillingQueryStoreConstants.FIELD_BILL_ADJUSTED_UUID));
	}
	
	@Test
	public void serialize_shouldIncludeAdjustedByUuidsWhenBillHasBeenAdjusted() {
		Bill bill = postedBillWithLineItem("R-206", new BigDecimal("50"), 1);
		Bill adjusterA = new Bill();
		adjusterA.setUuid("adjuster-a");
		Bill adjusterB = new Bill();
		adjusterB.setUuid("adjuster-b");
		bill.setAdjustedBy(new HashSet<>(Arrays.asList(adjusterA, adjusterB)));
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNotNull(doc);
		@SuppressWarnings("unchecked")
		List<String> uuids = (List<String>) doc.getMetadata().get(BillingQueryStoreConstants.FIELD_ADJUSTED_BY_UUIDS);
		assertNotNull(uuids);
		assertEquals(2, uuids.size());
		assertTrue(uuids.contains("adjuster-a"));
		assertTrue(uuids.contains("adjuster-b"));
	}
	
	@Test
	public void serialize_shouldOmitAdjustmentFieldsWhenAbsent() {
		Bill bill = postedBillWithLineItem("R-207", new BigDecimal("50"), 1);
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNotNull(doc);
		assertFalse(doc.getMetadata().containsKey(BillingQueryStoreConstants.FIELD_BILL_ADJUSTED_UUID));
		assertFalse(doc.getMetadata().containsKey(BillingQueryStoreConstants.FIELD_ADJUSTED_BY_UUIDS));
		assertFalse(doc.getMetadata().containsKey(BillingQueryStoreConstants.FIELD_ADJUSTMENT_REASON));
	}
	
	@Test
	public void serialize_shouldIncludeAdjustmentReasonWhenSet() {
		Bill bill = postedBillWithLineItem("R-208", new BigDecimal("50"), 1);
		bill.setAdjustmentReason("Patient was overcharged for consult");
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNotNull(doc);
		assertEquals("Patient was overcharged for consult",
		    doc.getMetadata().get(BillingQueryStoreConstants.FIELD_ADJUSTMENT_REASON));
	}
	
	@Test
	public void serialize_shouldOmitAdjustmentReasonWhenEmpty() {
		// An empty-string reason is semantically equivalent to "no reason given" — emitting it
		// would pollute "find bills with an adjustment reason" exists-queries with empty matches.
		Bill bill = postedBillWithLineItem("R-208a", new BigDecimal("50"), 1);
		bill.setAdjustmentReason("");
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNotNull(doc);
		assertFalse(doc.getMetadata().containsKey(BillingQueryStoreConstants.FIELD_ADJUSTMENT_REASON));
	}
	
	@Test
	public void serialize_shouldEmitAdjustedByUuidsSorted() {
		// Bill.adjustedBy is a HashSet — without sorting, the same logical state would emit
		// different document bytes across reindexes, breaking snapshot/cache stability.
		Bill bill = postedBillWithLineItem("R-208b", new BigDecimal("50"), 1);
		Bill adjusterC = new Bill();
		adjusterC.setUuid("z-uuid");
		Bill adjusterA = new Bill();
		adjusterA.setUuid("a-uuid");
		Bill adjusterB = new Bill();
		adjusterB.setUuid("m-uuid");
		bill.setAdjustedBy(new HashSet<>(Arrays.asList(adjusterC, adjusterA, adjusterB)));
		
		QueryDocument doc = serializer.serialize(bill);
		
		assertNotNull(doc);
		assertEquals(Arrays.asList("a-uuid", "m-uuid", "z-uuid"),
		    doc.getMetadata().get(BillingQueryStoreConstants.FIELD_ADJUSTED_BY_UUIDS));
	}
	
	@Test
	public void serialize_shouldAlwaysEmitReceiptPrintedAsBoolean() {
		// Always present so consumers can build "paid bills not yet printed" queries with a single
		// term filter, no exists-clause. Null on the entity is normalized to false — a half-saved
		// bill that never set the flag is treated as not-printed, which is the safer default.
		Bill notPrinted = postedBillWithLineItem("R-209", new BigDecimal("50"), 1);
		notPrinted.setReceiptPrinted(false);
		Bill printed = postedBillWithLineItem("R-210", new BigDecimal("50"), 1);
		printed.setReceiptPrinted(true);
		Bill unset = postedBillWithLineItem("R-211", new BigDecimal("50"), 1);
		unset.setReceiptPrinted(null);
		
		assertEquals(Boolean.FALSE,
		    serializer.serialize(notPrinted).getMetadata().get(BillingQueryStoreConstants.FIELD_RECEIPT_PRINTED));
		assertEquals(Boolean.TRUE,
		    serializer.serialize(printed).getMetadata().get(BillingQueryStoreConstants.FIELD_RECEIPT_PRINTED));
		assertEquals(Boolean.FALSE,
		    serializer.serialize(unset).getMetadata().get(BillingQueryStoreConstants.FIELD_RECEIPT_PRINTED));
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
		bill.getLineItems().add(newLineItem(price, quantity));
		return bill;
	}
	
	private static BillLineItem newLineItem(BigDecimal price, int quantity) {
		BillLineItem lineItem = new BillLineItem();
		lineItem.setPrice(price);
		lineItem.setQuantity(quantity);
		lineItem.setVoided(false);
		return lineItem;
	}
	
	private static void setBillableService(BillLineItem lineItem, String name) {
		BillableService service = new BillableService();
		service.setName(name);
		lineItem.setBillableService(service);
	}
	
	private static void setStockItem(BillLineItem lineItem, String commonName) {
		StockItem item = new StockItem();
		item.setCommonName(commonName);
		lineItem.setItem(item);
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
		addPayment(bill, amount, null);
	}
	
	private void addPayment(Bill bill, BigDecimal amount, PaymentMode mode) {
		if (bill.getPayments() == null) {
			bill.setPayments(new HashSet<>());
		}
		Payment payment = new Payment();
		payment.setAmount(amount);
		payment.setAmountTendered(amount);
		payment.setVoided(false);
		payment.setInstanceType(mode);
		bill.getPayments().add(payment);
	}
	
	private static PaymentMode paymentMode(String name) {
		PaymentMode mode = new PaymentMode();
		mode.setName(name);
		return mode;
	}
	
	private void addDiscount(Bill bill, DiscountStatus status, boolean voided) {
		if (bill.getDiscounts() == null) {
			bill.setDiscounts(new HashSet<>());
		}
		BillDiscount discount = new BillDiscount();
		discount.setDiscountType(DiscountType.FIXED_AMOUNT);
		discount.setDiscountValue(new BigDecimal("5"));
		discount.setStatus(status);
		discount.setVoided(voided);
		bill.getDiscounts().add(discount);
	}
}
