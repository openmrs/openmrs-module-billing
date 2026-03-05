/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.billing.api.model;

/**
 * Defines the types of auditable actions that can be performed on a bill.
 */
public enum BillAuditAction {
	
	BILL_CREATED,
	LINE_ITEM_ADDED,
	LINE_ITEM_REMOVED,
	LINE_ITEM_MODIFIED,
	QUANTITY_CHANGED,
	PRICE_CHANGED,
	STATUS_CHANGED,
	PAYMENT_ADDED,
	PAYMENT_REMOVED,
	BILL_ADJUSTED,
	ADJUSTMENT_REASON_UPDATED,
	BILL_VOIDED,
	BILL_UNVOIDED
}
