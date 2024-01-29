package org.openmrs.module.cashier.api;

import org.openmrs.module.cashier.api.base.entity.IEntityDataService;
import org.openmrs.module.cashier.api.model.BillableService;
import org.openmrs.module.cashier.api.search.BillableServiceSearch;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface IBillableItemsService extends IEntityDataService<BillableService> {
	
	List<BillableService> findServices(final BillableServiceSearch search);
}
