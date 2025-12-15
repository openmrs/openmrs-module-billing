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
package org.openmrs.module.billing.api.search;

import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.billing.api.base.entity.search.BaseDataTemplateSearch;
import org.openmrs.module.billing.api.model.BillableService;

public class BillableServiceSearch extends BaseDataTemplateSearch<BillableService> {
	
	public BillableServiceSearch() {
		this(new BillableService(), false);
	}
	
	public BillableServiceSearch(BillableService template) {
		this(template, false);
	}
	
	public BillableServiceSearch(BillableService template, Boolean includeRetired) {
		super(template, includeRetired);
	}
	
	@Override
	public void updateCriteria(Criteria criteria) {
		super.updateCriteria(criteria);
		
		BillableService billableService = getTemplate();
		if (billableService.getServiceStatus() != null) {
			criteria.add(Restrictions.eq("serviceStatus", billableService.getServiceStatus()));
		}
		if (billableService.getServiceCategory() != null) {
			criteria.add(Restrictions.eq("serviceCategory", billableService.getServiceCategory()));
		}
		if (billableService.getServiceType() != null) {
			criteria.add(Restrictions.eq("serviceType", billableService.getServiceType()));
		}
		if (billableService.getConcept() != null) {
			criteria.add(Restrictions.eq("concept", billableService.getConcept()));
		}
		if (billableService.getName() != null) {
			criteria.add(Restrictions.like("name", billableService.getName(), MatchMode.ANYWHERE));
		}
		if (billableService.getProvider() != null) {
			criteria.add(Restrictions.eq("provider", billableService.getProvider()));
		}
		if (billableService.getLocation() != null) {
			criteria.add(Restrictions.eq("location", billableService.getLocation()));
		}
	}
}
