package org.openmrs.module.cashier.api.search;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.cashier.api.base.entity.search.BaseDataTemplateSearch;
import org.openmrs.module.cashier.api.model.BillableService;

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
	}
}
