package org.openmrs.module.billing.api;

import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.CashierItemPrice;

import java.util.List;

/**
 * Service interface to manage ChargeItemDefinition-related operations.
 */
public interface ChargeItemDefinitionService extends ItemPriceService {
	
	List<CashierItemPrice> getChargeItemDefinition(BillableService billableService);
	
	CashierItemPrice saveChargeItemDefinition(CashierItemPrice cashierItemPrice);
	
	CashierItemPrice getChargeItemDefinitionByCode(String code);
	
}
