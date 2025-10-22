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
package org.openmrs.module.billing.api.db.hibernate;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.db.BillingExemptionCategoryDao;
import org.openmrs.module.billing.api.db.BillingExemptionConceptDao;
import org.openmrs.module.billing.api.model.BillingExemptionCategory;
import org.openmrs.module.billing.api.model.BillingExemptionConcept;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.util.List;

/**
 * Tests for {@link HibernateBillingExemptionConceptDao}
 */
public class HibernateBillingExemptionConceptDaoTest extends BaseModuleContextSensitiveTest {
	
	public static final String BILLING_EXEMPTION_DATASET = TestConstants.BASE_DATASET_DIR + "BillingExemptionTest.xml";
	
	private BillingExemptionConceptDao dao;
	
	private BillingExemptionCategoryDao categoryDao;
	
	private ConceptService conceptService;
	
	@BeforeEach
	public void before() throws Exception {
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(BILLING_EXEMPTION_DATASET);
		
		SessionFactory sessionFactory = Context.getRegisteredComponents(SessionFactory.class).get(0);
		dao = new HibernateBillingExemptionConceptDao(sessionFactory);
		categoryDao = new HibernateBillingExemptionCategoryDao(sessionFactory);
		conceptService = Context.getConceptService();
	}
	
	/**
	 * @see BillingExemptionConceptDao#getExemptionConcept(Integer)
	 */
	@Test
	public void getExemptionConcept_shouldReturnExemptionConceptWhenValidIdIsProvided() {
		BillingExemptionConcept exemptionConcept = dao.getExemptionConcept(0);
		
		Assertions.assertNotNull(exemptionConcept);
		Assertions.assertEquals(Integer.valueOf(0), exemptionConcept.getExemptionConceptId());
		Assertions.assertNotNull(exemptionConcept.getCategory());
		Assertions.assertNotNull(exemptionConcept.getConcept());
		Assertions.assertFalse(exemptionConcept.getVoided());
	}
	
	/**
	 * @see BillingExemptionConceptDao#getExemptionConcept(Integer)
	 */
	@Test
	public void getExemptionConcept_shouldReturnNullWhenInvalidIdIsProvided() {
		BillingExemptionConcept exemptionConcept = dao.getExemptionConcept(9999);
		
		Assertions.assertNull(exemptionConcept);
	}
	
	/**
	 * @see BillingExemptionConceptDao#getExemptionConceptByUuid(String)
	 */
	@Test
	public void getExemptionConceptByUuid_shouldReturnExemptionConceptWhenValidUuidIsProvided() {
		BillingExemptionConcept exemptionConcept = dao.getExemptionConceptByUuid("660e8400-e29b-41d4-a716-446655440001");
		
		Assertions.assertNotNull(exemptionConcept);
		Assertions.assertEquals(Integer.valueOf(0), exemptionConcept.getExemptionConceptId());
		Assertions.assertNotNull(exemptionConcept.getCategory());
		Assertions.assertNotNull(exemptionConcept.getConcept());
	}
	
	/**
	 * @see BillingExemptionConceptDao#getExemptionConceptByUuid(String)
	 */
	@Test
	public void getExemptionConceptByUuid_shouldReturnNullWhenInvalidUuidIsProvided() {
		BillingExemptionConcept exemptionConcept = dao.getExemptionConceptByUuid("invalid-uuid-12345");
		
		Assertions.assertNull(exemptionConcept);
	}
	
	/**
	 * @see BillingExemptionConceptDao#getExemptionConceptsByCategory(BillingExemptionCategory)
	 */
	@Test
	public void getExemptionConceptsByCategory_shouldReturnExemptionConceptsForCategoryExcludingVoided() {
		BillingExemptionCategory category = categoryDao.getCategory(0);
		Assertions.assertNotNull(category);
		
		List<BillingExemptionConcept> exemptionConcepts = dao.getExemptionConceptsByCategory(category);
		
		Assertions.assertNotNull(exemptionConcepts);
		Assertions.assertEquals(2, exemptionConcepts.size());
		
		for (BillingExemptionConcept concept : exemptionConcepts) {
			Assertions.assertFalse(concept.getVoided());
			Assertions.assertEquals(category.getExemptionCategoryId(), concept.getCategory().getExemptionCategoryId());
		}
	}
	
	/**
	 * @see BillingExemptionConceptDao#getExemptionConceptsByCategory(BillingExemptionCategory, boolean)
	 */
	@Test
	public void getExemptionConceptsByCategory_shouldReturnAllExemptionConceptsForCategoryWhenIncludeVoidedIsTrue() {
		BillingExemptionCategory category = categoryDao.getCategory(0);
		Assertions.assertNotNull(category);
		
		List<BillingExemptionConcept> exemptionConcepts = dao.getExemptionConceptsByCategory(category, true);
		
		Assertions.assertNotNull(exemptionConcepts);
		Assertions.assertEquals(3, exemptionConcepts.size());
		
		boolean hasVoidedConcept = false;
		for (BillingExemptionConcept concept : exemptionConcepts) {
			if (concept.getVoided()) {
				hasVoidedConcept = true;
				break;
			}
		}
		Assertions.assertTrue(hasVoidedConcept);
	}
	
	/**
	 * @see BillingExemptionConceptDao#getExemptionConceptsByCategory(BillingExemptionCategory, boolean)
	 */
	@Test
	public void getExemptionConceptsByCategory_shouldReturnOnlyNonVoidedExemptionConceptsWhenIncludeVoidedIsFalse() {
		BillingExemptionCategory category = categoryDao.getCategory(0);
		Assertions.assertNotNull(category);
		
		List<BillingExemptionConcept> exemptionConcepts = dao.getExemptionConceptsByCategory(category, false);
		
		Assertions.assertNotNull(exemptionConcepts);
		Assertions.assertEquals(2, exemptionConcepts.size());
		
		for (BillingExemptionConcept concept : exemptionConcepts) {
			Assertions.assertFalse(concept.getVoided());
		}
	}
	
