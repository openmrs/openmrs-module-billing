/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api;

import java.util.List;

import org.openmrs.annotation.Authorized;
import org.openmrs.module.billing.api.model.BillDiscount;
import org.openmrs.module.billing.api.util.PrivilegeConstants;

/**
 * Service for managing {@link BillDiscount} records — both bill-level and line-item-scoped
 * discounts. Writes are gated by {@link PrivilegeConstants#MANAGE_BILL_DISCOUNTS} and reads by
 * {@link PrivilegeConstants#VIEW_BILL_DISCOUNTS}; saves are validated by
 * {@code BillDiscountValidator}, which enforces feature-flag, scope-exclusivity and amount
 * derivation rules.
 */
public interface BillDiscountService {
	
	/**
	 * Looks up a discount by its primary key.
	 *
	 * @param id the bill_discount_id
	 * @return the discount, or {@code null} if no row exists
	 */
	@Authorized(PrivilegeConstants.VIEW_BILL_DISCOUNTS)
	BillDiscount getBillDiscountById(Integer id);
	
	/**
	 * Looks up a discount by UUID.
	 *
	 * @param uuid the discount UUID
	 * @return the discount, or {@code null} if no row matches
	 */
	@Authorized(PrivilegeConstants.VIEW_BILL_DISCOUNTS)
	BillDiscount getBillDiscountByUuid(String uuid);
	
	/**
	 * Returns the active bill-level discount for a bill (i.e. {@code lineItem == null} and
	 * {@code voided == false}). Line-item scoped discounts are excluded — use
	 * {@link #getActiveLineItemDiscount(Integer)} for those.
	 *
	 * @param billId the bill's primary key
	 * @return the active bill-level discount, or {@code null} if none exists
	 */
	@Authorized(PrivilegeConstants.VIEW_BILL_DISCOUNTS)
	BillDiscount getBillDiscountByBillId(Integer billId);
	
	/**
	 * Returns the active discount targeting a specific line item.
	 *
	 * @param lineItemId the bill_line_item_id
	 * @return the active line-scoped discount, or {@code null} if none exists
	 */
	@Authorized(PrivilegeConstants.VIEW_BILL_DISCOUNTS)
	BillDiscount getActiveLineItemDiscount(Integer lineItemId);
	
	/**
	 * Returns the full discount history for a bill — active and voided rows alike — ordered newest
	 * first. Intended for audit-trail surfaces; for the currently effective discount(s) use
	 * {@link #getBillDiscountByBillId(Integer)} or {@link #getActiveLineItemDiscount(Integer)}.
	 *
	 * @param billId the bill's primary key
	 * @return the audit history (possibly empty), never {@code null}
	 */
	@Authorized(PrivilegeConstants.VIEW_BILL_DISCOUNTS)
	List<BillDiscount> getDiscountsByBillId(Integer billId);
	
	/**
	 * Persists a new or existing discount. The {@code discountAmount} field is authoritatively
	 * (re)derived from {@code discountValue} and the scope total before persist — caller-supplied
	 * amounts are not trusted. The validator enforces scope-exclusivity (a bill cannot carry both a
	 * bill-level and any line-scoped discount at once) and per-scope uniqueness.
	 *
	 * @param billDiscount the discount to save
	 * @return the persisted (managed) instance
	 */
	@Authorized(PrivilegeConstants.MANAGE_BILL_DISCOUNTS)
	BillDiscount saveBillDiscount(BillDiscount billDiscount);
}
