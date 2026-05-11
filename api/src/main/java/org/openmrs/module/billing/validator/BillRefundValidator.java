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

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.ModuleSettings;
import org.openmrs.module.billing.api.BillRefundService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillRefund;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.RefundStatus;
import org.openmrs.module.billing.api.util.PrivilegeConstants;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Handler(supports = { BillRefund.class }, order = 50)
public class BillRefundValidator implements Validator {
	
	private static final Set<BillStatus> REFUND_ELIGIBLE_NEW_BILL_LEVEL = EnumSet.of(BillStatus.PAID,
	    BillStatus.PARTIALLY_REFUNDED);
	
	private static final Set<BillStatus> REFUND_ELIGIBLE_NEW_LINE_SCOPED = EnumSet.of(BillStatus.PAID,
	    BillStatus.REFUND_REQUESTED, BillStatus.PARTIALLY_REFUNDED);
	
	private static final Set<BillStatus> REFUND_ELIGIBLE_UPDATE = EnumSet.of(BillStatus.PAID, BillStatus.REFUND_REQUESTED,
	    BillStatus.REFUNDED, BillStatus.PARTIALLY_REFUNDED);
	
	@Override
	public boolean supports(@Nonnull Class<?> clazz) {
		return BillRefund.class.isAssignableFrom(clazz);
	}
	
	@Override
	public void validate(@Nonnull Object target, @Nonnull Errors errors) {
		if (!(target instanceof BillRefund)) {
			errors.reject("error.general");
			return;
		}
		BillRefund refund = (BillRefund) target;
		
		// Voiding a finalized refund (APPROVED/REJECTED/COMPLETED) requires APPROVE_REFUNDS.
		if (refund.getId() != null && Boolean.TRUE.equals(refund.getVoided())) {
			if (StringUtils.isBlank(refund.getVoidReason())) {
				errors.reject("billing.error.refund.voidReasonRequired");
			}
			RefundStatus current = refund.getStatus();
			if ((current == RefundStatus.APPROVED || current == RefundStatus.REJECTED || current == RefundStatus.COMPLETED)
			        && !Context.hasPrivilege(PrivilegeConstants.APPROVE_REFUNDS)) {
				errors.reject("billing.error.refund.approvePrivilegeRequired");
			}
			return;
		}
		
		String enabled = Context.getAdministrationService().getGlobalProperty(ModuleSettings.REFUND_ENABLED);
		if (!Boolean.parseBoolean(enabled)) {
			errors.reject("billing.error.refund.featureDisabled");
			return;
		}
		
		Bill bill = refund.getBill();
		if (bill == null) {
			errors.rejectValue("bill", "billing.error.refund.billRequired");
			return;
		}
		
		boolean isNew = refund.getId() == null;
		BillLineItem lineItem = refund.getLineItem();
		Set<BillStatus> eligible;
		if (!isNew) {
			eligible = REFUND_ELIGIBLE_UPDATE;
		} else if (lineItem == null) {
			eligible = REFUND_ELIGIBLE_NEW_BILL_LEVEL;
		} else {
			eligible = REFUND_ELIGIBLE_NEW_LINE_SCOPED;
		}
		if (!eligible.contains(bill.getStatus())) {
			errors.rejectValue("bill", "billing.error.refund.billNotEligible");
		}
		if (lineItem != null) {
			if (lineItem.getBill() == null || !lineItem.getBill().getId().equals(bill.getId())) {
				errors.rejectValue("lineItem", "billing.error.refund.lineItemNotOnBill");
			} else if (Boolean.TRUE.equals(lineItem.getVoided())) {
				errors.rejectValue("lineItem", "billing.error.refund.lineItemVoided");
			}
		}
		
		Integer selfId = refund.getId();
		BillRefundService refundService = Context.getService(BillRefundService.class);
		if (lineItem == null) {
			BillRefund existing = refundService.getActiveBillRefund(bill.getId());
			if (existing != null && !existing.getId().equals(selfId)) {
				errors.rejectValue("bill", "billing.error.refund.alreadyExists");
			} else if (hasActiveLineScopedRefund(refundService, bill.getId(), selfId)) {
				errors.rejectValue("bill", "billing.error.refund.scopeConflict");
			}
		} else if (lineItem.getId() != null) {
			BillRefund existing = refundService.getActiveLineItemRefund(lineItem.getId());
			if (existing != null && !existing.getId().equals(selfId)) {
				errors.rejectValue("lineItem", "billing.error.refund.alreadyExistsForLineItem");
			} else {
				BillRefund existingBillLevel = refundService.getActiveBillRefund(bill.getId());
				if (existingBillLevel != null && !existingBillLevel.getId().equals(selfId)) {
					errors.rejectValue("bill", "billing.error.refund.scopeConflict");
				}
			}
		}
		
		BigDecimal amount = refund.getRefundAmount();
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			errors.rejectValue("refundAmount", "billing.error.refund.amountRequired");
		} else if (amount.stripTrailingZeros().scale() > 2) {
			errors.rejectValue("refundAmount", "billing.error.refund.amountScale");
		} else if (lineItem != null && exceedsLineItemRemaining(refundService, bill.getId(), lineItem, amount, selfId)) {
			errors.rejectValue("refundAmount", "billing.error.refund.exceedsLineItemTotal");
		} else if (lineItem == null && bill.getTotal() != null && amount.compareTo(bill.getTotal()) > 0) {
			errors.rejectValue("refundAmount", "billing.error.refund.exceedsBillTotal");
		} else if (exceedsRemainingRefundable(refundService, bill, amount, selfId)) {
			errors.rejectValue("refundAmount", "billing.error.refund.exceedsRemainingRefundable");
		}
		
