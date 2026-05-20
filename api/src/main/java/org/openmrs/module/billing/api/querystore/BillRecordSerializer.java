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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillDiscount;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.Payment;
import org.openmrs.module.billing.api.model.PaymentMode;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.serialization.AbstractRecordSerializer;
import org.openmrs.module.querystore.util.DateFormatUtil;

public class BillRecordSerializer extends AbstractRecordSerializer<Bill> {
	
	@Override
	public String getResourceType() {
		return BillingQueryStoreConstants.RESOURCE_TYPE_BILL;
	}
	
	@Override
	public Class<Bill> getSupportedType() {
		return Bill.class;
	}
	
	@Override
	protected String getPatientUuid(Bill bill) {
		Patient patient = bill.getPatient();
		return patient != null ? patient.getUuid() : null;
	}
	
	@Override
	protected String getResourceUuid(Bill bill) {
		return bill.getUuid();
	}
	
	@Override
	protected LocalDate getDate(Bill bill) {
		return DateFormatUtil.toLocalDate(bill.getDateCreated());
	}
	
	@Override
	protected void populate(Bill bill, QueryDocument doc) {
		if (bill.getPatient() == null) {
			return;
		}
		
		BillStatus status = bill.getStatus();
		BigDecimal total = bill.getTotal();
		BigDecimal amountAfterDiscount = bill.getAmountAfterDiscount();
		BigDecimal totalPaid = bill.getTotalPayments();
		// Balance must be derived from amountAfterDiscount, not total — a bill whose only discount
		// brings the effective amount to ≤ totalPayments has a zero/negative balance, and using
		// gross total here would over-state what's still owed.
		BigDecimal balance = amountAfterDiscount.subtract(totalPaid);
		
		// Multi-valued metadata is stored as a List<String> per the querystore module convention
		// (see VisitRecordSerializer's FIELD_ENCOUNTER_UUIDS, AllergyRecordSerializer's FIELD_REACTIONS).
		// Storing a comma-joined string would force consumers into substring matching, breaking
		// exact-match queries like "bills containing item X".
		List<String> itemNames = collectLineItemNames(bill);
		
		String receiptOrUuid = bill.getReceiptNumber() != null ? bill.getReceiptNumber() : bill.getUuid();
		String itemsClause = itemNames.isEmpty() ? "" : " Items: " + String.join(", ", itemNames) + ".";
		doc.setText(String.format("Bill %s. Status: %s. Total: %s. Paid: %s. Balance: %s.%s", receiptOrUuid,
		    status != null ? status.name() : "UNKNOWN", total.toPlainString(), totalPaid.toPlainString(),
		    balance.toPlainString(), itemsClause));
		
		if (!itemNames.isEmpty()) {
			doc.putMetadata(BillingQueryStoreConstants.FIELD_LINE_ITEM_NAMES, itemNames);
		}
		doc.putMetadata(BillingQueryStoreConstants.FIELD_RECEIPT_NUMBER, bill.getReceiptNumber());
		doc.putMetadata(BillingQueryStoreConstants.FIELD_STATUS, status != null ? status.name() : null);
		doc.putMetadata(BillingQueryStoreConstants.FIELD_TOTAL, total);
		doc.putMetadata(BillingQueryStoreConstants.FIELD_AMOUNT_AFTER_DISCOUNT, amountAfterDiscount);
		doc.putMetadata(BillingQueryStoreConstants.FIELD_TOTAL_PAID, totalPaid);
		doc.putMetadata(BillingQueryStoreConstants.FIELD_BALANCE, balance);
		doc.putMetadata(BillingQueryStoreConstants.FIELD_VOIDED, bill.getVoided());
		
		if (bill.getCashier() != null) {
			doc.putMetadata(BillingQueryStoreConstants.FIELD_CASHIER_UUID, bill.getCashier().getUuid());
		}
		if (bill.getCashPoint() != null) {
			doc.putMetadata(BillingQueryStoreConstants.FIELD_CASH_POINT_UUID, bill.getCashPoint().getUuid());
			doc.putMetadata(BillingQueryStoreConstants.FIELD_CASH_POINT_NAME, bill.getCashPoint().getName());
		}
		Visit visit = bill.getVisit();
		if (visit != null) {
			doc.putMetadata(BillingQueryStoreConstants.FIELD_VISIT_UUID, visit.getUuid());
		}
		
		List<String> paymentModes = collectPaymentModes(bill);
		if (!paymentModes.isEmpty()) {
			doc.putMetadata(BillingQueryStoreConstants.FIELD_PAYMENT_MODES, paymentModes);
		}
		List<String> discountStatuses = collectDiscountStatuses(bill);
		if (!discountStatuses.isEmpty()) {
			doc.putMetadata(BillingQueryStoreConstants.FIELD_DISCOUNT_STATUSES, discountStatuses);
		}
		if (bill.getBillAdjusted() != null) {
			doc.putMetadata(BillingQueryStoreConstants.FIELD_BILL_ADJUSTED_UUID, bill.getBillAdjusted().getUuid());
		}
		List<String> adjustedByUuids = collectAdjustedByUuids(bill);
		if (!adjustedByUuids.isEmpty()) {
			doc.putMetadata(BillingQueryStoreConstants.FIELD_ADJUSTED_BY_UUIDS, adjustedByUuids);
		}
		if (bill.getAdjustmentReason() != null && !bill.getAdjustmentReason().trim().isEmpty()) {
			doc.putMetadata(BillingQueryStoreConstants.FIELD_ADJUSTMENT_REASON, bill.getAdjustmentReason());
		}
		// Boolean.TRUE.equals normalizes null → false. Persisted bills always have a value
		// (Bill.hbm.xml: not-null, defaults to false), so this only matters for hand-built bills
		// in tests or for in-flight bills constructed via the builder paths. Always-emit pattern
		// lets consumers write "paid bills not yet printed" as a single term filter, no
		// exists-clause.
		doc.putMetadata(BillingQueryStoreConstants.FIELD_RECEIPT_PRINTED, Boolean.TRUE.equals(bill.getReceiptPrinted()));
	}
	
