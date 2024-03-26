package org.openmrs.module.billing.api;

import java.util.List;

import org.openmrs.module.billing.api.base.entity.IEntityDataService;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.openmrs.module.stockmanagement.api.model.StockItem;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ItemPriceService extends IEntityDataService<CashierItemPrice> {
	
	CashierItemPrice save(CashierItemPrice price);
	
	List<CashierItemPrice> getItemPrice(StockItem stockItem);
	
	List<CashierItemPrice> getServicePrice(BillableService billableService);
}
