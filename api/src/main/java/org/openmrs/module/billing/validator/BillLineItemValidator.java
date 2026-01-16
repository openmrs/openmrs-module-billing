package org.openmrs.module.billing.validator;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.annotation.Handler;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Handler(supports = { BillLineItem.class }, order = 50)
public class BillLineItemValidator implements Validator {

    @Override
    public boolean supports(@Nonnull Class<?> clazz) {
        return BillLineItem.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@Nonnull Object target, @Nonnull Errors errors) {
        if (!(target instanceof BillLineItem)) {
            errors.reject("error.general");
            return;
        }

        BillLineItem lineItem = (BillLineItem) target;

        if (lineItem.getVoided() && StringUtils.isBlank(lineItem.getVoidReason())) {
            errors.rejectValue("voidReason", "error.null");
        }

        if (lineItem.getItem() == null && lineItem.getBillableService() == null) {
            errors.rejectValue("item", "billing.lineItem.error.itemOrServiceRequired");
        }

        if (lineItem.getQuantity() == null || lineItem.getQuantity() <= 0) {
            errors.rejectValue("quantity", "billing.lineItem.error.quantityMustBePositive");
        }

        if (lineItem.getPrice() == null || lineItem.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            errors.rejectValue("price", "billing.lineItem.error.priceCannotBeNegative");
        }

        if (lineItem.getPaymentStatus() == null) {
            errors.rejectValue("paymentStatus", "billing.lineItem.error.paymentStatusRequired");
        } else if (lineItem.getPaymentStatus() != BillStatus.PENDING
                && lineItem.getPaymentStatus() != BillStatus.PAID) {
            errors.rejectValue("paymentStatus", "billing.lineItem.error.invalidPaymentStatus");
        }

        if (lineItem.getPriceName() != null && lineItem.getPriceName().length() > 255) {
            errors.rejectValue("priceName", "billing.lineItem.error.priceNameTooLong");
        }
    }
}
