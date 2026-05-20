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
	
	static final String RESOURCE_TYPE_BILL_DISCOUNT = "billing_bill_discount";
	
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
	
	// Shape contract: List<String> on BOTH bill and refund docs. The bill emits one entry per
	// non-voided line item; the refund emits a singleton (refunds are line-scoped). The shared
	// key + uniform list shape lets a single "find every record referencing item X" query span
	// both resource types without type-branching downstream.
	static final String FIELD_LINE_ITEM_NAMES = "line_item_names";
	
	static final String FIELD_INITIATOR_UUID = "initiator_uuid";
	
	static final String FIELD_APPROVER_UUID = "approver_uuid";
	
	static final String FIELD_COMPLETER_UUID = "completer_uuid";
	
	// Distinct non-voided PaymentMode.name values across the bill's payments. Lets ops queries
	// like "settlements by tender type" succeed without scanning every payment row.
	static final String FIELD_PAYMENT_MODES = "payment_modes";
	
	// Distinct non-voided DiscountStatus values on the bill, sorted alphabetically (NOT workflow
	// order — APPROVED comes before PENDING). The aggregate exists for presence queries ("which
	// bills have a pending discount?") — consumers must not treat the list as a timeline; the
	// BillDiscount resource type carries the per-discount detail when ordering matters.
	static final String FIELD_DISCOUNT_STATUSES = "discount_statuses";
	
	// UUID of the bill this one adjusts (if any). Together with FIELD_ADJUSTED_BY_UUIDS, lets a
	// query trace the adjustment chain in either direction.
	static final String FIELD_BILL_ADJUSTED_UUID = "bill_adjusted_uuid";
	
	static final String FIELD_ADJUSTED_BY_UUIDS = "adjusted_by_uuids";
	
	static final String FIELD_ADJUSTMENT_REASON = "adjustment_reason";
	
	static final String FIELD_RECEIPT_PRINTED = "receipt_printed";
	
	// BillDiscount fields. The BillDiscount document is keyed to its parent bill's patient so the
	// "approval queue" question — "show me all pending discounts requiring my review" — is a
	// patient-scoped search per the querystore SPI contract.
	static final String FIELD_DISCOUNT_TYPE = "discount_type";
	
	// Raw input — a percentage (e.g., 15 for "15% off") when discount_type=PERCENTAGE, a money
	// amount when discount_type=FIXED_AMOUNT. Always paired with discount_type to be meaningful.
	static final String FIELD_DISCOUNT_VALUE = "discount_value";
	
	// Computed money figure — the actual currency amount removed from the bill, derived from
	// (value, type, current base). Use this for "find discounts > $50" queries; use discount_value
	// for "find 15% discounts" queries.
	static final String FIELD_DISCOUNT_AMOUNT = "discount_amount";
	
	static final String FIELD_JUSTIFICATION = "justification";
	
	private BillingQueryStoreConstants() {
	}
}
