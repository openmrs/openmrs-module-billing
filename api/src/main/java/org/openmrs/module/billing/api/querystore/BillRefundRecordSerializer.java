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

import java.time.LocalDate;

import org.openmrs.Patient;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillRefund;
import org.openmrs.module.billing.api.model.RefundStatus;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.serialization.AbstractRecordSerializer;
import org.openmrs.module.querystore.util.DateFormatUtil;

public class BillRefundRecordSerializer extends AbstractRecordSerializer<BillRefund> {
	
	@Override
	public String getResourceType() {
		return BillingQueryStoreConstants.RESOURCE_TYPE_BILL_REFUND;
	}
	
	@Override
	public Class<BillRefund> getSupportedType() {
		return BillRefund.class;
	}
	
	@Override
	protected String getPatientUuid(BillRefund refund) {
		Bill bill = refund.getBill();
		if (bill == null) {
			return null;
		}
		Patient patient = bill.getPatient();
		return patient != null ? patient.getUuid() : null;
	}
	
	@Override
	protected String getResourceUuid(BillRefund refund) {
		return refund.getUuid();
	}
	
	@Override
	protected LocalDate getDate(BillRefund refund) {
		return DateFormatUtil.toLocalDate(refund.getDateCreated());
	}
	
	@Override
	protected void populate(BillRefund refund, QueryDocument doc) {
		Bill bill = refund.getBill();
		if (bill == null || bill.getPatient() == null) {
			return;
		}
		
		RefundStatus status = refund.getStatus();
		String receiptOrUuid = bill.getReceiptNumber() != null ? bill.getReceiptNumber() : bill.getUuid();
		doc.setText(String.format("Refund of %s for bill %s. Status: %s. Reason: %s.",
		    refund.getRefundAmount().toPlainString(), receiptOrUuid, status != null ? status.name() : "UNKNOWN",
		    refund.getReason() != null ? refund.getReason() : ""));
		
		doc.putMetadata(BillingQueryStoreConstants.FIELD_BILL_UUID, bill.getUuid());
		doc.putMetadata(BillingQueryStoreConstants.FIELD_RECEIPT_NUMBER, bill.getReceiptNumber());
		doc.putMetadata(BillingQueryStoreConstants.FIELD_REFUND_AMOUNT, refund.getRefundAmount());
		doc.putMetadata(BillingQueryStoreConstants.FIELD_STATUS, status != null ? status.name() : null);
		doc.putMetadata(BillingQueryStoreConstants.FIELD_REASON, refund.getReason());
		doc.putMetadata(BillingQueryStoreConstants.FIELD_VOIDED, refund.getVoided());
		doc.putMetadata(BillingQueryStoreConstants.FIELD_DATE_APPROVED, refund.getDateApproved());
		doc.putMetadata(BillingQueryStoreConstants.FIELD_DATE_COMPLETED, refund.getDateCompleted());
		
		BillLineItem lineItem = refund.getLineItem();
		if (lineItem != null) {
			doc.putMetadata(BillingQueryStoreConstants.FIELD_BILL_LINE_ITEM_UUID, lineItem.getUuid());
		}
		if (refund.getInitiator() != null) {
			doc.putMetadata(BillingQueryStoreConstants.FIELD_INITIATOR_UUID, refund.getInitiator().getUuid());
		}
		if (refund.getApprover() != null) {
			doc.putMetadata(BillingQueryStoreConstants.FIELD_APPROVER_UUID, refund.getApprover().getUuid());
		}
		if (refund.getCompleter() != null) {
			doc.putMetadata(BillingQueryStoreConstants.FIELD_COMPLETER_UUID, refund.getCompleter().getUuid());
		}
	}
}
