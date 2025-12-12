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
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.base.entity.impl.BaseEntityDataServiceImpl;
import org.openmrs.module.billing.api.base.entity.security.IEntityAuthorizationPrivileges;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class BillLineItemServiceImpl extends BaseEntityDataServiceImpl<BillLineItem> implements IEntityAuthorizationPrivileges, BillLineItemService {
	
	@Override
	protected IEntityAuthorizationPrivileges getPrivileges() {
		return this;
	}
	
	@Override
	protected void validate(BillLineItem object) {
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
	
	@Override
	public BillLineItem voidEntity(BillLineItem entity, String reason) {
		BillLineItem voidedLineItem = super.voidEntity(entity, reason);
		
		if (voidedLineItem != null && voidedLineItem.getBill() != null) {
			Bill bill = voidedLineItem.getBill();
			bill.synchronizeBillStatus();
		}
		
		return voidedLineItem;
	}
	
	@Override
	public BillLineItem unvoidEntity(BillLineItem entity) {
		BillLineItem unvoidedLineItem = super.unvoidEntity(entity);
		
		if (unvoidedLineItem != null && unvoidedLineItem.getBill() != null) {
			Bill bill = unvoidedLineItem.getBill();
			bill.synchronizeBillStatus();
		}
		
		return unvoidedLineItem;
	}
	
	@Override
	public void purge(BillLineItem entity) {
		Bill bill = null;
		if (entity != null && entity.getBill() != null) {
			bill = entity.getBill();
			// Validate before purging (purge doesn't call validate())
		}
		
		super.purge(entity);
		
		if (bill != null) {
			// Remove the line item from the bill's collection
			bill.removeLineItem(entity);
			bill.synchronizeBillStatus();
			// Save the bill to persist the collection change
			BillService billService = Context.getService(BillService.class);
			billService.saveBill(bill);
		}
	}
}
