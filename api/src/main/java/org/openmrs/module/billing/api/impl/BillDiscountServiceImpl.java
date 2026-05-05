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

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.openmrs.module.billing.api.BillDiscountService;
import org.openmrs.module.billing.api.db.BillDiscountDAO;
import org.openmrs.module.billing.api.model.BillDiscount;
import org.springframework.transaction.annotation.Transactional;

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
		return billDiscountDAO.saveBillDiscount(billDiscount);
	}
}
