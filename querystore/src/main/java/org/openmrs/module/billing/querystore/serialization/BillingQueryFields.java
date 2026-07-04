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

/**
 * Single source of truth for the billing-specific {@code metadata} field names written to
 * {@link org.openmrs.module.querystore.model.QueryDocument}. Keeping them here (rather than as raw
 * literals scattered across the serializers) stops the shared keys - {@link #RECEIPT_NUMBER},
 * {@link #BILL_UUID} - from drifting between the bill, discount and refund documents.
 * <p>
 * Cross-cutting field names that querystore itself defines (e.g. {@code visit_uuid}) are taken from
 * {@code QueryStoreConstants}, not redefined here.
 */
final class BillingQueryFields {
	
	private BillingQueryFields() {
	}
	
	// Shared across bill / discount / refund documents.
	static final String RECEIPT_NUMBER = "receipt_number";
	
	static final String BILL_UUID = "bill_uuid";
	
	// billing_bill
	static final String BILL_STATUS = "bill_status";
	
	static final String TOTAL = "total";
	
	static final String AMOUNT_PAID = "amount_paid";
	
	static final String CASH_POINT = "cash_point";
	
	static final String SERVICES = "services";
	
	static final String PAYMENT_MODES = "payment_modes";
	
	static final String BILLED_SERVICE_COUNT = "billed_service_count";
	
	// billing_discount
	static final String DISCOUNT_STATUS = "discount_status";
	
	static final String DISCOUNT_TYPE = "discount_type";
	
	static final String DISCOUNT_PERCENT = "discount_percent";
	
	static final String DISCOUNT_AMOUNT = "discount_amount";
	
	// billing_refund
	static final String REFUND_STATUS = "refund_status";
	
	static final String REFUND_AMOUNT = "refund_amount";
}
