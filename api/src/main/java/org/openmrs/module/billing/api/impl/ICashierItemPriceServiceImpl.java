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

import org.openmrs.module.billing.api.ICashierItemPriceService;
import org.openmrs.module.billing.api.base.entity.impl.BaseEntityDataServiceImpl;
import org.openmrs.module.billing.api.base.entity.security.IEntityAuthorizationPrivileges;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ICashierItemPriceServiceImpl extends BaseEntityDataServiceImpl<CashierItemPrice> implements IEntityAuthorizationPrivileges, ICashierItemPriceService {
	
	@Override
	protected IEntityAuthorizationPrivileges getPrivileges() {
		return this;
	}
	
	@Override
	protected void validate(CashierItemPrice object) {
		
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
