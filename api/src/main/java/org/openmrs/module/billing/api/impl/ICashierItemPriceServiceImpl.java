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
