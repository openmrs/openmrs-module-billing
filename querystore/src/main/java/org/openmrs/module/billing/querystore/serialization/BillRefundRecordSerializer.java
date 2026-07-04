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

import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillRefund;
import org.openmrs.module.querystore.model.QueryDocument;

/**
 * Serializes a {@link BillRefund} into a {@code billing_refund} document. Refunds are the weakest
 * clinical signal of the three billing types (mostly administrative), but they complete the record
 * and are indexed as their own type because their lifecycle (REQUESTED -> APPROVED -> COMPLETED)
 * runs through the dedicated {@code saveBillRefund} service, whose save events keep the projection
 * current.
 */
public class BillRefundRecordSerializer extends AbstractBillChildRecordSerializer<BillRefund> {
	
	public static final String RESOURCE_TYPE = "billing_refund";
	
	@Override
	public String getResourceType() {
		return RESOURCE_TYPE;
	}
	
	@Override
	public Class<BillRefund> getSupportedType() {
		return BillRefund.class;
	}
	
	@Override
	protected Bill billOf(BillRefund refund) {
		return refund.getBill();
	}
	
	@Override
	protected void populate(BillRefund refund, QueryDocument doc) {
		if (doc.getPatientUuid() == null) {
			return; // no patient scope (unreachable: bill/patient are NOT NULL FKs) - skip, matching backfill
		}
		Bill bill = refund.getBill();
		String receipt = receiptOf(bill);
		
		StringBuilder text = new StringBuilder("Bill refund");
		if (receipt != null) {
			text.append(" on bill ").append(receipt);
		}
		text.append(": amount ").append(money(refund.getRefundAmount()));
		if (refund.getStatus() != null) {
			text.append(". Status: ").append(readable(refund.getStatus().name()));
		}
		String reason = trimToNull(refund.getReason());
		if (reason != null) {
			text.append(". Reason: ").append(reason);
		}
		text.append('.');
		doc.setText(text.toString());
		
		if (refund.getStatus() != null) {
			doc.putMetadata(BillingQueryFields.REFUND_STATUS, refund.getStatus().name());
		}
		doc.putMetadata(BillingQueryFields.REFUND_AMOUNT, money(refund.getRefundAmount()));
		putBillReference(doc, bill, receipt);
	}
}
