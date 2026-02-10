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
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
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
			
			validateLineItemsNotModified(bill, errors);
			validatePaymentsHaveCashier(bill, errors);
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
	 * Validates that all non-voided payments on the bill have an associated cashier (Provider). This
	 * ensures accountability by tracking which cashier processed each payment.
	 *
	 * @param bill the bill whose payments to validate
	 * @param errors the errors object to add validation errors to
	 */
	private void validatePaymentsHaveCashier(Bill bill, Errors errors) {
		if (bill.getPayments() == null) {
			return;
		}
		for (Payment payment : bill.getPayments()) {
			if (payment != null && !payment.getVoided() && payment.getCashier() == null) {
				errors.reject("billing.payment.error.cashierRequired", "Each payment must have an associated cashier");
				return;
			}
		}
	}
	
}
