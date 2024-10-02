package org.openmrs.module.billing.api.impl;

import org.openmrs.module.billing.api.ChargeItemDefinitionService;
import org.openmrs.module.billing.api.ItemPriceService;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.openmrs.module.billing.api.search.BillableServiceSearch;
import org.openmrs.module.stockmanagement.api.model.StockItem;
import org.openmrs.module.billing.api.base.entity.impl.BaseEntityDataServiceImpl;
import org.openmrs.module.billing.api.base.entity.security.IEntityAuthorizationPrivileges;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.math.BigDecimal;
import java.util.Collections;

/**
 * Implementation of ChargeItemDefinitionService for managing ChargeItemDefinitions.
 */
@Service
@Transactional
public class ChargeItemDefinitionServiceImpl extends BaseEntityDataServiceImpl<CashierItemPrice> implements ChargeItemDefinitionService {
	
	@Autowired
	private ItemPriceService itemPriceService;
	
	@Override
	public List<CashierItemPrice> getChargeItemDefinition(BillableService billableService) {
		return itemPriceService.getServicePrice(billableService);
	}
	
	@Override
	public List<CashierItemPrice> getItemPrice(StockItem stockItem) {
		return Collections.emptyList();
	}
	
	@Override
	public List<CashierItemPrice> getServicePriceByName(String name) {
		return Collections.emptyList();
	}
	
	@Override
	protected IEntityAuthorizationPrivileges getPrivileges() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public CashierItemPrice getChargeItemDefinitionByCode(String name) {
		BillableServiceSearch search = new BillableServiceSearch();
		search.getTemplate().setName(name);
		List<CashierItemPrice> prices = itemPriceService.getServicePrice(search.getTemplate());
		return prices.isEmpty() ? null : prices.get(0);
	}
	
	@Override
	public CashierItemPrice saveChargeItemDefinition(CashierItemPrice cashierItemPrice) {
		return itemPriceService.save(cashierItemPrice);
	}
	
	@Override
	public List<CashierItemPrice> getServicePrice(BillableService billableService) {
		return itemPriceService.getServicePrice(billableService);
	}
	
	@Override
	public CashierItemPrice getById(int id) {
		return itemPriceService.getById(id);
	}
	
	@Override
	public CashierItemPrice unvoidEntity(CashierItemPrice cashierItemPrice) {
		return super.unvoidEntity(cashierItemPrice);
	}
	
	@Override
	protected void validate(CashierItemPrice cashierItemPrice) {
		if (cashierItemPrice == null) {
			throw new IllegalArgumentException("CashierItemPrice cannot be null.");
		}
		if (cashierItemPrice.getPrice() == null || cashierItemPrice.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("The price for CashierItemPrice must be greater than zero.");
		}
	}
	
}
