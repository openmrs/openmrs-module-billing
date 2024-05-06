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

import java.util.List;

import org.hibernate.Criteria;
import org.openmrs.module.billing.api.IBillableItemsService;
import org.openmrs.module.billing.api.base.entity.impl.BaseEntityDataServiceImpl;
import org.openmrs.module.billing.api.base.entity.security.IEntityAuthorizationPrivileges;
import org.openmrs.module.billing.api.base.f.Action1;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.search.BillableServiceSearch;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class BillableItemsServiceImpl extends BaseEntityDataServiceImpl<BillableService> implements IEntityAuthorizationPrivileges, IBillableItemsService {
	
	@Override
	public List<BillableService> findServices(final BillableServiceSearch serviceSearch) {
		return executeCriteria(BillableService.class, null, new Action1<Criteria>() {
			
			@Override
			public void apply(Criteria criteria) {
				serviceSearch.updateCriteria(criteria);
			}
		});
	}
	
	@Override
	protected IEntityAuthorizationPrivileges getPrivileges() {
		return this;
	}
	
	@Override
	protected void validate(BillableService object) {
		
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
