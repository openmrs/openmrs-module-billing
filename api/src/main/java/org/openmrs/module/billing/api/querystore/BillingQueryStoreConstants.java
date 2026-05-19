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

// Resource-type names and document field names that appear in BOTH the serializer (QueryDocument
// metadata writes) AND any future consumer (querystore queries, dashboards, integrations). A typo
// on either side of that boundary silently breaks indexing of the renamed field — the schema is
// self-healing, so a misspelled key just creates a parallel column nobody queries against.
// Per the ADR, field names are part of the public contract; a rename is a re-index event.
final class BillingQueryStoreConstants {
	
	static final String RESOURCE_TYPE_BILL = "billing_bill";
	
	static final String RESOURCE_TYPE_BILL_REFUND = "billing_bill_refund";
	
	static final String FIELD_RECEIPT_NUMBER = "receipt_number";
	
	static final String FIELD_BILL_UUID = "bill_uuid";
	
	static final String FIELD_STATUS = "status";
	
	static final String FIELD_VOIDED = "voided";
	
	static final String FIELD_TOTAL = "total";
	
	static final String FIELD_AMOUNT_AFTER_DISCOUNT = "amount_after_discount";
	
	static final String FIELD_TOTAL_PAID = "total_paid";
	
	static final String FIELD_BALANCE = "balance";
	
	static final String FIELD_CASHIER_UUID = "cashier_uuid";
	
	static final String FIELD_CASH_POINT_UUID = "cash_point_uuid";
	
	static final String FIELD_CASH_POINT_NAME = "cash_point_name";
	
	static final String FIELD_VISIT_UUID = "visit_uuid";
	
	static final String FIELD_REFUND_AMOUNT = "refund_amount";
	
	static final String FIELD_REASON = "reason";
	
	static final String FIELD_DATE_APPROVED = "date_approved";
	
	static final String FIELD_DATE_COMPLETED = "date_completed";
	
	static final String FIELD_BILL_LINE_ITEM_UUID = "bill_line_item_uuid";
	
	static final String FIELD_INITIATOR_UUID = "initiator_uuid";
	
	static final String FIELD_APPROVER_UUID = "approver_uuid";
	
	static final String FIELD_COMPLETER_UUID = "completer_uuid";
	
	private BillingQueryStoreConstants() {
	}
}
