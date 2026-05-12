/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillRefundService;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.db.BillRefundDAO;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillRefund;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.RefundStatus;
import org.openmrs.module.billing.api.util.PrivilegeConstants;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class BillRefundServiceImpl implements BillRefundService {
	
	private final BillRefundDAO billRefundDAO;
	
	@Override
	@Transactional(readOnly = true)
	public BillRefund getBillRefundById(Integer id) {
		return billRefundDAO.getBillRefundById(id);
	}
	
	@Override
	@Transactional(readOnly = true)
	public BillRefund getBillRefundByUuid(String uuid) {
		return billRefundDAO.getBillRefundByUuid(uuid);
	}
	
	@Override
	@Transactional(readOnly = true)
	public BillRefund getActiveBillRefund(Integer billId) {
		return billRefundDAO.getActiveBillRefund(billId);
	}
	
	@Override
	@Transactional(readOnly = true)
	public BillRefund getActiveLineItemRefund(Integer lineItemId) {
		return billRefundDAO.getActiveLineItemRefund(lineItemId);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<BillRefund> getActiveLineScopedRefunds(Integer billId) {
		return billRefundDAO.getActiveLineScopedRefunds(billId);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<BillRefund> getRefundsByBillId(Integer billId) {
		return billRefundDAO.getRefundsByBillId(billId);
	}
	
	@Override
	@Transactional
	public BillRefund saveBillRefund(BillRefund billRefund) {
		BillRefund saved = billRefundDAO.saveBillRefund(billRefund);
		stampTransitionTimestamps(saved);
		reconcileBillStatus(saved.getBill() == null ? null : saved.getBill().getId());
		return saved;
	}
	
	@Override
	@Transactional(readOnly = true)
	public RefundStatus getStatusById(Integer id) {
		return billRefundDAO.getStatusById(id);
	}
	
	private void stampTransitionTimestamps(BillRefund refund) {
		RefundStatus current = refund.getStatus();
		Date now = new Date();
		if ((current == RefundStatus.APPROVED || current == RefundStatus.REJECTED) && refund.getDateApproved() == null) {
			refund.setDateApproved(now);
		}
		if (current == RefundStatus.COMPLETED && refund.getDateCompleted() == null) {
			refund.setDateCompleted(now);
		}
	}
	
	private void reconcileBillStatus(Integer billId) {
		if (billId == null) {
			return;
		}
		Context.addProxyPrivilege(PrivilegeConstants.MANAGE_BILLS);
		try {
			Bill freshBill = Context.getService(BillService.class).getBill(billId);
			if (freshBill == null) {
				return;
			}
			BillStatus target = deriveBillStatusFromRefunds(billId, freshBill);
			if (freshBill.getStatus() == target) {
				return;
			}
			freshBill.setStatus(target);
			Context.getService(BillService.class).saveBill(freshBill);
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_BILLS);
		}
	}
	
	private BillStatus deriveBillStatusFromRefunds(Integer billId, Bill bill) {
		List<BillRefund> history = billRefundDAO.getRefundsByBillId(billId).stream()
		        .filter(r -> !Boolean.TRUE.equals(r.getVoided())).collect(java.util.stream.Collectors.toList());
		
		if (history.stream().anyMatch(r -> r.getStatus() == RefundStatus.REQUESTED)) {
			return BillStatus.REFUND_REQUESTED;
		}
		
		BigDecimal totalRefunded = history.stream()
		        .filter(r -> r.getStatus() == RefundStatus.APPROVED || r.getStatus() == RefundStatus.COMPLETED)
		        .map(BillRefund::getRefundAmount).filter(a -> a != null).reduce(BigDecimal.ZERO, BigDecimal::add);
		
		if (totalRefunded.compareTo(BigDecimal.ZERO) == 0) {
			return BillStatus.PAID;
		}
		BigDecimal billTotal = bill.getAmountAfterDiscount();
		if (billTotal != null && totalRefunded.compareTo(billTotal) >= 0) {
			return BillStatus.REFUNDED;
		}
		return BillStatus.PARTIALLY_REFUNDED;
	}
}
