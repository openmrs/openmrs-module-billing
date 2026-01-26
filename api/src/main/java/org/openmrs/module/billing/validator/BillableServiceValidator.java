package org.openmrs.module.billing.validator;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillableServiceService;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.search.BillableServiceSearch;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;

@Handler(supports = { BillableService.class }, order = 50)
public class BillableServiceValidator implements Validator {
	
	@Override
	public boolean supports(Class<?> clazz) {
		return BillableService.class.isAssignableFrom(clazz);
	}
	
	@Override
	public void validate(Object target, Errors errors) {
		if (!(target instanceof BillableService)) {
			return;
		}
		
		BillableService billableService = (BillableService) target;
		BillableServiceService service = Context.getService(BillableServiceService.class);
		
		// Validate Name Uniqueness
		if (StringUtils.isNotBlank(billableService.getName())) {
			BillableServiceSearch search = new BillableServiceSearch();
			search.setName(billableService.getName());
			search.setIncludeRetired(false);
			
			List<BillableService> existingServices = service.getBillableServices(search, null);
			
			for (BillableService existing : existingServices) {
				if (!existing.getId().equals(billableService.getId())
				        && existing.getName().equalsIgnoreCase(billableService.getName())) {
					errors.rejectValue("name", "billing.error.name.duplicate", "The name you have entered already exists");
					break;
				}
			}
		}
		
		// Validate Short Name Uniqueness
		if (StringUtils.isNotBlank(billableService.getShortName())) {
			BillableServiceSearch search = new BillableServiceSearch();
			search.setShortName(billableService.getShortName());
			search.setIncludeRetired(false);
			
			List<BillableService> existingServices = service.getBillableServices(search, null);
			
			for (BillableService existing : existingServices) {
				if (!existing.getId().equals(billableService.getId())
				        && existing.getShortName().equalsIgnoreCase(billableService.getShortName())) {
					errors.rejectValue("shortName", "billing.error.shortName.duplicate",
					    "The short name you have entered already exists");
					break;
				}
			}
		}
	}
}
