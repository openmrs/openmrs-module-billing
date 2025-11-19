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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.db.BillExemptionDAO;
import org.openmrs.module.billing.api.model.BillExemption;
import org.openmrs.module.billing.api.model.BillExemptionRule;
import org.openmrs.module.billing.api.model.ExemptionType;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BillExemptionDAOImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String EXEMPTION_UUID_1 = "3386610d-d272-43a9-9083-6c2a5272ade9";
	
	private BillExemptionDAO dao;
	
	private ConceptService conceptService;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@BeforeEach
	public void setup() {
		dao = new BillExemptionDAOImpl(sessionFactory);
		conceptService = Context.getConceptService();
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillExemptionTest.xml");
	}
	
	/**
	 * @see BillExemptionDAO#getBillingExemptionById(Integer)
	 */
	@Test
	public void getBillingExemptionById_shouldReturnExemptionWithSpecifiedId() {
		BillExemption exemption = dao.getBillingExemptionById(1);
		
		assertNotNull(exemption);
		assertEquals(1, exemption.getExemptionId());
		assertEquals("Service Exemption 1", exemption.getName());
		assertEquals(ExemptionType.SERVICE, exemption.getExemptionType());
		assertFalse(exemption.getRetired());
	}
	
	/**
	 * @see BillExemptionDAO#getBillingExemptionById(Integer)
	 */
	@Test
	public void getBillingExemptionById_shouldReturnNullForInvalidId() {
		BillExemption exemption = dao.getBillingExemptionById(999);
		
		assertNull(exemption);
	}
	
	/**
	 * @see BillExemptionDAO#getBillingExemptionByUuid(String)
	 */
	@Test
	public void getBillingExemptionByUuid_shouldReturnExemptionWithSpecifiedUuid() {
		BillExemption exemption = dao.getBillingExemptionByUuid(EXEMPTION_UUID_1);
		
		assertNotNull(exemption);
		assertEquals(EXEMPTION_UUID_1, exemption.getUuid());
		assertEquals("Service Exemption 1", exemption.getName());
		assertEquals(ExemptionType.SERVICE, exemption.getExemptionType());
	}
	
	/**
	 * @see BillExemptionDAO#getExemptionsByConcept(Concept, ExemptionType, boolean)
	 */
	@Test
	public void getExemptionsByConcept_shouldReturnExemptionsForSpecificConcept() {
		Concept concept = conceptService.getConcept(100);
		assertNotNull(concept);
		
		List<BillExemption> exemptions = dao.getExemptionsByConcept(concept, null, false);
		
		assertNotNull(exemptions);
		assertEquals(1, exemptions.size());
		assertEquals("Service Exemption 1", exemptions.get(0).getName());
	}
	
	/**
	 * @see BillExemptionDAO#getExemptionsByConcept(Concept, ExemptionType, boolean)
	 */
	@Test
	public void getExemptionsByConcept_shouldReturnExemptionsFilteredByExemptionType() {
		Concept concept = conceptService.getConcept(100);
		assertNotNull(concept);
		
		List<BillExemption> serviceExemptions = dao.getExemptionsByConcept(concept, ExemptionType.SERVICE, false);
		
		assertNotNull(serviceExemptions);
		assertEquals(1, serviceExemptions.size());
		assertEquals(ExemptionType.SERVICE, serviceExemptions.get(0).getExemptionType());
	}
	
	/**
	 * @see BillExemptionDAO#getExemptionsByConcept(Concept, ExemptionType, boolean)
	 */
	@Test
	public void getExemptionsByConcept_shouldNotReturnRetiredExemptionsWhenIncludeRetiredIsFalse() {
		Concept concept = conceptService.getConcept(103);
		assertNotNull(concept);
		
		List<BillExemption> exemptions = dao.getExemptionsByConcept(concept, null, false);
		
		assertNotNull(exemptions);
		assertTrue(exemptions.isEmpty() || exemptions.stream().noneMatch(BaseOpenmrsMetadata::getRetired));
	}
	
	/**
	 * @see BillExemptionDAO#getExemptionsByConcept(Concept, ExemptionType, boolean)
	 */
	@Test
	public void getExemptionsByConcept_shouldReturnOnlyRetiredExemptionsWhenIncludeRetiredIsTrue() {
		Concept concept = conceptService.getConcept(103);
		assertNotNull(concept);
		
		List<BillExemption> exemptions = dao.getExemptionsByConcept(concept, null, true);
		
		assertNotNull(exemptions);
		assertFalse(exemptions.isEmpty());
		assertTrue(exemptions.stream().allMatch(BaseOpenmrsMetadata::getRetired));
		assertEquals("Retired Service Exemption", exemptions.get(0).getName());
	}
	
	/**
	 * @see BillExemptionDAO#getExemptionsByItemType(ExemptionType, boolean)
	 */
	@Test
	public void getExemptionsByItemType_shouldReturnAllServiceExemptions() {
		List<BillExemption> serviceExemptions = dao.getExemptionsByItemType(ExemptionType.SERVICE, false);
		
		assertNotNull(serviceExemptions);
		assertTrue(!serviceExemptions.isEmpty());
		assertTrue(
		    serviceExemptions.stream().allMatch(e -> e.getExemptionType() == ExemptionType.SERVICE && !e.getRetired()));
	}
	
	/**
	 * @see BillExemptionDAO#getExemptionsByItemType(ExemptionType, boolean)
	 */
	@Test
	public void getExemptionsByItemType_shouldReturnAllCommodityExemptions() {
		List<BillExemption> commodityExemptions = dao.getExemptionsByItemType(ExemptionType.COMMODITY, false);
		
		assertNotNull(commodityExemptions);
		assertEquals(1, commodityExemptions.size());
		assertEquals(ExemptionType.COMMODITY, commodityExemptions.get(0).getExemptionType());
		assertEquals("Commodity Exemption 1", commodityExemptions.get(0).getName());
	}
	
	/**
	 * @see BillExemptionDAO#getExemptionsByItemType(ExemptionType, boolean)
	 */
	@Test
	public void getExemptionsByItemType_shouldReturnBothTypeExemptions() {
		List<BillExemption> bothTypeExemptions = dao.getExemptionsByItemType(ExemptionType.BOTH, false);
		
		assertNotNull(bothTypeExemptions);
		assertEquals(1, bothTypeExemptions.size());
		assertEquals(ExemptionType.BOTH, bothTypeExemptions.get(0).getExemptionType());
		assertEquals("Both Type Exemption", bothTypeExemptions.get(0).getName());
	}
	
	/**
	 * @see BillExemptionDAO#getExemptionsByItemType(ExemptionType, boolean)
	 */
	@Test
	public void getExemptionsByItemType_shouldReturnAllExemptionsIncludingRetiredWhenIncludeRetiredIsTrue() {
		List<BillExemption> allExemptions = dao.getExemptionsByItemType(ExemptionType.SERVICE, true);
		
		assertNotNull(allExemptions);
		assertFalse(allExemptions.isEmpty());
		assertTrue(allExemptions.size() >= 2);
		assertTrue(allExemptions.stream().anyMatch(BaseOpenmrsMetadata::getRetired));
		assertTrue(allExemptions.stream().anyMatch(e -> !e.getRetired()));
	}
	
	/**
	 * @see BillExemptionDAO#save(BillExemption)
	 */
	@Test
	public void save_shouldSaveNewBillingExemption() {
		Concept concept = conceptService.getConcept(100);
		assertNotNull(concept);
		
		BillExemption newExemption = new BillExemption();
		newExemption.setName("New Test Exemption");
		newExemption.setDescription("Test exemption created by test");
		newExemption.setConcept(concept);
		newExemption.setExemptionType(ExemptionType.SERVICE);
		newExemption.setCreator(Context.getAuthenticatedUser());
		newExemption.setDateCreated(new Date());
		
		BillExemption saved = dao.save(newExemption);
		
		assertNotNull(saved);
		assertNotNull(saved.getExemptionId());
		assertEquals("New Test Exemption", saved.getName());
		assertEquals(ExemptionType.SERVICE, saved.getExemptionType());
		assertEquals(concept.getId(), saved.getConcept().getId());
	}
	
	/**
	 * @see BillExemptionDAO#save(BillExemption)
	 */
	@Test
	public void save_shouldUpdateExistingBillingExemption() {
		BillExemption exemption = dao.getBillingExemptionById(1);
		assertNotNull(exemption);
		
		String originalName = exemption.getName();
		String newName = "Updated Service Exemption";
		exemption.setName(newName);
		
		BillExemption updated = dao.save(exemption);
		
		assertNotNull(updated);
		assertEquals(1, updated.getExemptionId());
		assertEquals(newName, updated.getName());
		assertTrue(!originalName.equals(updated.getName()));
	}
	
	/**
	 * @see BillExemptionDAO#getBillingExemptionById(Integer)
	 */
	@Test
	public void getBillingExemptionById_shouldLoadExemptionWithRules() {
		BillExemption exemption = dao.getBillingExemptionById(1);
		
		assertNotNull(exemption);
		assertNotNull(exemption.getRules());
		assertFalse(exemption.getRules().isEmpty());
		
		BillExemptionRule rule = exemption.getRules().get(0);
		assertNotNull(rule);
		assertEquals("patientAge < 5", rule.getScript());
	}
	
	/**
	 * @see BillExemptionDAO#getExemptionsByConcept(Concept, ExemptionType, boolean)
	 */
	@Test
	public void getExemptionsByConcept_shouldReturnEmptyListWhenNoMatchingExemptions() {
		Concept concept = conceptService.getConcept(100);
		assertNotNull(concept);
		
		List<BillExemption> exemptions = dao.getExemptionsByConcept(concept, ExemptionType.COMMODITY, false);
		
		assertNotNull(exemptions);
		assertTrue(exemptions.isEmpty());
	}
	
	/**
	 * @see BillExemptionDAO#getExemptionsByItemType(ExemptionType, boolean)
	 */
	@Test
	public void getExemptionsByItemType_shouldReturnAllExemptionsWhenItemTypeIsNull() {
		List<BillExemption> allExemptions = dao.getExemptionsByItemType(null, false);
		
		assertNotNull(allExemptions);
		assertTrue(allExemptions.size() >= 3);
		assertTrue(allExemptions.stream().noneMatch(BaseOpenmrsMetadata::getRetired));
	}
}
