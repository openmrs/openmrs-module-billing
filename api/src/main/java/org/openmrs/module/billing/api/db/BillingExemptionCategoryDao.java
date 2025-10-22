package org.openmrs.module.billing.api.db;

import org.openmrs.module.billing.api.model.BillingExemptionCategory;
import org.openmrs.module.billing.api.model.ExemptionCategoryType;

import java.util.List;

public interface BillingExemptionCategoryDao {
	
	BillingExemptionCategory getCategory(Integer id);
	
	BillingExemptionCategory getCategoryByUuid(String uuid);
	
	List<BillingExemptionCategory> getCategoriesByType(ExemptionCategoryType type);
	
	BillingExemptionCategory getCategoryByTypeAndKey(ExemptionCategoryType type, String exemptionKey);
	
	BillingExemptionCategory save(BillingExemptionCategory category);
	
	List<BillingExemptionCategory> getAll(boolean includeRetired);
	
}
