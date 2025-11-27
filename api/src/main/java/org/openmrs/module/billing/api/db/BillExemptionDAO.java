package org.openmrs.module.billing.api.db;

import org.openmrs.Concept;
import org.openmrs.module.billing.api.model.BillExemption;
import org.openmrs.module.billing.api.model.ExemptionType;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BillExemptionDAO {
	
	BillExemption save(BillExemption billExemption);
	
	BillExemption getBillingExemptionById(Integer id);
	
	@Transactional(readOnly = true)
	BillExemption getBillingExemptionByUuid(String uuid);
	
	@Transactional(readOnly = true)
	List<BillExemption> getExemptionsByConcept(Concept concept, ExemptionType itemType, boolean includeRetired);
	
	@Transactional(readOnly = true)
	List<BillExemption> getExemptionsByItemType(ExemptionType itemType, boolean includeRetired);
}
