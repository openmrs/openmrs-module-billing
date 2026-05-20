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

import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
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
}
