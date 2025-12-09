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

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Concept;
import org.openmrs.api.APIException;
import org.openmrs.module.billing.api.IBillableItemsService;
import org.openmrs.module.billing.api.base.entity.impl.BaseMetadataDataServiceImpl;
import org.openmrs.module.billing.api.base.entity.security.IMetadataAuthorizationPrivileges;
import org.openmrs.module.billing.api.base.f.Action1;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.search.BillableServiceSearch;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class BillableItemsServiceImpl extends BaseMetadataDataServiceImpl<BillableService> implements IMetadataAuthorizationPrivileges, IBillableItemsService {
	
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
	@Transactional(readOnly = true)
	public BillableService findByName(final String name) {
		if (StringUtils.isBlank(name)) {
			return null;
		}
		
		List<BillableService> results = executeCriteria(BillableService.class, null, new Action1<Criteria>() {
			@Override
			public void apply(Criteria criteria) {
				criteria.add(Restrictions.eq("name", name).ignoreCase());
				criteria.add(Restrictions.eq("retired", false));
			}
		});
		
		return results.isEmpty() ? null : results.get(0);
	}
	
	@Override
	@Transactional(readOnly = true)
	public BillableService findByShortName(final String shortName) {
		if (StringUtils.isBlank(shortName)) {
			return null;
		}
		
		List<BillableService> results = executeCriteria(BillableService.class, null, new Action1<Criteria>() {
			@Override
			public void apply(Criteria criteria) {
				criteria.add(Restrictions.eq("shortName", shortName).ignoreCase());
				criteria.add(Restrictions.eq("retired", false));
			}
		});
		
		return results.isEmpty() ? null : results.get(0);
	}
	
	@Override
	@Transactional(readOnly = true)
	public BillableService findByServiceType(final Concept serviceType) {
		if (serviceType == null) {
			return null;
		}
		
		List<BillableService> results = executeCriteria(BillableService.class, null, new Action1<Criteria>() {
			@Override
			public void apply(Criteria criteria) {
				criteria.add(Restrictions.eq("serviceType", serviceType));
				criteria.add(Restrictions.eq("retired", false));
			}
		});
		
		return results.isEmpty() ? null : results.get(0);
	}
	
	@Override
	protected IMetadataAuthorizationPrivileges getPrivileges() {
		return this;
	}
	
	@Override
	protected void validate(BillableService object) {
		if (object == null) {
			throw new APIException("Billable service cannot be null");
		}
		
		// Validate name uniqueness
		if (StringUtils.isNotBlank(object.getName())) {
			BillableService existing = findByName(object.getName());
			if (existing != null && !existing.getId().equals(object.getId())) {
				throw new APIException("A billable service with the name '" + object.getName() + "' already exists");
			}
		}
		
		// Validate short name uniqueness (only if not null/blank)
		if (StringUtils.isNotBlank(object.getShortName())) {
			BillableService existing = findByShortName(object.getShortName());
			if (existing != null && !existing.getId().equals(object.getId())) {
				throw new APIException("A billable service with the short name '" + object.getShortName() + "' already exists");
			}
		}
		
		// Validate service type uniqueness (only if not null)
		if (object.getServiceType() != null) {
			BillableService existing = findByServiceType(object.getServiceType());
			if (existing != null && !existing.getId().equals(object.getId())) {
				throw new APIException("A billable service with the service type '" + object.getServiceType().getName().getName() + "' already exists");
			}
		}
	}
	
	@Override
	public String getRetirePrivilege() {
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