	private List<String> collectLineItemNames(Bill bill) {
		List<String> names = new ArrayList<>();
		if (bill.getLineItems() == null) {
			return names;
		}
		for (BillLineItem lineItem : bill.getLineItems()) {
			if (lineItem == null || lineItem.getVoided()) {
				continue;
			}
			String name = BillingDisplayNames.lineItemDisplayName(lineItem);
			if (name != null) {
				names.add(name);
			}
		}
		return names;
	}
	
	// Distinct + sorted. Bill.payments and Bill.discounts are Set<>s, so iteration order is
	// non-deterministic — sorting gives consumers a stable list for snapshot / cache use without
	// committing to any source-side ordering contract. Also keeps the resulting bill document
	// bytewise-identical across reindexes of the same logical state.
	private List<String> collectPaymentModes(Bill bill) {
		Set<String> modes = new TreeSet<>();
		if (bill.getPayments() == null) {
			return new ArrayList<>(modes);
		}
		for (Payment payment : bill.getPayments()) {
			if (payment == null || payment.getVoided()) {
				continue;
			}
			PaymentMode mode = payment.getInstanceType();
			// Whitespace-only names slip past isEmpty(); a tender mode literally named "   "
			// would otherwise show up in the indexed list between Cash and Mobile Money.
			if (mode != null && mode.getName() != null && !mode.getName().trim().isEmpty()) {
				modes.add(mode.getName());
			}
		}
		return new ArrayList<>(modes);
	}
	
	private List<String> collectDiscountStatuses(Bill bill) {
		Set<String> statuses = new TreeSet<>();
		if (bill.getDiscounts() == null) {
			return new ArrayList<>(statuses);
		}
		for (BillDiscount discount : bill.getDiscounts()) {
			if (discount == null || discount.getVoided() || discount.getStatus() == null) {
				continue;
			}
			statuses.add(discount.getStatus().name());
		}
		return new ArrayList<>(statuses);
	}
	
	private List<String> collectAdjustedByUuids(Bill bill) {
		// Sorted for the same reason payment_modes / discount_statuses are: Bill.adjustedBy is a
		// HashSet, so iteration order is non-deterministic. Without the sort, the same logical
		// state would emit different document bytes across reindexes.
		Set<String> uuids = new TreeSet<>();
		if (bill.getAdjustedBy() == null) {
			return new ArrayList<>(uuids);
		}
		for (Bill adjuster : bill.getAdjustedBy()) {
			if (adjuster != null && adjuster.getUuid() != null) {
				uuids.add(adjuster.getUuid());
			}
		}
		return new ArrayList<>(uuids);
	}
}
