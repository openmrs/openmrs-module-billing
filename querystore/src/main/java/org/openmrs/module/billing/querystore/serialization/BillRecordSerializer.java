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
import static org.openmrs.module.billing.querystore.serialization.BillDocFormat.trimToNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
 * patient was charged for) and its payments cascade with it and are always current when the bill is
 * saved, so they are folded into this one document rather than indexed separately. The searchable
 * {@code text} leads with the billed items - what a clinician reading the chart would actually
 * search for - and carries the payment picture (total, paid, outstanding balance, status) so a
 * cost-of-care / affordability signal is retrievable too.
 */
public class BillRecordSerializer extends AbstractRecordSerializer<Bill> {
	
	public static final String RESOURCE_TYPE = "billing_bill";
	
	static final String FIELD_BILL_STATUS = "bill_status";
	
	static final String FIELD_RECEIPT_NUMBER = "receipt_number";
	
	static final String FIELD_TOTAL = "total";
	
	static final String FIELD_AMOUNT_PAID = "amount_paid";
	
	static final String FIELD_BALANCE = "balance";
	
	static final String FIELD_CASH_POINT = "cash_point";
	
	static final String FIELD_VISIT_UUID = "visit_uuid";
	
	static final String FIELD_SERVICES = "services";
	
	static final String FIELD_PAYMENT_MODES = "payment_modes";
	
	static final String FIELD_LINE_ITEM_COUNT = "line_item_count";
	
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
		return bill.getDateCreated() != null ? DateFormatUtil.toLocalDate(bill.getDateCreated()) : null;
	}
	
	@Override
	protected void populate(Bill bill, QueryDocument doc) {
		List<String> services = billedItemLabels(bill);
		
		BigDecimal total = bill.getTotal();
		BigDecimal afterDiscount = bill.getAmountAfterDiscount();
		BigDecimal paid = bill.getTotalPayments();
		BigDecimal balance = afterDiscount.subtract(paid);
		
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
		text.append(". Total: ").append(money(total));
		if (afterDiscount.compareTo(total) != 0) {
			text.append(" (after discount ").append(money(afterDiscount)).append(')');
		}
		text.append(", paid: ").append(money(paid)).append(", balance: ").append(money(balance));
		String cashPoint = bill.getCashPoint() != null ? trimToNull(bill.getCashPoint().getName()) : null;
		if (cashPoint != null) {
			text.append(". Cash point: ").append(cashPoint);
		}
		text.append('.');
		doc.setText(text.toString());
		
		if (bill.getStatus() != null) {
			doc.putMetadata(FIELD_BILL_STATUS, bill.getStatus().name());
		}
		if (receipt != null) {
			doc.putMetadata(FIELD_RECEIPT_NUMBER, receipt);
		}
		doc.putMetadata(FIELD_TOTAL, money(afterDiscount));
		doc.putMetadata(FIELD_AMOUNT_PAID, money(paid));
		doc.putMetadata(FIELD_BALANCE, money(balance));
		if (cashPoint != null) {
			doc.putMetadata(FIELD_CASH_POINT, cashPoint);
		}
		if (bill.getVisit() != null) {
			doc.putMetadata(FIELD_VISIT_UUID, bill.getVisit().getUuid());
		}
		if (!services.isEmpty()) {
			doc.putMetadata(FIELD_SERVICES, services);
			doc.putMetadata(FIELD_LINE_ITEM_COUNT, services.size());
		}
		List<String> modes = paymentModes(bill);
		if (!modes.isEmpty()) {
			doc.putMetadata(FIELD_PAYMENT_MODES, modes);
		}
	}
	
	/**
	 * Human-readable label for each non-voided line item, preferring the billable service name and
	 * falling back to the stock item (dispensed drug / supply) or the price name.
	 */
	private static List<String> billedItemLabels(Bill bill) {
		List<String> labels = new ArrayList<String>();
		if (bill.getLineItems() == null) {
			return labels;
		}
		for (BillLineItem item : bill.getLineItems()) {
			if (item == null || Boolean.TRUE.equals(item.getVoided())) {
				continue;
			}
			String label = labelFor(item);
			if (label == null) {
				continue;
			}
			if (item.getQuantity() != null && item.getQuantity() != 1) {
				label = label + " (x" + item.getQuantity() + ")";
			}
			labels.add(label);
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
	
	private static List<String> paymentModes(Bill bill) {
		List<String> modes = new ArrayList<String>();
		if (bill.getPayments() == null) {
			return modes;
		}
		for (Payment payment : bill.getPayments()) {
			if (payment == null || Boolean.TRUE.equals(payment.getVoided())) {
				continue;
			}
			if (payment.getInstanceType() != null) {
				String name = trimToNull(payment.getInstanceType().getName());
				if (name != null && !modes.contains(name)) {
					modes.add(name);
				}
			}
		}
		return modes;
	}
}
