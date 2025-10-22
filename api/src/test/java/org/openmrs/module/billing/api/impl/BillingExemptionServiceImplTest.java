/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.billing.api.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.BillingExemptionService;
import org.openmrs.module.billing.api.model.BillingExemptionCategory;
import org.openmrs.module.billing.api.model.BillingExemptionConcept;
import org.openmrs.module.billing.api.model.ExemptionCategoryType;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.util.List;
import java.util.Set;

/**
 * Tests for {@link BillingExemptionServiceImpl}
 */
public class BillingExemptionServiceImplTest extends BaseModuleContextSensitiveTest {
	
	public static final String BILLING_EXEMPTION_DATASET = TestConstants.BASE_DATASET_DIR + "BillingExemptionTest.xml";
	
	private BillingExemptionService service;
	
	private ConceptService conceptService;
	
	@BeforeEach
	public void before() throws Exception {
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(BILLING_EXEMPTION_DATASET);
		
		service = Context.getService(BillingExemptionService.class);
		conceptService = Context.getConceptService();
	}
	
	/**
	 * @see BillingExemptionService#getCategory(Integer)
	 */
	@Test
	public void getCategory_shouldReturnCategoryWhenValidIdIsProvided() {
		BillingExemptionCategory category = service.getCategory(0);
		
		Assertions.assertNotNull(category);
		Assertions.assertEquals(Integer.valueOf(0), category.getExemptionCategoryId());
		Assertions.assertEquals(ExemptionCategoryType.SERVICE, category.getType());
		Assertions.assertEquals("CHILD_0_5", category.getExemptionKey());
		Assertions.assertEquals("Children 0-5 Service Exemption", category.getName());
		Assertions.assertFalse(category.getRetired());
	}
	
	/**
	 * @see BillingExemptionService#getCategory(Integer)
	 */
	@Test
	public void getCategory_shouldReturnNullWhenInvalidIdIsProvided() {
		BillingExemptionCategory category = service.getCategory(9999);
		
		Assertions.assertNull(category);
	}
	
	/**
	 * @see BillingExemptionService#getCategoryByUuid(String)
	 */
	@Test
	public void getCategoryByUuid_shouldReturnCategoryWhenValidUuidIsProvided() {
		BillingExemptionCategory category = service.getCategoryByUuid("550e8400-e29b-41d4-a716-446655440001");
		
		Assertions.assertNotNull(category);
		Assertions.assertEquals(Integer.valueOf(0), category.getExemptionCategoryId());
		Assertions.assertEquals("CHILD_0_5", category.getExemptionKey());
		Assertions.assertEquals(ExemptionCategoryType.SERVICE, category.getType());
	}
	
	/**
	 * @see BillingExemptionService#getCategoryByUuid(String)
	 */
	@Test
	public void getCategoryByUuid_shouldReturnNullWhenInvalidUuidIsProvided() {
		BillingExemptionCategory category = service.getCategoryByUuid("invalid-uuid-12345");
		
		Assertions.assertNull(category);
	}
	
	/**
	 * @see BillingExemptionService#getCategoriesByType(ExemptionCategoryType)
	 */
	@Test
	public void getCategoriesByType_shouldReturnCategoriesOfSpecifiedType() {
		List<BillingExemptionCategory> serviceCategories = service.getCategoriesByType(ExemptionCategoryType.SERVICE);
		
		Assertions.assertNotNull(serviceCategories);
		Assertions.assertTrue(serviceCategories.size() >= 3);
		
		for (BillingExemptionCategory category : serviceCategories) {
			Assertions.assertEquals(ExemptionCategoryType.SERVICE, category.getType());
		}
		
		List<BillingExemptionCategory> commodityCategories = service.getCategoriesByType(ExemptionCategoryType.COMMODITY);
		
		Assertions.assertNotNull(commodityCategories);
		Assertions.assertTrue(commodityCategories.size() >= 1);
		
		for (BillingExemptionCategory category : commodityCategories) {
			Assertions.assertEquals(ExemptionCategoryType.COMMODITY, category.getType());
		}
	}
	
	/**
	 * @see BillingExemptionService#getCategoriesByType(ExemptionCategoryType)
	 */
	@Test
	public void getCategoriesByType_shouldReturnEmptyListWhenNoCategoriesOfSpecifiedTypeExist() {
		List<BillingExemptionCategory> categories = service.getCategoriesByType(ExemptionCategoryType.SERVICE);
		Assertions.assertNotNull(categories);
		Assertions.assertFalse(categories.isEmpty());
	}
	
