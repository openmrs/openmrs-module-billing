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
package org.openmrs.module.billing.api.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.billing.api.BillLineItemService;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.db.BillLineItemDAO;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.springframework.transaction.annotation.Transactional;

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
		if (StringUtils.isEmpty(uuid)) {
			return null;
		}
		return billLineItemDAO.getBillLineItemByUuid(uuid);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public Bill voidBillLineItem(String lineItemUuid, String voidReason) {
		if (StringUtils.isEmpty(lineItemUuid)) {
			throw new IllegalArgumentException("lineItemUuid cannot be null or empty");
		}
		if (StringUtils.isEmpty(voidReason)) {
			throw new IllegalArgumentException("voidReason cannot be null or empty");
		}
		
		BillLineItem lineItem = getBillLineItemByUuid(lineItemUuid);
		if (lineItem == null) {
			throw new IllegalArgumentException("Line item with UUID " + lineItemUuid + " not found");
		}
		
		Bill bill = lineItem.getBill();
		if (bill == null) {
			throw new IllegalStateException("Cannot void a line item without an associated bill.");
		}
		
		// Reload the bill to ensure we have a fully managed entity with all relationships loaded
		BillService billService = Context.getService(BillService.class);
		Bill managedBill = billService.getBill(bill.getId());
		if (managedBill == null) {
			throw new IllegalStateException("Cannot find the bill associated with this line item.");
		}
		
		// Find the line item in the bill's line items collection
		BillLineItem lineItemToVoid = null;
		if (managedBill.getLineItems() != null) {
			for (BillLineItem item : managedBill.getLineItems()) {
				if (item.getUuid().equals(lineItemUuid)) {
					lineItemToVoid = item;
					break;
				}
			}
		}
		
		if (lineItemToVoid == null) {
			throw new IllegalStateException("Line item not found in the bill's line items collection.");
		}
		
		// Set void properties on the line item
		User user = Context.getAuthenticatedUser();
		Date dateVoided = new Date();
		lineItemToVoid.setVoided(true);
		lineItemToVoid.setVoidReason(voidReason);
		lineItemToVoid.setVoidedBy(user);
		lineItemToVoid.setDateVoided(dateVoided);
		
		// Save the bill, which will persist the voided line item
		return billService.saveBill(managedBill);
	}
}
