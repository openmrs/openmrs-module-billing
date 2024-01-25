package org.openmrs.module.cashier.api.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.cashier.api.ItemPriceService;
import org.openmrs.module.cashier.api.base.entity.impl.BaseEntityDataServiceImpl;
import org.openmrs.module.cashier.api.base.entity.security.IEntityAuthorizationPrivileges;
import org.openmrs.module.cashier.api.model.BillableService;
import org.openmrs.module.cashier.api.model.CashierItemPrice;
import org.openmrs.module.stockmanagement.api.model.StockItem;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ItemPriceServiceImpl extends BaseEntityDataServiceImpl<CashierItemPrice> implements IEntityAuthorizationPrivileges
        , ItemPriceService {

	@Override
	protected IEntityAuthorizationPrivileges getPrivileges() {
		return this;
	}

	@Override
	protected void validate(CashierItemPrice object) {

	}

	@Override
	public CashierItemPrice save(CashierItemPrice object) {
		System.out.println("Processing save Price");
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
}
