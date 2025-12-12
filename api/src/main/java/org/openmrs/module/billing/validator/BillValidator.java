package org.openmrs.module.billing.validator;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.model.Bill;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Handler(supports = { Bill.class }, order = 50)
public class BillValidator implements Validator {
	
	@Override
	public boolean supports(Class<?> clazz) {
		return Bill.class.isAssignableFrom(clazz);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void validate(Object target, Errors errors) {
		if (!(target instanceof Bill)) {
			errors.reject("error.general");
		} else {
			Bill bill = (Bill) target;
			
			if (bill.getVoided() && StringUtils.isBlank(bill.getVoidReason())) {
				errors.rejectValue("voided", "error.null");
			}
			
			if (bill.getId() != null) {
				Bill existingBill = Context.getService(BillService.class).getBill(bill.getBillId());
				if (existingBill != null && !existingBill.editable()) {
					errors.reject("billing.bill.notEditable",
					    "Bill can only be modified when the bill is in PENDING state. Current status: "
					            + existingBill.getStatus());
				}
			}
		}
	}
	
}
