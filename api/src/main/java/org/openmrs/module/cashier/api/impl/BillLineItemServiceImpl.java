package org.openmrs.module.cashier.api.impl;

import org.openmrs.module.cashier.api.BillLineItemService;
import org.openmrs.module.cashier.api.base.entity.impl.BaseEntityDataServiceImpl;
import org.openmrs.module.cashier.api.base.entity.security.IEntityAuthorizationPrivileges;
import org.openmrs.module.cashier.api.model.BillLineItem;
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
}
