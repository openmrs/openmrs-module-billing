/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.querystore.serialization;

import static org.openmrs.module.billing.querystore.serialization.BillDocFormat.money;
import static org.openmrs.module.billing.querystore.serialization.BillDocFormat.readable;
import static org.openmrs.module.querystore.QueryStoreConstants.FIELD_VISIT_UUID;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openmrs.Drug;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.Payment;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.serialization.AbstractRecordSerializer;
import org.openmrs.module.querystore.util.DateFormatUtil;
import org.openmrs.module.stockmanagement.api.model.StockItem;

/**
 * Serializes a {@link Bill} into a single, clinician-oriented {@code billing_bill} document.
 * <p>
 * A bill is the patient-anchored unit here: its line items (the services / drugs / tests the
 * patient was charged for) and its payments cascade with it and are persisted atomically on
 * {@code saveBill}, so they are folded into this one document rather than indexed separately. The
 * searchable {@code text} leads with the billed items - what a clinician reading the chart would
 * actually search for - then the raw total, amount paid and status.
 * <p>
 * <b>Only bill-aggregate-derived, always-refreshed fields are folded in.</b> The projection is
 * re-run by querystore's events consumer whenever a {@code *ServiceEvent} fires for the bill
 * ({@code saveBill} / {@code voidBill} / {@code purgeBill}), so a folded field is only kept current
 * if every mutation that changes it re-saves the bill. That holds for line items, payments and
 * status. It deliberately does <em>not</em> hold for the discount-adjusted total / outstanding
 * balance: approving a fee waiver goes through {@code BillDiscountService.saveBillDiscount}, which
 * persists the discount alone and does not re-save the bill - so a denormalized "amount after
 * discount" here would silently overstate what the patient owes until the bill's next save. We
 * therefore expose the raw (pre-discount) line-item {@code total} plus {@code amount_paid} and
 * leave discount detail to the {@code billing_discount} document; {@code status} conveys
 * settled-ness.
 * <p>
 * Residual eventual-consistency: the dedicated single-child endpoints ({@code voidBillLineItem},
 * discount approval) mutate bill-child state without re-saving the bill, so a folded item can lag
 * until the bill's next save. Acceptable for a retrieval index and self-healing; authoritative
 * figures live in the billing UI/API.
 */
public class BillRecordSerializer extends AbstractRecordSerializer<Bill> {
	
	public static final String RESOURCE_TYPE = "billing_bill";
	
	@Override
	public String getResourceType() {
		return RESOURCE_TYPE;
	}
	
	@Override
	public Class<Bill> getSupportedType() {
		return Bill.class;
	}
	
	@Override
	protected String getPatientUuid(Bill bill) {
		return bill.getPatient() != null ? bill.getPatient().getUuid() : null;
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
		if (doc.getPatientUuid() == null) {
			// No patient scope (unreachable for a persisted bill - patient is a NOT NULL FK). Leaving
			// text unset makes serialize() skip it, matching the backfill scan's patient IS NOT NULL filter.
			return;
		}
		
		List<String> services = billedItemLabels(bill);
		
		// Raw line-item total (discount-independent). See the class javadoc for why the
		// discount-adjusted total/balance are intentionally not folded in here.
		BigDecimal total = bill.getTotal();
		BigDecimal paid = bill.getTotalPayments();
		
		StringBuilder text = new StringBuilder("Bill");
		String receipt = trimToNull(bill.getReceiptNumber());
		if (receipt != null) {
			text.append(' ').append(receipt);
		}
		if (bill.getStatus() != null) {
			text.append(". Status: ").append(readable(bill.getStatus().name()));
		}
		if (!services.isEmpty()) {
			text.append(". Services billed: ").append(String.join(", ", services));
		}
		text.append(". Total: ").append(money(total)).append(", paid: ").append(money(paid));
		String cashPoint = bill.getCashPoint() != null ? trimToNull(bill.getCashPoint().getName()) : null;
		if (cashPoint != null) {
			text.append(". Cash point: ").append(cashPoint);
		}
		text.append('.');
		doc.setText(text.toString());
		
		if (bill.getStatus() != null) {
			doc.putMetadata(BillingQueryFields.BILL_STATUS, bill.getStatus().name());
		}
		if (receipt != null) {
			doc.putMetadata(BillingQueryFields.RECEIPT_NUMBER, receipt);
		}
		doc.putMetadata(BillingQueryFields.TOTAL, money(total));
		doc.putMetadata(BillingQueryFields.AMOUNT_PAID, money(paid));
		if (cashPoint != null) {
			doc.putMetadata(BillingQueryFields.CASH_POINT, cashPoint);
		}
		if (bill.getVisit() != null) {
			doc.putMetadata(FIELD_VISIT_UUID, bill.getVisit().getUuid());
		}
		if (!services.isEmpty()) {
			doc.putMetadata(BillingQueryFields.SERVICES, services);
			doc.putMetadata(BillingQueryFields.BILLED_SERVICE_COUNT, services.size());
		}
		List<String> modes = paymentModes(bill);
		if (!modes.isEmpty()) {
			doc.putMetadata(BillingQueryFields.PAYMENT_MODES, modes);
		}
	}
	
	/**
	 * Label for each non-voided line item (one entry per line item - not de-duplicated, since two line
	 * items for the same service are two distinct charges), preferring the billable service name and
	 * falling back to the stock item (dispensed drug / supply) or the price name.
	 */
	private static List<String> billedItemLabels(Bill bill) {
		if (bill.getLineItems() == null) {
			return Collections.emptyList();
		}
		List<String> labels = new ArrayList<String>();
		for (BillLineItem item : bill.getLineItems()) {
			if (item == null || Boolean.TRUE.equals(item.getVoided())) {
				continue;
			}
			String label = labelFor(item);
			if (label != null) {
				if (item.getQuantity() != null && item.getQuantity() != 1) {
					label = label + " (x" + item.getQuantity() + ")";
				}
				labels.add(label);
			}
		}
		return labels;
	}
	
	private static String labelFor(BillLineItem item) {
		if (item.getBillableService() != null) {
			String name = trimToNull(item.getBillableService().getName());
			if (name != null) {
				return name;
			}
		}
		StockItem stockItem = item.getItem();
		if (stockItem != null) {
			String common = trimToNull(stockItem.getCommonName());
			if (common != null) {
				return common;
			}
			Drug drug = stockItem.getDrug();
			if (drug != null) {
				String drugName = trimToNull(drug.getName());
				if (drugName != null) {
					return drugName;
				}
			}
		}
		return trimToNull(item.getPriceName());
	}
	
	/** Distinct payment-mode names across the bill's non-voided payments. */
	private static List<String> paymentModes(Bill bill) {
		if (bill.getPayments() == null) {
			return Collections.emptyList();
		}
		Set<String> modes = new LinkedHashSet<String>();
		for (Payment payment : bill.getPayments()) {
			if (payment == null || Boolean.TRUE.equals(payment.getVoided())) {
				continue;
			}
			if (payment.getInstanceType() != null) {
				String name = trimToNull(payment.getInstanceType().getName());
				if (name != null) {
					modes.add(name);
				}
			}
		}
		return new ArrayList<String>(modes);
	}
}
