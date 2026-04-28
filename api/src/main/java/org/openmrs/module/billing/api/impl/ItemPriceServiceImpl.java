/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.impl;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.billing.api.ItemPriceService;
import org.openmrs.module.billing.api.base.entity.impl.BaseMetadataDataServiceImpl;
import org.openmrs.module.billing.api.base.entity.security.IMetadataAuthorizationPrivileges;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.openmrs.module.stockmanagement.api.model.StockItem;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
public class ItemPriceServiceImpl extends BaseMetadataDataServiceImpl<CashierItemPrice> implements IMetadataAuthorizationPrivileges, ItemPriceService {
	
	@Override
	protected IMetadataAuthorizationPrivileges getPrivileges() {
		return this;
	}
	
	@Override
	protected void validate(CashierItemPrice object) {
		
	}
	
	@Override
	public CashierItemPrice save(CashierItemPrice object) {
		log.debug("Processing save Price");
		return super.save(object);
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
}
