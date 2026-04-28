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
		
		// Uniqueness depends on scope: at most one active discount per scope
		if (discount.getId() == null) {
			BillDiscountService discountService = Context.getService(BillDiscountService.class);
			if (lineItem == null) {
				BillDiscount existing = discountService.getBillDiscountByBillId(bill.getId());
				if (existing != null) {
					errors.rejectValue("bill", "billing.error.discount.alreadyExists");
				}
			} else if (lineItem.getId() != null) {
				BillDiscount existing = discountService.getActiveLineItemDiscount(lineItem.getId());
				if (existing != null) {
					errors.rejectValue("lineItem", "billing.error.discount.alreadyExistsForLineItem");
				}
			}
		}
		
		// Discount type is required
		if (discount.getDiscountType() == null) {
			errors.rejectValue("discountType", "billing.error.discount.typeRequired");
		}
		
		// Discount value must be positive
		if (discount.getDiscountValue() == null || discount.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
			errors.rejectValue("discountValue", "billing.error.discount.valueRequired");
		}
		
		// Discount amount must not exceed the scoped target's total (line item or bill)
		if (discount.getDiscountAmount() != null) {
			BigDecimal cap = lineItem != null ? lineItem.getTotal() : bill.getTotal();
			if (cap != null && discount.getDiscountAmount().compareTo(cap) > 0) {
				errors.rejectValue("discountAmount", lineItem != null ? "billing.error.discount.exceedsLineItemTotal"
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
}
