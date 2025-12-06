package org.openmrs.module.billing.validator;

import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.IBillService;
import org.openmrs.module.billing.api.model.Bill;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Handler(supports = { Bill.class }, order = 50)
public class BillValidator implements Validator {
	
	@Override
	public boolean supports(Class<?> clazz) {
		return Bill.class.isAssignableFrom(clazz);
	}
	
	@Override
	public void validate(Object target, Errors errors) {
		if (!(target instanceof Bill)) {
			throw new IllegalArgumentException("error.general and must be of type " + Bill.class);
		}
		Bill bill = (Bill) target;
		if (bill.getId() != null) {
			Bill existingBill = Context.getService(IBillService.class).getById(bill.getId());
			if (existingBill != null && !existingBill.isPending()) {
				errors.reject("billing.bill.notPending",
				    "Bill can only be modified when the bill is in PENDING state. Current status: "
				            + existingBill.getStatus());
			}
		}
	}
	
}