		if (StringUtils.isBlank(refund.getReason())) {
			errors.rejectValue("reason", "billing.error.refund.reasonRequired");
		}
		
		if (refund.getInitiator() == null) {
			errors.rejectValue("initiator", "billing.error.refund.initiatorRequired");
		}
		
		validateStatusTransition(refund, errors);
	}
	
	private void validateStatusTransition(BillRefund refund, Errors errors) {
		RefundStatus status = refund.getStatus();
		if (status == null) {
			errors.rejectValue("status", "billing.error.refund.statusRequired");
			return;
		}
		
		RefundStatus previous;
		if (refund.getId() == null) {
			if (status != RefundStatus.REQUESTED) {
				errors.rejectValue("status", "billing.error.refund.initialStatusInvalid");
			}
			return;
		}
		
		previous = Context.getService(BillRefundService.class).getStatusById(refund.getId());
		if (previous == null) {
			errors.rejectValue("status", "billing.error.refund.notFound");
			return;
		}
		
		if (previous == status) {
			return;
		}
		
		boolean allowed = false;
		String requiredPrivilege = null;
		if (previous == RefundStatus.REQUESTED && (status == RefundStatus.APPROVED || status == RefundStatus.REJECTED)) {
			allowed = true;
			requiredPrivilege = PrivilegeConstants.APPROVE_REFUNDS;
		} else if (previous == RefundStatus.APPROVED && status == RefundStatus.COMPLETED) {
			allowed = true;
			requiredPrivilege = PrivilegeConstants.COMPLETE_REFUNDS;
		}
		
		if (!allowed) {
			errors.rejectValue("status", "billing.error.refund.transitionNotAllowed");
			return;
		}
		
		if (!Context.hasPrivilege(requiredPrivilege)) {
			errors.rejectValue("status", "billing.error.refund.transitionPrivilegeRequired");
		}
		
		if (status == RefundStatus.APPROVED || status == RefundStatus.REJECTED) {
			if (refund.getApprover() == null) {
				errors.rejectValue("approver", "billing.error.refund.approverRequired");
			}
		}
		if (status == RefundStatus.COMPLETED && refund.getCompleter() == null) {
			errors.rejectValue("completer", "billing.error.refund.completerRequired");
		}
	}
	
	private boolean hasActiveLineScopedRefund(BillRefundService refundService, Integer billId, Integer excludeId) {
		return refundService.getActiveLineScopedRefunds(billId).stream()
		        .anyMatch(r -> excludeId == null || !excludeId.equals(r.getId()));
	}
	
	private boolean exceedsRemainingRefundable(BillRefundService refundService, Bill bill, BigDecimal proposed,
	        Integer excludeId) {
		BigDecimal alreadyCommitted = refundService.getRefundsByBillId(bill.getId()).stream()
		        .filter(r -> !Boolean.TRUE.equals(r.getVoided()))
		        .filter(r -> r.getStatus() == RefundStatus.REQUESTED || r.getStatus() == RefundStatus.APPROVED
		                || r.getStatus() == RefundStatus.COMPLETED)
		        .filter(r -> excludeId == null || !excludeId.equals(r.getId())).map(BillRefund::getRefundAmount)
		        .filter(a -> a != null).reduce(BigDecimal.ZERO, BigDecimal::add);
		return alreadyCommitted.add(proposed).compareTo(bill.getAmountAfterDiscount()) > 0;
	}
	
	private boolean exceedsLineItemRemaining(BillRefundService refundService, Integer billId, BillLineItem lineItem,
	        BigDecimal proposed, Integer excludeId) {
		BigDecimal lineTotal = lineItem.getTotal();
		if (lineTotal == null) {
			return false;
		}
		BigDecimal priorOnLine = refundService.getRefundsByBillId(billId).stream()
		        .filter(r -> !Boolean.TRUE.equals(r.getVoided()))
		        .filter(r -> r.getStatus() == RefundStatus.REQUESTED || r.getStatus() == RefundStatus.APPROVED
		                || r.getStatus() == RefundStatus.COMPLETED)
		        .filter(r -> r.getLineItem() != null && r.getLineItem().getId().equals(lineItem.getId()))
		        .filter(r -> excludeId == null || !excludeId.equals(r.getId())).map(BillRefund::getRefundAmount)
		        .filter(a -> a != null).reduce(BigDecimal.ZERO, BigDecimal::add);
		return priorOnLine.add(proposed).compareTo(lineTotal) > 0;
	}
}
