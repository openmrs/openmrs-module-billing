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

import org.openmrs.Patient;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillDiscount;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.DiscountStatus;
import org.openmrs.module.billing.api.model.DiscountType;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.serialization.AbstractRecordSerializer;
import org.openmrs.module.querystore.util.DateFormatUtil;

public class BillDiscountRecordSerializer extends AbstractRecordSerializer<BillDiscount> {
	
	@Override
	public String getResourceType() {
		return BillingQueryStoreConstants.RESOURCE_TYPE_BILL_DISCOUNT;
	}
	
	@Override
	public Class<BillDiscount> getSupportedType() {
		return BillDiscount.class;
	}
	
	@Override
	protected String getPatientUuid(BillDiscount discount) {
		Bill bill = discount.getBill();
		if (bill == null) {
			return null;
		}
		Patient patient = bill.getPatient();
		return patient != null ? patient.getUuid() : null;
	}
	
	@Override
	protected String getResourceUuid(BillDiscount discount) {
		return discount.getUuid();
	}
	
	@Override
	protected LocalDate getDate(BillDiscount discount) {
		return DateFormatUtil.toLocalDate(discount.getDateCreated());
	}
	
	@Override
	protected void populate(BillDiscount discount, QueryDocument doc) {
		Bill bill = discount.getBill();
		if (bill == null || bill.getPatient() == null) {
			return;
		}
		// Defensive: if discountType or discountValue is null the validator should have rejected
		// the row, but the indexing advice swallows RuntimeException per-entity and would silently
		// drop the discount from the approval queue. Skip the document rather than NPE inside
		// getDiscountAmount().
		if (discount.getDiscountType() == null || discount.getDiscountValue() == null) {
			return;
		}
		
		DiscountStatus status = discount.getStatus();
		DiscountType type = discount.getDiscountType();
		BigDecimal value = discount.getDiscountValue();
		BigDecimal amount = discount.getDiscountAmount();
		String receiptOrUuid = bill.getReceiptNumber() != null ? bill.getReceiptNumber() : bill.getUuid();
		
		doc.setText(String.format("Discount on bill %s. Status: %s. Type: %s. Value: %s. Amount: %s. Reason: %s.",
		    receiptOrUuid, status != null ? status.name() : "UNKNOWN", type.name(), value.toPlainString(),
		    amount.toPlainString(), discount.getJustification() != null ? discount.getJustification() : ""));
		
		doc.putMetadata(BillingQueryStoreConstants.FIELD_BILL_UUID, bill.getUuid());
		doc.putMetadata(BillingQueryStoreConstants.FIELD_RECEIPT_NUMBER, bill.getReceiptNumber());
		doc.putMetadata(BillingQueryStoreConstants.FIELD_STATUS, status != null ? status.name() : null);
		doc.putMetadata(BillingQueryStoreConstants.FIELD_DISCOUNT_TYPE, type.name());
		doc.putMetadata(BillingQueryStoreConstants.FIELD_DISCOUNT_VALUE, value);
		doc.putMetadata(BillingQueryStoreConstants.FIELD_DISCOUNT_AMOUNT, amount);
		doc.putMetadata(BillingQueryStoreConstants.FIELD_JUSTIFICATION, discount.getJustification());
		doc.putMetadata(BillingQueryStoreConstants.FIELD_VOIDED, discount.getVoided());
		
		BillLineItem lineItem = discount.getLineItem();
		if (lineItem != null) {
			doc.putMetadata(BillingQueryStoreConstants.FIELD_BILL_LINE_ITEM_UUID, lineItem.getUuid());
		}
		if (discount.getInitiator() != null) {
			doc.putMetadata(BillingQueryStoreConstants.FIELD_INITIATOR_UUID, discount.getInitiator().getUuid());
		}
		if (discount.getApprover() != null) {
			doc.putMetadata(BillingQueryStoreConstants.FIELD_APPROVER_UUID, discount.getApprover().getUuid());
		}
		
		BillingAuditFields.populate(doc, discount);
	}
}
