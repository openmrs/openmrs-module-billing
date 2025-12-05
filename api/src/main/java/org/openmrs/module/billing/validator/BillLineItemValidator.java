package org.openmrs.module.billing.validator;

import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.IBillService;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Handler(supports = { BillLineItem.class }, order = 50)
public class BillLineItemValidator implements Validator {
	
	@Override
	public boolean supports(Class<?> clazz) {
		return BillLineItem.class.isAssignableFrom(clazz);
	}
	
	@Override
	public void validate(Object target, Errors errors) {
		if (!(target instanceof BillLineItem)) {
			throw new IllegalArgumentException("error.general and must be of type " + BillLineItem.class);
		}
		BillLineItem item = (BillLineItem) target;
		if (item.getBill() != null) {
			BillStatus status = Context.getService(IBillService.class).getBillStatus(item.getBill().getUuid());
			if (status != null && status != BillStatus.PENDING && status != BillStatus.POSTED) {
				errors.reject("billing.bill.notEditable",
				    "Bill can only be modified when the bill is in PENDING or POSTED states. Current status: " + status);
			}
		}
	}
	
}