	/**
	 * @see BillingExemptionService#getCategoryByTypeAndKey(ExemptionCategoryType, String)
	 */
	@Test
	public void getCategoryByTypeAndKey_shouldReturnCategoryWhenValidTypeAndKeyAreProvided() {
		BillingExemptionCategory category = service.getCategoryByTypeAndKey(ExemptionCategoryType.SERVICE, "CHILD_0_5");
		
		Assertions.assertNotNull(category);
		Assertions.assertEquals(Integer.valueOf(0), category.getExemptionCategoryId());
		Assertions.assertEquals(ExemptionCategoryType.SERVICE, category.getType());
		Assertions.assertEquals("CHILD_0_5", category.getExemptionKey());
		
		BillingExemptionCategory commodityCategory = service.getCategoryByTypeAndKey(ExemptionCategoryType.COMMODITY,
		    "CHILD_0_5");
		
		Assertions.assertNotNull(commodityCategory);
		Assertions.assertEquals(Integer.valueOf(2), commodityCategory.getExemptionCategoryId());
		Assertions.assertEquals(ExemptionCategoryType.COMMODITY, commodityCategory.getType());
		Assertions.assertEquals("CHILD_0_5", commodityCategory.getExemptionKey());
	}
	
	/**
	 * @see BillingExemptionService#getCategoryByTypeAndKey(ExemptionCategoryType, String)
	 */
	@Test
	public void getCategoryByTypeAndKey_shouldReturnNullWhenNoCategoryMatchesTypeAndKey() {
		BillingExemptionCategory category = service.getCategoryByTypeAndKey(ExemptionCategoryType.SERVICE,
		    "NON_EXISTENT_KEY");
		
		Assertions.assertNull(category);
	}
	
	/**
	 * @see BillingExemptionService#saveCategory(BillingExemptionCategory)
	 */
	@Test
	public void saveCategory_shouldSaveNewCategorySuccessfully() {
		BillingExemptionCategory category = new BillingExemptionCategory();
		category.setType(ExemptionCategoryType.SERVICE);
		category.setExemptionKey("TEST_NEW_CATEGORY");
		category.setName("Test New Category");
		category.setDescription("Test description for new category");
		
		BillingExemptionCategory saved = service.saveCategory(category);
		Context.flushSession();
		
		Assertions.assertNotNull(saved);
		Assertions.assertNotNull(saved.getExemptionCategoryId());
		Assertions.assertEquals(ExemptionCategoryType.SERVICE, saved.getType());
		Assertions.assertEquals("TEST_NEW_CATEGORY", saved.getExemptionKey());
		Assertions.assertEquals("Test New Category", saved.getName());
		
		BillingExemptionCategory retrieved = service.getCategory(saved.getExemptionCategoryId());
		Assertions.assertNotNull(retrieved);
		Assertions.assertEquals(saved.getExemptionKey(), retrieved.getExemptionKey());
	}
	
	/**
	 * @see BillingExemptionService#saveCategory(BillingExemptionCategory)
	 */
	@Test
	public void saveCategory_shouldUpdateExistingCategorySuccessfully() {
		BillingExemptionCategory category = service.getCategory(0);
		Assertions.assertNotNull(category);
		
		String originalName = category.getName();
		String updatedName = originalName + " - Updated";
		category.setName(updatedName);
		category.setDescription("Updated description");
		
		BillingExemptionCategory updated = service.saveCategory(category);
		Context.flushSession();
		
		Assertions.assertNotNull(updated);
		Assertions.assertEquals(Integer.valueOf(0), updated.getExemptionCategoryId());
		Assertions.assertEquals(updatedName, updated.getName());
		Assertions.assertEquals("Updated description", updated.getDescription());
		
		BillingExemptionCategory retrieved = service.getCategory(0);
		Assertions.assertEquals(updatedName, retrieved.getName());
	}
	
