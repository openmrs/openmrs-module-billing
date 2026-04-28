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

import java.util.Collections;
import java.util.List;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Order;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.billing.api.BillLineItemService;
import org.openmrs.module.billing.api.db.BillLineItemDAO;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public class BillLineItemServiceImpl extends BaseOpenmrsService implements BillLineItemService {
	
	@Setter
	private BillLineItemDAO billLineItemDAO;
	
	@Override
	@Transactional(readOnly = true)
	public List<Integer> getPersistedLineItemIds(Integer billId) {
		if (billId == null) {
			return Collections.emptyList();
		}
		return billLineItemDAO.getLineItemIdsByBillId(billId);
	}
	
	@Override
	@Transactional(readOnly = true)
	public BillLineItem getBillLineItemByUuid(String uuid) {
		if (StringUtils.isBlank(uuid)) {
			return null;
		}
		return billLineItemDAO.getBillLineItemByUuid(uuid);
	}
	
	@Override
	@Transactional(readOnly = true)
	public BillLineItem getBillLineItemByOrder(Order order) {
		if (order == null) {
			return null;
		}
		return billLineItemDAO.getBillLineItemByOrder(order);
	}
	
	@Override
	@Transactional
	public void voidBillLineItem(BillLineItem lineItem, String voidReason) {
		if (StringUtils.isBlank(voidReason)) {
			throw new IllegalArgumentException("voidReason cannot be null or empty");
		}
		billLineItemDAO.saveBillLineItem(lineItem);
	}
}
