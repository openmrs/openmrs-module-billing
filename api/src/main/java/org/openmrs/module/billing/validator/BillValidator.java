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
import org.openmrs.module.billing.api.model.BillStatus;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
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

			if (bill.getPatient() == null) {
				errors.rejectValue("patient", "billing.bill.error.patientRequired");
			}

			if (bill.getCashier() == null) {
				errors.rejectValue("cashier", "billing.bill.error.cashierRequired");
			}

			if (bill.getCashPoint() == null) {
				errors.rejectValue("cashPoint", "billing.bill.error.cashPointRequired");
			}

			if (bill.getStatus() == null) {
				errors.rejectValue("status", "billing.bill.error.statusRequired");
			}

			validateLineItems(bill, errors);

			if (bill.getStatus() != null && bill.getStatus() == BillStatus.PAID) {
				if (bill.getTotalPayments().compareTo(bill.getTotal()) < 0) {
					errors.rejectValue("status", "billing.bill.error.insufficientPayments");
				}
			}

			if (bill.getReceiptNumber() != null && bill.getReceiptNumber().length() > 255) {
				errors.rejectValue("receiptNumber", "billing.bill.error.receiptNumberTooLong");
			}

			if (bill.getAdjustmentReason() != null && bill.getAdjustmentReason().length() > 500) {
				errors.rejectValue("adjustmentReason", "billing.bill.error.adjustmentReasonTooLong");
			}
			
			validateLineItemsNotModified(bill, errors);
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

	private void validateLineItems(Bill bill, Errors errors) {
		if (bill.getLineItems() == null || bill.getLineItems().isEmpty()) {
			errors.rejectValue("lineItems", "billing.bill.error.atLeastOneLineItemRequired");
			return;
		}

		boolean hasNonVoidedLineItem = bill.getLineItems().stream().anyMatch(item -> !item.getVoided());
		if (!hasNonVoidedLineItem) {
			errors.rejectValue("lineItems", "billing.bill.error.atLeastOneNonVoidedLineItemRequired");
		}

		for (int i = 0; i < bill.getLineItems().size(); i++) {
			BillLineItem lineItem = bill.getLineItems().get(i);
			if (lineItem != null) {
				try {
					errors.pushNestedPath("lineItems[" + i + "]");
					ValidationUtils.invokeValidator(new BillLineItemValidator(), lineItem, errors);
				} finally {
					errors.popNestedPath();
				}
			}
		}
	}
}
