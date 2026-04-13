package org.openmrs.module.billing.validator;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.ModuleSettings;
import org.openmrs.module.billing.api.BillDiscountService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillDiscount;
import org.openmrs.module.billing.api.model.BillStatus;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Handler(supports = { BillDiscount.class }, order = 50)
public class BillDiscountValidator implements Validator {
	
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
		
		// Bill must be PENDING or POSTED (not PAID, REFUNDED, CANCELLED, ADJUSTED, EXEMPTED)
		BillStatus status = bill.getStatus();
		if (status != BillStatus.PENDING && status != BillStatus.POSTED) {
			errors.rejectValue("bill", "billing.error.discount.billNotEligible");
		}
		
		// Cannot apply multiple discounts to the same bill
		if (discount.getId() == null) {
			BillDiscountService discountService = Context.getService(BillDiscountService.class);
			BillDiscount existing = discountService.getBillDiscountByBillId(bill.getId());
			if (existing != null) {
				errors.rejectValue("bill", "billing.error.discount.alreadyExists");
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
		
		// Discount amount must not exceed bill total
		if (discount.getDiscountAmount() != null && bill.getTotal() != null
		        && discount.getDiscountAmount().compareTo(bill.getTotal()) > 0) {
			errors.rejectValue("discountAmount", "billing.error.discount.exceedsBillTotal");
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