	/**
	 * @see BillingExemptionConceptDao#getExemptionConceptByCategoryAndConcept(BillingExemptionCategory,
	 *      Concept)
	 */
	@Test
	public void getExemptionConceptByCategoryAndConcept_shouldReturnExemptionConceptWhenCategoryAndConceptMatch() {
		BillingExemptionCategory category = categoryDao.getCategory(0);
		Concept concept = conceptService.getConcept(100);
		
		Assertions.assertNotNull(category);
		Assertions.assertNotNull(concept);
		
		BillingExemptionConcept exemptionConcept = dao.getExemptionConceptByCategoryAndConcept(category, concept);
		
		Assertions.assertNotNull(exemptionConcept);
		Assertions.assertEquals(category.getExemptionCategoryId(), exemptionConcept.getCategory().getExemptionCategoryId());
		Assertions.assertEquals(concept.getConceptId(), exemptionConcept.getConcept().getConceptId());
	}
	
	/**
	 * @see BillingExemptionConceptDao#getExemptionConceptByCategoryAndConcept(BillingExemptionCategory,
	 *      Concept)
	 */
	@Test
	public void getExemptionConceptByCategoryAndConcept_shouldReturnNullWhenNoExemptionConceptMatchesCategoryAndConcept() {
		BillingExemptionCategory category = categoryDao.getCategory(0);
		Concept concept = conceptService.getConcept(5089);
		
		Assertions.assertNotNull(category);
		Assertions.assertNotNull(concept);
		
		BillingExemptionConcept exemptionConcept = dao.getExemptionConceptByCategoryAndConcept(category, concept);
		
		Assertions.assertNull(exemptionConcept);
	}
	
	/**
	 * @see BillingExemptionConceptDao#getExemptionConceptByCategoryAndConcept(BillingExemptionCategory,
	 *      Concept)
	 */
	@Test
	public void getExemptionConceptByCategoryAndConcept_shouldReturnNullWhenExemptionConceptIsVoided() {
		BillingExemptionCategory category = categoryDao.getCategory(0);
		Concept concept = conceptService.getConcept(103);
		
		Assertions.assertNotNull(category);
		Assertions.assertNotNull(concept);
		
		BillingExemptionConcept exemptionConcept = dao.getExemptionConceptByCategoryAndConcept(category, concept);
		
		Assertions.assertNull(exemptionConcept);
	}
	
	/**
	 * @see BillingExemptionConceptDao#saveExemptionConcept(BillingExemptionConcept)
	 */
	@Test
	public void saveExemptionConcept_shouldSaveNewExemptionConceptSuccessfully() {
		BillingExemptionCategory category = categoryDao.getCategory(1);
		Concept concept = conceptService.getConcept(101);
		
		Assertions.assertNotNull(category);
		Assertions.assertNotNull(concept);
		
		BillingExemptionConcept newExemptionConcept = new BillingExemptionConcept();
		newExemptionConcept.setCategory(category);
		newExemptionConcept.setConcept(concept);
		newExemptionConcept.setVoided(false);
		
		BillingExemptionConcept savedExemptionConcept = dao.saveExemptionConcept(newExemptionConcept);
		
		Assertions.assertNotNull(savedExemptionConcept);
		Assertions.assertNotNull(savedExemptionConcept.getExemptionConceptId());
		
		BillingExemptionConcept retrievedExemptionConcept = dao
		        .getExemptionConcept(savedExemptionConcept.getExemptionConceptId());
		Assertions.assertNotNull(retrievedExemptionConcept);
		Assertions.assertEquals(category.getExemptionCategoryId(),
		    retrievedExemptionConcept.getCategory().getExemptionCategoryId());
		Assertions.assertEquals(concept.getConceptId(), retrievedExemptionConcept.getConcept().getConceptId());
	}
	
	/**
	 * @see BillingExemptionConceptDao#saveExemptionConcept(BillingExemptionConcept)
	 */
	@Test
	public void saveExemptionConcept_shouldUpdateExistingExemptionConceptSuccessfully() {
		BillingExemptionConcept exemptionConcept = dao.getExemptionConcept(0);
		Assertions.assertNotNull(exemptionConcept);
		
		exemptionConcept.setVoided(true);
		exemptionConcept.setVoidReason("Test void reason");
		
		dao.saveExemptionConcept(exemptionConcept);
		
		BillingExemptionConcept updatedExemptionConcept = dao.getExemptionConcept(0);
		Assertions.assertNotNull(updatedExemptionConcept);
		Assertions.assertTrue(updatedExemptionConcept.getVoided());
		Assertions.assertEquals("Test void reason", updatedExemptionConcept.getVoidReason());
	}
	
	/**
	 * @see BillingExemptionConceptDao#deleteExemptionConcept(BillingExemptionConcept)
	 */
	@Test
	public void deleteExemptionConcept_shouldDeleteExemptionConceptSuccessfully() {
		BillingExemptionConcept exemptionConcept = dao.getExemptionConcept(0);
		Assertions.assertNotNull(exemptionConcept);
		
		dao.deleteExemptionConcept(exemptionConcept);
		
		BillingExemptionConcept deletedExemptionConcept = dao.getExemptionConcept(0);
		Assertions.assertNull(deletedExemptionConcept);
	}
}
