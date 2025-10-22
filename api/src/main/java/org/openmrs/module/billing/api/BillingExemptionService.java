/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 */
package org.openmrs.module.billing.api;

import org.openmrs.Concept;
import org.openmrs.module.billing.api.model.BillingExemptionCategory;
import org.openmrs.module.billing.api.model.BillingExemptionConcept;
import org.openmrs.module.billing.api.model.ExemptionCategoryType;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Service interface for managing billing exemptions
 */
@Transactional
public interface BillingExemptionService {
	
	// Category methods
	
	@Transactional(readOnly = true)
	BillingExemptionCategory getCategory(Integer id);
	
	@Transactional(readOnly = true)
	BillingExemptionCategory getCategoryByUuid(String uuid);
	
	@Transactional(readOnly = true)
	List<BillingExemptionCategory> getCategoriesByType(ExemptionCategoryType type);
	
	@Transactional(readOnly = true)
	BillingExemptionCategory getCategoryByTypeAndKey(ExemptionCategoryType type, String exemptionKey);
	
	@Transactional
	BillingExemptionCategory saveCategory(BillingExemptionCategory category);
	
	@Transactional(readOnly = true)
	List<BillingExemptionCategory> getAllCategories(boolean includeRetired);
	
	// Exemption Concept methods
	
	@Transactional(readOnly = true)
	BillingExemptionConcept getExemptionConcept(Integer exemptionConceptId);
	
	@Transactional(readOnly = true)
	BillingExemptionConcept getExemptionConceptByUuid(String uuid);
	
	@Transactional(readOnly = true)
	List<BillingExemptionConcept> getExemptionConceptsByCategory(BillingExemptionCategory category);
	
	@Transactional(readOnly = true)
	List<BillingExemptionConcept> getExemptionConceptsByCategory(BillingExemptionCategory category, boolean includeVoided);
	
	@Transactional(readOnly = true)
	BillingExemptionConcept getExemptionConceptByCategoryAndConcept(BillingExemptionCategory category, Concept concept);
	
	@Transactional
	BillingExemptionConcept saveExemptionConcept(BillingExemptionConcept exemptionConcept);
	
	@Transactional
	void deleteExemptionConcept(BillingExemptionConcept exemptionConcept);
	
	// Helper methods
	
	@Transactional(readOnly = true)
	Set<Integer> getExemptedConceptIds(ExemptionCategoryType type, String exemptionKey);
}
