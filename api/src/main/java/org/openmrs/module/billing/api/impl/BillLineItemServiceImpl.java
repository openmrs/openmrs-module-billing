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

import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillLineItemService;
import org.openmrs.module.billing.api.IBillableItemsService;
import org.openmrs.module.billing.api.base.entity.impl.BaseEntityDataServiceImpl;
import org.openmrs.module.billing.api.base.entity.security.IEntityAuthorizationPrivileges;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.stockmanagement.api.StockManagementService;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class BillLineItemServiceImpl extends BaseEntityDataServiceImpl<BillLineItem> implements IEntityAuthorizationPrivileges, BillLineItemService {
	
	@Override
	protected IEntityAuthorizationPrivileges getPrivileges() {
		return this;
	}
	
	@Override
	protected void validate(BillLineItem object) {
		if (object == null) {
			throw new IllegalArgumentException("The bill line item must be defined.");
		}
		
		// Validate that if item is set, it exists in the database
		if (object.getItem() != null) {
			StockManagementService stockService = Context.getService(StockManagementService.class);
			if (object.getItem().getUuid() != null) {
				if (stockService.getStockItemByUuid(object.getItem().getUuid()) == null) {
					throw new IllegalArgumentException(
					        "A stock item with the given uuid does not exist: " + object.getItem().getUuid());
				}
			}
		}
		
		// Validate that if billableService is set, it exists in the database
		if (object.getBillableService() != null) {
			IBillableItemsService billableService = Context.getService(IBillableItemsService.class);
			if (object.getBillableService().getUuid() != null) {
				if (billableService.getByUuid(object.getBillableService().getUuid()) == null) {
					throw new IllegalArgumentException("A billable service with the given uuid does not exist: "
					        + object.getBillableService().getUuid());
				}
			}
		}
	}
	
	@Override
	public String getVoidPrivilege() {
		return null;
	}
	
	@Override
	public String getSavePrivilege() {
		return null;
	}
	
	@Override
	public String getPurgePrivilege() {
		return null;
	}
	
	@Override
	public String getGetPrivilege() {
		return null;
	}
}
