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

import java.util.Date;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillDiscountService;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.db.BillDiscountDAO;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillDiscount;
import org.openmrs.module.billing.api.model.DiscountStatus;
import org.openmrs.module.billing.api.util.PrivilegeConstants;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class BillDiscountServiceImpl implements BillDiscountService {
	
	private final BillDiscountDAO billDiscountDAO;
	
	@Override
	@Transactional(readOnly = true)
	public BillDiscount getBillDiscountById(Integer id) {
		return billDiscountDAO.getBillDiscountById(id);
	}
	
	@Override
	@Transactional(readOnly = true)
	public BillDiscount getBillDiscountByUuid(String uuid) {
		return billDiscountDAO.getBillDiscountByUuid(uuid);
	}
	
	@Override
	@Transactional(readOnly = true)
	public BillDiscount getBillDiscountByBillId(Integer billId) {
		return billDiscountDAO.getBillDiscountByBillId(billId);
	}
	
	@Override
	@Transactional(readOnly = true)
	public BillDiscount getActiveLineItemDiscount(Integer lineItemId) {
		return billDiscountDAO.getActiveLineItemDiscount(lineItemId);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<BillDiscount> getDiscountsByBillId(Integer billId) {
		return billDiscountDAO.getDiscountsByBillId(billId);
	}
	
	@Override
	@Transactional
	public BillDiscount saveBillDiscount(BillDiscount billDiscount) {
		BillDiscount saved = billDiscountDAO.saveBillDiscount(billDiscount);
		touchParentBill(saved);
		return saved;
	}
	
	@Override
	@Transactional(readOnly = true)
	public DiscountStatus getStatusById(Integer id) {
		return billDiscountDAO.getStatusById(id);
	}
	
	// Bill.getAmountAfterDiscount() is derived; a discount mutation changes the bill's effective
	// value without touching any bill column, so the parent row stays clean. Re-save to advance
	// dateChanged — the querystore BillIndexingAdvice fires on the resulting BillService.saveBill.
	private void touchParentBill(BillDiscount discount) {
		Integer billId = discount.getBill() == null ? null : discount.getBill().getId();
		if (billId == null) {
			log.error("Saved discount {} has no associated bill; skipping parent bill touch", discount.getUuid());
			return;
		}
		try {
			Context.addProxyPrivilege(PrivilegeConstants.MANAGE_BILLS);
			Bill freshBill = Context.getService(BillService.class).getBill(billId);
			if (freshBill == null) {
				// Bill was concurrently voided/purged between this discount's save and the reload —
				// recoverable race, not a hard failure. warn rather than error so ops dashboards
				// don't page on routine concurrent edits.
				log.warn("Discount {} references bill {} which could not be loaded; parent bill not touched",
				    discount.getUuid(), billId);
				return;
			}
			freshBill.setDateChanged(new Date());
			Context.getService(BillService.class).saveBill(freshBill);
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_BILLS);
		}
	}
}
