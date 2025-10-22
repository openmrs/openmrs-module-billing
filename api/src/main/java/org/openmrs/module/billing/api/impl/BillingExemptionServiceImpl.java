/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 */
package org.openmrs.module.billing.api.impl;

import lombok.RequiredArgsConstructor;
import org.openmrs.Concept;
import org.openmrs.module.billing.api.BillingExemptionService;
import org.openmrs.module.billing.api.db.BillingExemptionCategoryDao;
import org.openmrs.module.billing.api.db.BillingExemptionConceptDao;
import org.openmrs.module.billing.api.model.BillingExemptionCategory;
import org.openmrs.module.billing.api.model.BillingExemptionConcept;
import org.openmrs.module.billing.api.model.ExemptionCategoryType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of IBillingExemptionService
 */
@Service("billing.BillingExemptionService")
@Transactional
@RequiredArgsConstructor
public class BillingExemptionServiceImpl implements BillingExemptionService {
	
	private final BillingExemptionCategoryDao exemptionCategoryDao;
	
	private final BillingExemptionConceptDao billingExemptionConceptDao;
	
	// Category methods
	
	@Override
	@Transactional(readOnly = true)
	public BillingExemptionCategory getCategory(Integer id) {
		return exemptionCategoryDao.getCategory(id);
	}
	
	@Override
	@Transactional(readOnly = true)
	public BillingExemptionCategory getCategoryByUuid(String uuid) {
		return exemptionCategoryDao.getCategoryByUuid(uuid);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<BillingExemptionCategory> getCategoriesByType(ExemptionCategoryType type) {
		return exemptionCategoryDao.getCategoriesByType(type);
	}
	
	@Override
	@Transactional(readOnly = true)
	public BillingExemptionCategory getCategoryByTypeAndKey(ExemptionCategoryType type, String exemptionKey) {
		return exemptionCategoryDao.getCategoryByTypeAndKey(type, exemptionKey);
	}
	
	@Override
	@Transactional
	public BillingExemptionCategory saveCategory(BillingExemptionCategory category) {
		return exemptionCategoryDao.save(category);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<BillingExemptionCategory> getAllCategories(boolean includeRetired) {
		return exemptionCategoryDao.getAll(includeRetired);
	}
	
	// Exemption Concept methods
	
	@Override
	@Transactional(readOnly = true)
	public BillingExemptionConcept getExemptionConcept(Integer exemptionConceptId) {
		return billingExemptionConceptDao.getExemptionConcept(exemptionConceptId);
	}
	
	@Override
	@Transactional(readOnly = true)
	public BillingExemptionConcept getExemptionConceptByUuid(String uuid) {
		return billingExemptionConceptDao.getExemptionConceptByUuid(uuid);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<BillingExemptionConcept> getExemptionConceptsByCategory(BillingExemptionCategory category) {
		return billingExemptionConceptDao.getExemptionConceptsByCategory(category);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<BillingExemptionConcept> getExemptionConceptsByCategory(BillingExemptionCategory category,
	        boolean includeVoided) {
		return billingExemptionConceptDao.getExemptionConceptsByCategory(category, includeVoided);
	}
	
	@Override
	@Transactional(readOnly = true)
	public BillingExemptionConcept getExemptionConceptByCategoryAndConcept(BillingExemptionCategory category,
	        Concept concept) {
		return billingExemptionConceptDao.getExemptionConceptByCategoryAndConcept(category, concept);
	}
	
	@Override
	@Transactional
	public BillingExemptionConcept saveExemptionConcept(BillingExemptionConcept exemptionConcept) {
		return billingExemptionConceptDao.saveExemptionConcept(exemptionConcept);
	}
	
	@Override
	@Transactional
	public void deleteExemptionConcept(BillingExemptionConcept exemptionConcept) {
		billingExemptionConceptDao.deleteExemptionConcept(exemptionConcept);
	}
	
	// Helper methods
	
	@Override
	@Transactional(readOnly = true)
	public Set<Integer> getExemptedConceptIds(ExemptionCategoryType type, String exemptionKey) {
		BillingExemptionCategory category = getCategoryByTypeAndKey(type, exemptionKey);
		
		if (category == null) {
			return Collections.emptySet();
		}
		
		List<BillingExemptionConcept> concepts = billingExemptionConceptDao.getExemptionConceptsByCategory(category, false);
		
		return concepts.stream().map(ec -> ec.getConcept().getConceptId()).collect(Collectors.toSet());
	}
}