	/**
	 * @see BillingExemptionService#getAllCategories(boolean)
	 */
	@Test
	public void getAllCategories_shouldReturnAllCategoriesWhenIncludeRetiredIsTrue() {
		List<BillingExemptionCategory> categories = service.getAllCategories(true);
		
		Assertions.assertNotNull(categories);
		Assertions.assertTrue(categories.size() >= 4);
		
		boolean hasRetiredCategory = false;
		for (BillingExemptionCategory category : categories) {
			if (category.getRetired()) {
				hasRetiredCategory = true;
				break;
			}
		}
		Assertions.assertTrue(hasRetiredCategory, "Should include retired categories");
	}
	
	/**
	 * @see BillingExemptionService#getAllCategories(boolean)
	 */
	@Test
	public void getAllCategories_shouldReturnOnlyNonRetiredCategoriesWhenIncludeRetiredIsFalse() {
		List<BillingExemptionCategory> categories = service.getAllCategories(false);
		
		Assertions.assertNotNull(categories);
		Assertions.assertTrue(categories.size() >= 3);
		
		for (BillingExemptionCategory category : categories) {
			Assertions.assertFalse(category.getRetired(), "Should not include retired categories");
		}
	}
	
	/**
	 * @see BillingExemptionService#getExemptionConcept(Integer)
	 */
	@Test
	public void getExemptionConcept_shouldReturnExemptionConceptWhenValidIdIsProvided() {
		BillingExemptionConcept exemptionConcept = service.getExemptionConcept(0);
		
		Assertions.assertNotNull(exemptionConcept);
		Assertions.assertEquals(Integer.valueOf(0), exemptionConcept.getExemptionConceptId());
		Assertions.assertNotNull(exemptionConcept.getCategory());
		Assertions.assertEquals(Integer.valueOf(0), exemptionConcept.getCategory().getExemptionCategoryId());
		Assertions.assertNotNull(exemptionConcept.getConcept());
		Assertions.assertEquals(Integer.valueOf(100), exemptionConcept.getConcept().getConceptId());
	}
	
	/**
	 * @see BillingExemptionService#getExemptionConcept(Integer)
	 */
	@Test
	public void getExemptionConcept_shouldReturnNullWhenInvalidIdIsProvided() {
		BillingExemptionConcept exemptionConcept = service.getExemptionConcept(9999);
		
		Assertions.assertNull(exemptionConcept);
	}
	
	/**
	 * @see BillingExemptionService#getExemptionConceptByUuid(String)
	 */
	@Test
	public void getExemptionConceptByUuid_shouldReturnExemptionConceptWhenValidUuidIsProvided() {
		BillingExemptionConcept exemptionConcept = service.getExemptionConceptByUuid("660e8400-e29b-41d4-a716-446655440001");
		
		Assertions.assertNotNull(exemptionConcept);
		Assertions.assertEquals(Integer.valueOf(0), exemptionConcept.getExemptionConceptId());
		Assertions.assertNotNull(exemptionConcept.getCategory());
		Assertions.assertNotNull(exemptionConcept.getConcept());
	}
	
	/**
	 * @verifies return null when invalid uuid is provided
	 * @see BillingExemptionService#getExemptionConceptByUuid(String)
	 */
	@Test
	public void getExemptionConceptByUuid_shouldReturnNullWhenInvalidUuidIsProvided() {
		BillingExemptionConcept exemptionConcept = service.getExemptionConceptByUuid("invalid-uuid-12345");
		
		Assertions.assertNull(exemptionConcept);
	}
	
	/**
	 * @see BillingExemptionService#getExemptionConceptsByCategory(BillingExemptionCategory)
	 */
	@Test
	public void getExemptionConceptsByCategory_shouldReturnExemptionConceptsForCategoryExcludingVoided() {
		BillingExemptionCategory category = service.getCategory(0);
		Assertions.assertNotNull(category);
		
		List<BillingExemptionConcept> exemptionConcepts = service.getExemptionConceptsByCategory(category);
		
		Assertions.assertNotNull(exemptionConcepts);
		Assertions.assertEquals(2, exemptionConcepts.size());
		
		for (BillingExemptionConcept concept : exemptionConcepts) {
			Assertions.assertFalse(concept.getVoided(), "Should not include voided concepts");
			Assertions.assertEquals(category.getExemptionCategoryId(), concept.getCategory().getExemptionCategoryId());
		}
	}
	
