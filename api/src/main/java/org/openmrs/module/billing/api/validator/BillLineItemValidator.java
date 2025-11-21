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
package org.openmrs.module.billing.api.validator;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for the {@link BillLineItem} class.
 * <p>
 * This validator ensures that a BillLineItem meets the following requirements:
 * </p>
 * <ul>
 * <li>At least one of item OR billableService must be set (not both null)</li>
 * <li>Quantity must be greater than 0</li>
 * <li>Price must not be negative</li>
 * <li>Payment status must be either PENDING or PAID</li>
 * <li>Field lengths must comply with database constraints</li>
 * </ul>
 */
@Handler(supports = { BillLineItem.class }, order = 50)
public class BillLineItemValidator implements Validator {
	
	protected final Log log = LogFactory.getLog(BillLineItemValidator.class);
	
	// Database field length constraints
	private static final int MAX_PRICE_NAME_LENGTH = 255;
	
	/**
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @param clazz the class to check support for
	 * @return true if this validator supports the provided class
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return BillLineItem.class.isAssignableFrom(clazz);
	}
	
	/**
	 * Validates the given BillLineItem object
	 * 
	 * @param obj the object to validate
	 * @param errors contextual state about the validation process
	 */
	@Override
	public void validate(Object obj, Errors errors) {
		if (obj == null) {
			log.error("BillLineItem object is null");
			throw new IllegalArgumentException("The BillLineItem object should not be null");
		}
		
		BillLineItem billLineItem = (BillLineItem) obj;
		
		// Validate that at least one of item or billableService is set
		validateItemOrService(billLineItem, errors);
		
		// Validate quantity
		validateQuantity(billLineItem, errors);
		
		// Validate price
		validatePrice(billLineItem, errors);
		
		// Validate payment status
		validatePaymentStatus(billLineItem, errors);
		
		// Validate field lengths
		validateFieldLengths(billLineItem, errors);
	}
	
	/**
	 * Validates that at least one of item or billableService is set
	 * 
	 * @param billLineItem the BillLineItem to validate
	 * @param errors the Errors object to register validation errors
	 */
	private void validateItemOrService(BillLineItem billLineItem, Errors errors) {
		if (billLineItem.getItem() == null && billLineItem.getBillableService() == null) {
			errors.rejectValue("item", "billing.billLineItem.itemOrServiceRequired",
			    "BillLineItem must have either an item or a billable service");
		}
	}
	
	/**
	 * Validates that quantity is greater than 0
	 * 
	 * @param billLineItem the BillLineItem to validate
	 * @param errors the Errors object to register validation errors
	 */
	private void validateQuantity(BillLineItem billLineItem, Errors errors) {
		Integer quantity = billLineItem.getQuantity();
		if (quantity == null || quantity <= 0) {
			errors.rejectValue("quantity", "billing.billLineItem.quantityInvalid", "Quantity must be greater than 0");
		}
	}
	
	/**
	 * Validates that price is not negative
	 * 
	 * @param billLineItem the BillLineItem to validate
	 * @param errors the Errors object to register validation errors
	 */
	private void validatePrice(BillLineItem billLineItem, Errors errors) {
		BigDecimal price = billLineItem.getPrice();
		if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
			errors.rejectValue("price", "billing.billLineItem.priceInvalid", "Price cannot be negative");
		}
	}
	
	/**
	 * Validates that payment status is either PENDING or PAID
	 * 
	 * @param billLineItem the BillLineItem to validate
	 * @param errors the Errors object to register validation errors
	 */
	private void validatePaymentStatus(BillLineItem billLineItem, Errors errors) {
		BillStatus paymentStatus = billLineItem.getPaymentStatus();
		if (paymentStatus != null && paymentStatus != BillStatus.PENDING && paymentStatus != BillStatus.PAID) {
			errors.rejectValue("paymentStatus", "billing.billLineItem.paymentStatusInvalid",
			    "Payment status must be either PENDING or PAID");
		}
	}
	
	/**
	 * Validates field lengths against database constraints
	 * 
	 * @param billLineItem the BillLineItem to validate
	 * @param errors the Errors object to register validation errors
	 */
	private void validateFieldLengths(BillLineItem billLineItem, Errors errors) {
		String priceName = billLineItem.getPriceName();
		if (!StringUtils.isEmpty(priceName) && priceName.length() > MAX_PRICE_NAME_LENGTH) {
			errors.rejectValue("priceName", "billing.billLineItem.fieldTooLong",
			    new Object[] { "priceName", MAX_PRICE_NAME_LENGTH },
			    "Field priceName exceeds maximum length of " + MAX_PRICE_NAME_LENGTH + " characters");
		}
	}
}
