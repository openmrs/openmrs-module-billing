package org.openmrs.module.billing.api;

import org.openmrs.module.billing.api.base.entity.IEntityDataService;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ICashierItemPriceService extends IEntityDataService<CashierItemPrice> {}
