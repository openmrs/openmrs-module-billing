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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for the {@link Bill} class.
 * <p>
 * This validator ensures that a Bill meets the following requirements:
 * </p>
 * <ul>
 * <li>Bill must contain at least one line item</li>
 * <li>Each line item must pass BillLineItemValidator validation</li>
 * <li>Patient, cashier, and cash point must not be null</li>
 * <li>Status must be a valid BillStatus enum value</li>
 * <li>If status is PAID, total payments must be >= total amount</li>
 * <li>Receipt number must be less than 256 characters</li>
 * </ul>
 */
@Handler(supports = { Bill.class }, order = 50)
public class BillValidator implements Validator {
	
	protected final Log log = LogFactory.getLog(BillValidator.class);
	
	@Autowired
	private BillLineItemValidator billLineItemValidator;
	
	// Database field length constraints
	private static final int MAX_RECEIPT_NUMBER_LENGTH = 256;
	
	/**
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @param clazz the class to check support for
	 * @return true if this validator supports the provided class
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return Bill.class.isAssignableFrom(clazz);
	}
	
	/**
	 * Validates the given Bill object
	 * 
	 * @param obj the object to validate
	 * @param errors contextual state about the validation process
	 */
	@Override
	public void validate(Object obj, Errors errors) {
		if (obj == null) {
			log.error("Bill object is null");
			throw new IllegalArgumentException("The Bill object should not be null");
		}
		
		Bill bill = (Bill) obj;
		
		// Validate required entities
		validateRequiredEntities(bill, errors);
		
		// Validate line items exist
		validateLineItemsExist(bill, errors);
		
		// Validate each line item
		validateLineItems(bill, errors);
		
		// Validate status
		validateStatus(bill, errors);
		
		// Validate payment coverage
		validatePaymentCoverage(bill, errors);
		
		// Validate receipt number length
		validateReceiptNumber(bill, errors);
	}
	
	/**
	 * Validates that required entities (patient, cashier, cashPoint) are not null
	 * 
	 * @param bill the Bill to validate
	 * @param errors the Errors object to register validation errors
	 */
	private void validateRequiredEntities(Bill bill, Errors errors) {
		if (bill.getPatient() == null) {
			errors.rejectValue("patient", "billing.bill.patientRequired", "Patient is required");
		}
		
		if (bill.getCashier() == null) {
			errors.rejectValue("cashier", "billing.bill.cashierRequired", "Cashier is required");
		}
		
		if (bill.getCashPoint() == null) {
			errors.rejectValue("cashPoint", "billing.bill.cashPointRequired", "Cash point is required");
		}
	}
	
	/**
	 * Validates that the bill contains at least one line item
	 * 
	 * @param bill the Bill to validate
	 * @param errors the Errors object to register validation errors
	 */
	private void validateLineItemsExist(Bill bill, Errors errors) {
		if (bill.getLineItems() == null || bill.getLineItems().isEmpty()) {
			errors.rejectValue("lineItems", "billing.bill.lineItemsRequired", "Bill must contain at least one line item");
		}
	}
	
	/**
	 * Validates each line item in the bill using BillLineItemValidator
	 * 
	 * @param bill the Bill to validate
	 * @param errors the Errors object to register validation errors
	 */
	private void validateLineItems(Bill bill, Errors errors) {
		if (bill.getLineItems() == null || bill.getLineItems().isEmpty()) {
			return; // Already validated in validateLineItemsExist
		}
		
		for (int i = 0; i < bill.getLineItems().size(); i++) {
			BillLineItem lineItem = bill.getLineItems().get(i);
			if (lineItem != null && !lineItem.getVoided()) {
				try {
					errors.pushNestedPath("lineItems[" + i + "]");
					billLineItemValidator.validate(lineItem, errors);
				}
				finally {
					errors.popNestedPath();
				}
			}
		}
	}
	
	/**
	 * Validates that the bill status is a valid BillStatus enum value
	 * 
	 * @param bill the Bill to validate
	 * @param errors the Errors object to register validation errors
	 */
	private void validateStatus(Bill bill, Errors errors) {
		if (bill.getStatus() == null) {
			errors.rejectValue("status", "billing.bill.statusInvalid", "Invalid bill status");
		}
	}
	
	/**
	 * Validates that if the bill status is PAID, the total payments cover the bill total
	 * 
	 * @param bill the Bill to validate
	 * @param errors the Errors object to register validation errors
	 */
	private void validatePaymentCoverage(Bill bill, Errors errors) {
		if (bill.getStatus() == BillStatus.PAID) {
			BigDecimal totalAmount = bill.getTotal();
			BigDecimal totalPayments = bill.getTotalPayments();
			
			if (totalAmount != null && totalPayments != null) {
				if (totalPayments.compareTo(totalAmount) < 0) {
					errors.rejectValue("payments", "billing.bill.paymentInsufficient",
					    "Payment amount does not cover bill total");
				}
			}
		}
	}
	
	/**
	 * Validates that the receipt number does not exceed the maximum length
	 * 
	 * @param bill the Bill to validate
	 * @param errors the Errors object to register validation errors
	 */
	private void validateReceiptNumber(Bill bill, Errors errors) {
		String receiptNumber = bill.getReceiptNumber();
		if (receiptNumber != null && receiptNumber.length() > MAX_RECEIPT_NUMBER_LENGTH) {
			errors.rejectValue("receiptNumber", "billing.bill.receiptNumberTooLong",
			    "Receipt number exceeds maximum length of " + MAX_RECEIPT_NUMBER_LENGTH + " characters");
		}
	}
	
	/**
	 * Sets the BillLineItemValidator for testing purposes
	 * 
	 * @param billLineItemValidator the validator to inject
	 */
	public void setBillLineItemValidator(BillLineItemValidator billLineItemValidator) {
		this.billLineItemValidator = billLineItemValidator;
	}
}
