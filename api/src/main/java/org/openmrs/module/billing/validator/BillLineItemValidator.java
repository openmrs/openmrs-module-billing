package org.openmrs.module.billing.validator;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillLineItemService;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Handler(supports = { BillLineItem.class }, order = 50)
public class BillLineItemValidator implements Validator {
	
	@Override
	public boolean supports(Class<?> clazz) {
		return BillLineItem.class.isAssignableFrom(clazz);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void validate(Object target, Errors errors) {
		if (!(target instanceof BillLineItem)) {
			errors.reject("error.general");
		}
		
		BillLineItem billLineItem = (BillLineItem) target;
		
		if (billLineItem.getVoided() && StringUtils.isBlank(billLineItem.getVoidReason())) {
			errors.rejectValue("voided", "error.null");
		}
		
		if (billLineItem.getId() != null) {
			BillLineItem exitingItem = Context.getService(BillLineItemService.class)
			        .getBillLineItemByUuid(billLineItem.getUuid());
			if (exitingItem != null && billLineItem.getPaymentStatus() != BillStatus.PENDING) {
				errors.reject("billing.lineItem.notEditable",
				    "BillLineItem can only be modified when the BillLineITem is in PENDING state. Current status: "
				            + exitingItem.getPaymentStatus());
			}
		}
	}
}