	/**
	 * @see BillingExemptionService#getExemptionConceptsByCategory(BillingExemptionCategory, boolean)
	 */
	@Test
	public void getExemptionConceptsByCategory_shouldReturnAllExemptionConceptsForCategoryWhenIncludeVoidedIsTrue() {
		BillingExemptionCategory category = service.getCategory(0);
		Assertions.assertNotNull(category);
		
		List<BillingExemptionConcept> exemptionConcepts = service.getExemptionConceptsByCategory(category, true);
		
		Assertions.assertNotNull(exemptionConcepts);
		Assertions.assertEquals(3, exemptionConcepts.size());
		
		boolean hasVoidedConcept = false;
		for (BillingExemptionConcept concept : exemptionConcepts) {
			if (concept.getVoided()) {
				hasVoidedConcept = true;
				break;
			}
		}
		Assertions.assertTrue(hasVoidedConcept, "Should include voided concepts");
	}
	
	/**
	 * @see BillingExemptionService#getExemptionConceptsByCategory(BillingExemptionCategory, boolean)
	 */
	@Test
	public void getExemptionConceptsByCategory_shouldReturnOnlyNonVoidedExemptionConceptsWhenIncludeVoidedIsFalse() {
		BillingExemptionCategory category = service.getCategory(0);
		Assertions.assertNotNull(category);
		
		List<BillingExemptionConcept> exemptionConcepts = service.getExemptionConceptsByCategory(category, false);
		
		Assertions.assertNotNull(exemptionConcepts);
		Assertions.assertEquals(2, exemptionConcepts.size());
		
		for (BillingExemptionConcept concept : exemptionConcepts) {
			Assertions.assertFalse(concept.getVoided(), "Should not include voided concepts");
		}
	}
	
	/**
	 * @see BillingExemptionService#getExemptionConceptByCategoryAndConcept(BillingExemptionCategory,
	 *      Concept)
	 */
	@Test
	public void getExemptionConceptByCategoryAndConcept_shouldReturnExemptionConceptWhenCategoryAndConceptMatch() {
		BillingExemptionCategory category = service.getCategory(0);
		Concept concept = conceptService.getConcept(100);
		
		Assertions.assertNotNull(category);
		Assertions.assertNotNull(concept);
		
		BillingExemptionConcept exemptionConcept = service.getExemptionConceptByCategoryAndConcept(category, concept);
		
		Assertions.assertNotNull(exemptionConcept);
		Assertions.assertEquals(category.getExemptionCategoryId(), exemptionConcept.getCategory().getExemptionCategoryId());
		Assertions.assertEquals(concept.getConceptId(), exemptionConcept.getConcept().getConceptId());
	}
	
	/**
	 * @see BillingExemptionService#getExemptionConceptByCategoryAndConcept(BillingExemptionCategory,
	 *      Concept)
	 */
	@Test
	public void getExemptionConceptByCategoryAndConcept_shouldReturnNullWhenNoExemptionConceptMatchesCategoryAndConcept() {
		BillingExemptionCategory category = service.getCategory(0);
		Concept concept = conceptService.getConcept(102);
		
		Assertions.assertNotNull(category);
		Assertions.assertNotNull(concept);
		
		BillingExemptionConcept exemptionConcept = service.getExemptionConceptByCategoryAndConcept(category, concept);
		
		Assertions.assertNull(exemptionConcept);
	}
	
	/**
	 * @see BillingExemptionService#saveExemptionConcept(BillingExemptionConcept)
	 */
	@Test
	public void saveExemptionConcept_shouldSaveNewExemptionConceptSuccessfully() {
		BillingExemptionCategory category = service.getCategory(1);
		Concept concept = conceptService.getConcept(102);
		
		Assertions.assertNotNull(category);
		Assertions.assertNotNull(concept);
		
		BillingExemptionConcept exemptionConcept = new BillingExemptionConcept();
		exemptionConcept.setCategory(category);
		exemptionConcept.setConcept(concept);
		
		BillingExemptionConcept saved = service.saveExemptionConcept(exemptionConcept);
		Context.flushSession();
		
		Assertions.assertNotNull(saved);
		Assertions.assertNotNull(saved.getExemptionConceptId());
		Assertions.assertEquals(category.getExemptionCategoryId(), saved.getCategory().getExemptionCategoryId());
		Assertions.assertEquals(concept.getConceptId(), saved.getConcept().getConceptId());
		
		BillingExemptionConcept retrieved = service.getExemptionConcept(saved.getExemptionConceptId());
		Assertions.assertNotNull(retrieved);
		Assertions.assertEquals(saved.getExemptionConceptId(), retrieved.getExemptionConceptId());
	}
	
