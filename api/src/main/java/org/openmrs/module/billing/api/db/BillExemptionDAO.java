package org.openmrs.module.billing.api.db;

import org.openmrs.Concept;
import org.openmrs.module.billing.api.model.BillExemption;
import org.openmrs.module.billing.api.model.ExemptionType;

import java.util.List;

public interface BillExemptionDAO {
	
	BillExemption save(BillExemption billExemption);
	
	BillExemption getBillingExemptionById(Integer id);
	
	BillExemption getBillingExemptionByUuid(String uuid);
	
	List<BillExemption> getExemptionsByConcept(Concept concept, ExemptionType itemType, boolean includeRetired);
	
	List<BillExemption> getExemptionsByItemType(ExemptionType itemType, boolean includeRetired);
}
