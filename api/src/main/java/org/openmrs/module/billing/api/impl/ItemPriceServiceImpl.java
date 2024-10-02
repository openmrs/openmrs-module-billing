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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.billing.api.ItemPriceService;
import org.openmrs.module.billing.api.base.entity.impl.BaseEntityDataServiceImpl;
import org.openmrs.module.billing.api.base.entity.security.IEntityAuthorizationPrivileges;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.openmrs.module.stockmanagement.api.model.StockItem;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ItemPriceServiceImpl extends BaseEntityDataServiceImpl<CashierItemPrice> implements IEntityAuthorizationPrivileges, ItemPriceService {
	
	private static final Log LOG = LogFactory.getLog(ItemPriceServiceImpl.class);
	
	@Override
	protected IEntityAuthorizationPrivileges getPrivileges() {
		return this;
	}
	
	@Override
	protected void validate(CashierItemPrice object) {
		
	}
	
	@Override
	public CashierItemPrice save(CashierItemPrice object) {
		LOG.debug("Processing save Price");
		return super.save(object);
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
	public List<CashierItemPrice> getItemPrice(StockItem stockItem) {
		// Criteria criteria = getRepository().createCriteria(getEntityClass());
		Criteria criteria = getRepository().createCriteria(CashierItemPrice.class);
		
		criteria.add(Restrictions.eq("item", stockItem));
		criteria.addOrder(Order.desc("id"));
		
		// List<ItemPrice> results = getRepository().select(getEntityClass(), criteria);
		// return(results);
		return criteria.list();
	}
	
	@Override
	public List<CashierItemPrice> getServicePrice(BillableService billableService) {
		Criteria criteria = getRepository().createCriteria(CashierItemPrice.class);
		
		criteria.add(Restrictions.eq("billableService", billableService));
		criteria.addOrder(Order.desc("id"));
		return criteria.list();
	}
	
	@Override
	public List<CashierItemPrice> getServicePriceByName(String name) {
		Criteria criteria = getRepository().createCriteria(CashierItemPrice.class);
		criteria.createAlias("billableService", "service");
		
		criteria.add(Restrictions.eq("service.name", name));
		criteria.addOrder(Order.desc("id"));
		
		return criteria.list();
	}
	
}