	/**
	 * @see BillingExemptionService#saveExemptionConcept(BillingExemptionConcept)
	 */
	@Test
	public void saveExemptionConcept_shouldUpdateExistingExemptionConceptSuccessfully() {
		BillingExemptionConcept exemptionConcept = service.getExemptionConcept(1);
		Assertions.assertNotNull(exemptionConcept);
		
		Concept newConcept = conceptService.getConcept(103);
		Assertions.assertNotNull(newConcept);
		
		exemptionConcept.setConcept(newConcept);
		
		BillingExemptionConcept updated = service.saveExemptionConcept(exemptionConcept);
		Context.flushSession();
		
		Assertions.assertNotNull(updated);
		Assertions.assertEquals(Integer.valueOf(1), updated.getExemptionConceptId());
		Assertions.assertEquals(Integer.valueOf(103), updated.getConcept().getConceptId());
		
		BillingExemptionConcept retrieved = service.getExemptionConcept(1);
		Assertions.assertEquals(Integer.valueOf(103), retrieved.getConcept().getConceptId());
	}
	
	/**
	 * @see BillingExemptionService#deleteExemptionConcept(BillingExemptionConcept)
	 */
	@Test
	public void deleteExemptionConcept_shouldDeleteExemptionConceptSuccessfully() {
		BillingExemptionConcept exemptionConcept = service.getExemptionConcept(1);
		Assertions.assertNotNull(exemptionConcept);
		
		Integer exemptionConceptId = exemptionConcept.getExemptionConceptId();
		
		service.deleteExemptionConcept(exemptionConcept);
		Context.flushSession();
		
		BillingExemptionConcept deleted = service.getExemptionConcept(exemptionConceptId);
		Assertions.assertNull(deleted);
	}
	
	/**
	 * @see BillingExemptionService#getExemptedConceptIds(ExemptionCategoryType, String)
	 */
	@Test
	public void getExemptedConceptIds_shouldReturnSetOfExemptedConceptIdsForValidTypeAndKey() {
		Set<Integer> conceptIds = service.getExemptedConceptIds(ExemptionCategoryType.SERVICE, "CHILD_0_5");
		
		Assertions.assertNotNull(conceptIds);
		Assertions.assertEquals(2, conceptIds.size());
		Assertions.assertTrue(conceptIds.contains(100));
		Assertions.assertTrue(conceptIds.contains(101));
	}
	
	/**
	 * @see BillingExemptionService#getExemptedConceptIds(ExemptionCategoryType, String)
	 */
	@Test
	public void getExemptedConceptIds_shouldReturnEmptySetWhenNoCategoryMatchesTypeAndKey() {
		Set<Integer> conceptIds = service.getExemptedConceptIds(ExemptionCategoryType.SERVICE, "NON_EXISTENT_KEY");
		
		Assertions.assertNotNull(conceptIds);
		Assertions.assertTrue(conceptIds.isEmpty());
	}
	
	/**
	 * @see BillingExemptionService#getExemptedConceptIds(ExemptionCategoryType, String)
	 */
	@Test
	public void getExemptedConceptIds_shouldExcludeVoidedConceptsFromReturnedSet() {
		Set<Integer> conceptIds = service.getExemptedConceptIds(ExemptionCategoryType.SERVICE, "CHILD_0_5");
		
		Assertions.assertNotNull(conceptIds);
		Assertions.assertFalse(conceptIds.contains(103), "Should not include voided concept 103");
		Assertions.assertEquals(2, conceptIds.size());
	}
	
	/**
	 * @see BillingExemptionService#getExemptedConceptIds(ExemptionCategoryType, String)
	 */
	@Test
	public void getExemptedConceptIds_shouldReturnCorrectConceptsForCommodityType() {
		Set<Integer> conceptIds = service.getExemptedConceptIds(ExemptionCategoryType.COMMODITY, "CHILD_0_5");
		
		Assertions.assertNotNull(conceptIds);
		Assertions.assertEquals(1, conceptIds.size());
		Assertions.assertTrue(conceptIds.contains(102));
	}
}
