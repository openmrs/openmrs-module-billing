package org.openmrs.module.billing.api;

import org.openmrs.module.billing.api.base.entity.IEntityDataService;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.search.BillableServiceSearch;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface IBillableItemsService extends IEntityDataService<BillableService> {
	
	List<BillableService> findServices(final BillableServiceSearch search);
}
