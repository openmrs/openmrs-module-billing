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
import static org.openmrs.module.billing.querystore.serialization.BillDocFormat.plain;
import static org.openmrs.module.billing.querystore.serialization.BillDocFormat.readable;

import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillDiscount;
import org.openmrs.module.billing.api.model.DiscountType;
import org.openmrs.module.querystore.model.QueryDocument;

/**
 * Serializes a {@link BillDiscount} into a {@code billing_discount} document. A discount / fee
 * waiver is patient-relevant because it often signals a subsidized programme (HIV, TB, under-5,
 * indigent) or financial hardship. It is indexed as its own type - rather than folded into the bill
 * - because its lifecycle (PENDING -> APPROVED/REJECTED) changes independently of the bill.
 * {@code BillDiscountService} is not an {@code OpenmrsService}, so core #6084's service-event
 * advice never fires for {@code saveBillDiscount}; live-sync is instead handled by
 * {@link org.openmrs.module.billing.querystore.BillChildDbEventListener} (core's non-AOP Hibernate
 * {@code SaveDbEvent}), with initial backfill by {@code BillDiscountBootstrapper}.
 */
public class BillDiscountRecordSerializer extends AbstractBillChildRecordSerializer<BillDiscount> {
	
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
	protected Bill billOf(BillDiscount discount) {
		return discount.getBill();
	}
	
	@Override
	protected void populate(BillDiscount discount, QueryDocument doc) {
		if (doc.getPatientUuid() == null) {
			return; // no patient scope (unreachable: bill/patient are NOT NULL FKs) - skip, matching backfill
		}
		Bill bill = discount.getBill();
		String receipt = receiptOf(bill);
		boolean hasPercent = discount.getDiscountType() == DiscountType.PERCENTAGE && discount.getDiscountValue() != null;
		
		StringBuilder text = new StringBuilder("Bill discount");
		if (receipt != null) {
			text.append(" on bill ").append(receipt);
		}
		text.append(": ").append(describeAmount(discount, hasPercent));
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
			doc.putMetadata(BillingQueryFields.DISCOUNT_STATUS, discount.getStatus().name());
		}
		if (discount.getDiscountType() != null) {
			doc.putMetadata(BillingQueryFields.DISCOUNT_TYPE, discount.getDiscountType().name());
		}
		// A single unit-ambiguous "discount_value" (percent for PERCENTAGE, currency for FIXED_AMOUNT)
		// would break numeric filtering, so expose the percentage only for PERCENTAGE and always the
		// resolved currency amount.
		if (hasPercent) {
			doc.putMetadata(BillingQueryFields.DISCOUNT_PERCENT, plain(discount.getDiscountValue()));
		}
		doc.putMetadata(BillingQueryFields.DISCOUNT_AMOUNT, money(discount.getDiscountAmount()));
		putBillReference(doc, bill, receipt);
	}
	
	private static String describeAmount(BillDiscount discount, boolean hasPercent) {
		if (hasPercent) {
			return plain(discount.getDiscountValue()) + "% (" + money(discount.getDiscountAmount()) + ")";
		}
		return money(discount.getDiscountAmount());
	}
}
