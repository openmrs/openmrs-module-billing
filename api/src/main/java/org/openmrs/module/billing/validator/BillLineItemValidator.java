/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.billing.validator;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.annotation.Handler;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Validator for bill line item operations. Supports:
 * <ul>
 * <li>{@link VoidRequest} – validation when voiding a line item</li>
 * <li>{@link BillLineItem} – validation when creating or updating a line item (add rules in
 * {@link #validateBillLineItem})</li>
 * </ul>
 */
@Handler(supports = { BillLineItem.class }, order = 50)
public class BillLineItemValidator implements Validator {
	
	@Override
	public boolean supports(@Nonnull Class<?> clazz) {
		return VoidRequest.class.isAssignableFrom(clazz) || BillLineItem.class.isAssignableFrom(clazz);
	}
	
	@Override
	public void validate(@Nonnull Object target, @Nonnull Errors errors) {
		if (target instanceof VoidRequest) {
			validateVoidRequest((VoidRequest) target, errors);
		}
	}
	
	/**
	 * Validates a void request (lineItemUuid and voidReason required).
	 */
	private void validateVoidRequest(VoidRequest request, Errors errors) {
		if (StringUtils.isEmpty(request.getLineItemUuid())) {
			errors.rejectValue("lineItemUuid", "billing.error.lineItemUuidRequired", "lineItemUuid cannot be null or empty");
		}
		if (StringUtils.isEmpty(request.getVoidReason())) {
			errors.rejectValue("voidReason", "billing.error.voidReasonRequired", "voidReason cannot be null or empty");
		}
	}
	
	/**
	 * Request object for voiding a bill line item.
	 */
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class VoidRequest {
		
		private String lineItemUuid;
		
		private String voidReason;
	}
}
