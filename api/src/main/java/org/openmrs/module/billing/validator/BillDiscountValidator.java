/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.validator;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.ModuleSettings;
import org.openmrs.module.billing.api.BillDiscountService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillDiscount;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.DiscountType;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Handler(supports = { BillDiscount.class }, order = 50)
public class BillDiscountValidator implements Validator {
	
	private static final Set<BillStatus> DISCOUNT_ELIGIBLE_STATUSES = EnumSet.of(BillStatus.PENDING, BillStatus.POSTED,
	    BillStatus.ADJUSTED);
	
	@Override
	public boolean supports(@Nonnull Class<?> clazz) {
		return BillDiscount.class.isAssignableFrom(clazz);
	}
	
	@Override
	public void validate(@Nonnull Object target, @Nonnull Errors errors) {
		if (!(target instanceof BillDiscount)) {
			errors.reject("error.general");
			return;
		}
		BillDiscount discount = (BillDiscount) target;
		
		// Discount feature must be enabled at the facility level
		String enabled = Context.getAdministrationService().getGlobalProperty(ModuleSettings.DISCOUNT_ENABLED);
		if (!Boolean.parseBoolean(enabled)) {
			errors.reject("billing.error.discount.featureDisabled");
			return;
		}
		
		Bill bill = discount.getBill();
		if (bill == null) {
			errors.rejectValue("bill", "billing.error.discount.billRequired");
			return;
		}
		
		if (!DISCOUNT_ELIGIBLE_STATUSES.contains(bill.getStatus())) {
			errors.rejectValue("bill", "billing.error.discount.billNotEligible");
		}
		
		BillLineItem lineItem = discount.getLineItem();
		
		// If scoped to a line item, it must belong to the same bill and not be voided
		if (lineItem != null) {
			if (lineItem.getBill() == null || !lineItem.getBill().getId().equals(bill.getId())) {
				errors.rejectValue("lineItem", "billing.error.discount.lineItemNotOnBill");
			} else if (lineItem.getVoided()) {
				errors.rejectValue("lineItem", "billing.error.discount.lineItemVoided");
			}
		}
		
		// Scope rules apply on every save (create AND update). The service is the public
		// contract — restricting these checks to creates would let a Java caller load an
		// existing row, flip lineItem on or off, and persist a state the validator never saw.
		// Self-exclusion (existing.id != incoming.id) keeps a row's own re-save from
		// false-positive matching against itself.
		//   - at most one active discount per scope (one bill-level OR one per line item),
		//   - bill-level and line-scoped discounts cannot coexist on the same bill (avoids
		//     ambiguous totals when bill-level percentages would be computed off the gross
		//     total while line discounts also subtract).
		Integer selfId = discount.getId();
		BillDiscountService discountService = Context.getService(BillDiscountService.class);
		if (lineItem == null) {
			BillDiscount existing = discountService.getBillDiscountByBillId(bill.getId());
			if (existing != null && !existing.getId().equals(selfId)) {
				errors.rejectValue("bill", "billing.error.discount.alreadyExists");
			} else if (hasActiveLineScopedDiscount(bill, selfId)) {
				errors.rejectValue("bill", "billing.error.discount.scopeConflict");
			}
		} else if (lineItem.getId() != null) {
			BillDiscount existing = discountService.getActiveLineItemDiscount(lineItem.getId());
			if (existing != null && !existing.getId().equals(selfId)) {
				errors.rejectValue("lineItem", "billing.error.discount.alreadyExistsForLineItem");
			} else {
				BillDiscount existingBillLevel = discountService.getBillDiscountByBillId(bill.getId());
				if (existingBillLevel != null && !existingBillLevel.getId().equals(selfId)) {
					errors.rejectValue("bill", "billing.error.discount.scopeConflict");
				}
			}
		}
		
		// Discount type is required
		if (discount.getDiscountType() == null) {
			errors.rejectValue("discountType", "billing.error.discount.typeRequired");
		}
		
		// Discount value must be positive and within range for its type. The amount is derived
		// from value at the service layer, so bounding value here is what keeps amount in range.
		BigDecimal value = discount.getDiscountValue();
		if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
			errors.rejectValue("discountValue", "billing.error.discount.valueRequired");
		} else if (discount.getDiscountType() == DiscountType.PERCENTAGE) {
			if (value.compareTo(BigDecimal.valueOf(100)) > 0) {
				errors.rejectValue("discountValue", "billing.error.discount.percentageOutOfRange");
			}
		} else if (discount.getDiscountType() == DiscountType.FIXED_AMOUNT) {
			BigDecimal cap = lineItem != null ? lineItem.getTotal() : bill.getTotal();
			if (cap != null && value.compareTo(cap) > 0) {
				errors.rejectValue("discountValue", lineItem != null ? "billing.error.discount.exceedsLineItemTotal"
				        : "billing.error.discount.exceedsBillTotal");
			}
		}
		
		// Justification is mandatory
		if (StringUtils.isBlank(discount.getJustification())) {
			errors.rejectValue("justification", "billing.error.discount.justificationRequired");
		}
		
		// Initiator is required
		if (discount.getInitiator() == null) {
			errors.rejectValue("initiator", "billing.error.discount.initiatorRequired");
		}
	}

	private boolean hasActiveLineScopedDiscount(Bill bill, Integer excludeId) {
		if (bill.getDiscounts() == null) {
			return false;
		}
		for (BillDiscount d : bill.getDiscounts()) {
			if (d != null && !d.getVoided() && d.getLineItem() != null
			        && (excludeId == null || !excludeId.equals(d.getId()))) {
				return true;
			}
		}
		return false;
	}
}
