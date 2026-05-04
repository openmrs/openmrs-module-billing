/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillLineItemService;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.Payment;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Handler(supports = { Bill.class }, order = 50)
public class BillValidator implements Validator {
	
	@Override
	public boolean supports(@Nonnull Class<?> clazz) {
		return Bill.class.isAssignableFrom(clazz);
	}
	
	@Override
	public void validate(@Nonnull Object target, @Nonnull Errors errors) {
		if (!(target instanceof Bill)) {
			errors.reject("error.general");
		} else {
			Bill bill = (Bill) target;
			
			if (bill.getVoided() && StringUtils.isBlank(bill.getVoidReason())) {
				errors.rejectValue("voided", "error.null");
			}
			
			validateNewPaymentsHaveCashier(bill, errors);
			validateLineItemsNotModified(bill, errors);
			validateRefundFields(bill, errors);
		}
	}
	
	/**
	 * Validates that line items have not been added or removed from a non-editable bill. This check is
	 * necessary because the ImmutableBillInterceptor cannot detect collection changes due to
	 * Hibernate's inverse="true" mapping on the lineItems collection.
	 *
	 * @param bill the bill to validate
	 * @param errors the errors object to add validation errors to
	 */
	private void validateLineItemsNotModified(Bill bill, Errors errors) {
		// Only check existing bills that are not editable
		if (bill.getId() == null || bill.editable()) {
			return;
		}
		
		// Get the original line item IDs from the database
		BillLineItemService billLineItemService = Context.getService(BillLineItemService.class);
		List<Integer> originalLineItemIds = billLineItemService.getPersistedLineItemIds(bill.getId());
		
		Set<Integer> originalIds = new HashSet<>(originalLineItemIds);
		
		// Get current line item IDs (only those that have been persisted, i.e., have an ID)
		Set<Integer> currentIds = new HashSet<>();
		if (bill.getLineItems() != null) {
			currentIds = bill.getLineItems().stream().map(BillLineItem::getId).filter(id -> id != null)
			        .collect(Collectors.toSet());
		}
		
		// Check if any line items were removed
		Set<Integer> removedIds = new HashSet<>(originalIds);
		removedIds.removeAll(currentIds);
		if (!removedIds.isEmpty()) {
			errors.reject("billing.error.lineItemsCannotBeRemovedFromNonPendingBill");
		}
		
		// Check if any new line items were added (new items have null ID)
		if (bill.getLineItems() != null) {
			boolean hasNewLineItems = bill.getLineItems().stream().anyMatch(item -> item.getId() == null);
			if (hasNewLineItems) {
				errors.reject("billing.error.lineItemsCannotBeAddedToNonPendingBill");
			}
		}
	}
	
	/**
	 * Validates refund-related fields and that the persisted status is a valid predecessor for the
	 * requested refund-workflow target. Reads the persisted status via
	 * {@link BillService#getPersistedBillStatus(Integer)} (bypasses the Hibernate session cache)
	 * because {@code BillResource.setBillStatus} and the refund service methods mutate the in-memory
	 * status to the target before validation runs — so the managed entity's status is not usable as the
	 * source of truth here.
	 */
	private void validateRefundFields(Bill bill, Errors errors) {
		if (bill.getId() == null || bill.getStatus() == null) {
			return;
		}
		
		BillStatus requiredSource;
		if (bill.getStatus() == BillStatus.REFUND_REQUESTED) {
			requiredSource = BillStatus.PAID;
		} else if (bill.getStatus() == BillStatus.REFUNDED || bill.getStatus() == BillStatus.REFUND_DENIED) {
			requiredSource = BillStatus.REFUND_REQUESTED;
		} else {
			return;
		}
		BillStatus persisted = Context.getService(BillService.class).getPersistedBillStatus(bill.getId());
		if (persisted != requiredSource) {
			errors.reject("billing.error.invalidBillStatusTransition");
		}
		
		if (bill.getStatus() == BillStatus.REFUND_REQUESTED && StringUtils.isBlank(bill.getRefundReason())) {
			errors.reject("billing.error.refundReasonRequired");
		}
		if (bill.getStatus() == BillStatus.REFUND_DENIED && StringUtils.isBlank(bill.getRefundDenialReason())) {
			errors.reject("billing.error.denialReasonRequired");
		}
	}
	
	/**
	 * Validates that any new (unsaved) non-voided payment has a cashier. Existing persisted payments
	 * (id != null) are exempt to allow legacy data.
	 */
	private void validateNewPaymentsHaveCashier(Bill bill, Errors errors) {
		if (bill.getPayments() == null) {
			return;
		}
		for (Payment payment : bill.getPayments()) {
			if (payment != null && !payment.getVoided() && payment.getId() == null && payment.getCashier() == null) {
				errors.reject("billing.error.paymentCashierRequired");
				return;
			}
		}
	}
	
}
