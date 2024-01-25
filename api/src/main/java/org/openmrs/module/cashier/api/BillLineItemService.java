package org.openmrs.module.cashier.api;

import org.openmrs.module.cashier.api.base.entity.IEntityDataService;
import org.openmrs.module.cashier.api.model.BillLineItem;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface BillLineItemService extends IEntityDataService<BillLineItem> {}
