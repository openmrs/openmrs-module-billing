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

import java.time.LocalDate;

import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillRefund;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.serialization.AbstractRecordSerializer;
import org.openmrs.module.querystore.util.DateFormatUtil;

/**
 * Serializes a {@link BillRefund} into a {@code billing_refund} document. Refunds are the weakest
 * clinical signal of the three billing types (mostly administrative), but they complete the record
 * and are indexed as their own type because their lifecycle (REQUESTED -> APPROVED -> COMPLETED)
 * runs through the dedicated {@code saveBillRefund} service, whose save events keep the projection
 * current.
 */
public class BillRefundRecordSerializer extends AbstractRecordSerializer<BillRefund> {
	
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
	protected String getPatientUuid(BillRefund refund) {
		Bill bill = refund.getBill();
		if (bill == null || bill.getPatient() == null) {
			return null;
		}
		return bill.getPatient().getUuid();
	}
	
	@Override
	protected String getResourceUuid(BillRefund refund) {
		return refund.getUuid();
	}
	
	@Override
	protected LocalDate getDate(BillRefund refund) {
		return refund.getDateCreated() != null ? DateFormatUtil.toLocalDate(refund.getDateCreated()) : null;
	}
	
	@Override
	protected void populate(BillRefund refund, QueryDocument doc) {
		Bill bill = refund.getBill();
		String receipt = bill != null ? trimToNull(bill.getReceiptNumber()) : null;
		
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
			doc.putMetadata("refund_status", refund.getStatus().name());
		}
		doc.putMetadata("refund_amount", money(refund.getRefundAmount()));
		if (bill != null && bill.getUuid() != null) {
			doc.putMetadata("bill_uuid", bill.getUuid());
		}
		if (receipt != null) {
			doc.putMetadata("receipt_number", receipt);
		}
	}
}
