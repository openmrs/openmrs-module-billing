package org.openmrs.module.billing.api.impl;

import org.hibernate.Criteria;
import org.openmrs.module.billing.api.IBillableItemsService;
import org.openmrs.module.billing.api.base.entity.impl.BaseEntityDataServiceImpl;
import org.openmrs.module.billing.api.base.entity.security.IEntityAuthorizationPrivileges;
import org.openmrs.module.billing.api.base.f.Action1;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.search.BillableServiceSearch;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
