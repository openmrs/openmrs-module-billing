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
import java.util.Collections;

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
		// Defensive: AbstractIndexingAdvice swallows RuntimeException per-entity, so an NPE here
		// would silently drop the refund from the index with only a warn-level log. A partially
		// constructed refund (validator gap, recovered transient) should be skipped, not crash.
		if (refund.getRefundAmount() == null) {
			return;
		}
		
		RefundStatus status = refund.getStatus();
		String receiptOrUuid = bill.getReceiptNumber() != null ? bill.getReceiptNumber() : bill.getUuid();
		// A refund is an audit record of a past line item. Even if the line item has since been
		// voided on the bill, the refund must still carry the item's name so the audit trail
		// reads coherently — the parent bill's indexed names omit voided items, but the refund's
		// own indexed name preserves them.
		String itemName = BillingDisplayNames.lineItemDisplayName(refund.getLineItem());
		// Singular "Item:" (vs. the bill's plural "Items:") is intentional — a refund row is
		// always line-scoped, so consumers parsing the text blob can rely on at most one item.
		String itemClause = itemName != null ? " Item: " + itemName + "." : "";
		doc.setText(String.format("Refund of %s for bill %s. Status: %s. Reason: %s.%s",
		    refund.getRefundAmount().toPlainString(), receiptOrUuid, status != null ? status.name() : "UNKNOWN",
		    refund.getReason() != null ? refund.getReason() : "", itemClause));
		
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
		if (itemName != null) {
			// Stored as a singleton list to match the Bill serializer's shape — consumers branch
			// on resource type but share the field key, so a String here against a List there
			// would surface as ClassCastException downstream.
			doc.putMetadata(BillingQueryStoreConstants.FIELD_LINE_ITEM_NAMES, Collections.singletonList(itemName));
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
