package org.openmrs.module.billing.api.db;

import org.openmrs.Concept;
import org.openmrs.module.billing.api.model.BillingExemptionCategory;
import org.openmrs.module.billing.api.model.BillingExemptionConcept;

import java.util.List;

public interface BillingExemptionConceptDao {
	
	BillingExemptionConcept getExemptionConcept(Integer exemptionConceptId);
	
	BillingExemptionConcept getExemptionConceptByUuid(String uuid);
	
	List<BillingExemptionConcept> getExemptionConceptsByCategory(BillingExemptionCategory category);
	
	List<BillingExemptionConcept> getExemptionConceptsByCategory(BillingExemptionCategory category, boolean includeVoided);
	
	BillingExemptionConcept getExemptionConceptByCategoryAndConcept(BillingExemptionCategory category, Concept concept);
	
	BillingExemptionConcept saveExemptionConcept(BillingExemptionConcept exemptionConcept);
	
	void deleteExemptionConcept(BillingExemptionConcept exemptionConcept);
}
