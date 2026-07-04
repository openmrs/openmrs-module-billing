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
import org.openmrs.module.billing.api.model.BillDiscount;
import org.openmrs.module.billing.api.model.DiscountType;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.serialization.AbstractRecordSerializer;
import org.openmrs.module.querystore.util.DateFormatUtil;

/**
 * Serializes a {@link BillDiscount} into a {@code billing_discount} document. A discount / fee
 * waiver is patient-relevant because it often signals a subsidized programme (HIV, TB, under-5,
 * indigent) or financial hardship. It is indexed as its own type - rather than folded into the bill
 * - because its lifecycle (PENDING -> APPROVED/REJECTED) is driven through its own
 * {@code saveBillDiscount} service call, so its own save events keep the projection current without
 * re-saving the bill.
 */
public class BillDiscountRecordSerializer extends AbstractRecordSerializer<BillDiscount> {
	
	public static final String RESOURCE_TYPE = "billing_discount";
	
	@Override
	public String getResourceType() {
		return RESOURCE_TYPE;
	}
	
	@Override
	public Class<BillDiscount> getSupportedType() {
		return BillDiscount.class;
	}
	
	@Override
	protected String getPatientUuid(BillDiscount discount) {
		Bill bill = discount.getBill();
		if (bill == null || bill.getPatient() == null) {
			return null;
		}
		return bill.getPatient().getUuid();
	}
	
	@Override
	protected String getResourceUuid(BillDiscount discount) {
		return discount.getUuid();
	}
	
	@Override
	protected LocalDate getDate(BillDiscount discount) {
		return discount.getDateCreated() != null ? DateFormatUtil.toLocalDate(discount.getDateCreated()) : null;
	}
	
	@Override
	protected void populate(BillDiscount discount, QueryDocument doc) {
		Bill bill = discount.getBill();
		String receipt = bill != null ? trimToNull(bill.getReceiptNumber()) : null;
		
		StringBuilder text = new StringBuilder("Bill discount");
		if (receipt != null) {
			text.append(" on bill ").append(receipt);
		}
		text.append(": ").append(describeAmount(discount));
		if (discount.getStatus() != null) {
			text.append(". Status: ").append(readable(discount.getStatus().name()));
		}
		String justification = trimToNull(discount.getJustification());
		if (justification != null) {
			text.append(". Justification: ").append(justification);
		}
		text.append('.');
		doc.setText(text.toString());
		
		if (discount.getStatus() != null) {
			doc.putMetadata("discount_status", discount.getStatus().name());
		}
		if (discount.getDiscountType() != null) {
			doc.putMetadata("discount_type", discount.getDiscountType().name());
		}
		if (discount.getDiscountValue() != null) {
			doc.putMetadata("discount_value", money(discount.getDiscountValue()));
		}
		doc.putMetadata("discount_amount", money(discount.getDiscountAmount()));
		if (bill != null && bill.getUuid() != null) {
			doc.putMetadata("bill_uuid", bill.getUuid());
		}
		if (receipt != null) {
			doc.putMetadata("receipt_number", receipt);
		}
	}
	
	private static String describeAmount(BillDiscount discount) {
		DiscountType type = discount.getDiscountType();
		if (type == DiscountType.PERCENTAGE && discount.getDiscountValue() != null) {
			return money(discount.getDiscountValue()) + "% (" + money(discount.getDiscountAmount()) + ")";
		}
		// FIXED_AMOUNT (or an unspecified type): the amount is the value itself.
		return money(discount.getDiscountAmount());
	}
}
