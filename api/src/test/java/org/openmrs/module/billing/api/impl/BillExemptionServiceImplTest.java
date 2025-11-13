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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.BillExemptionService;
import org.openmrs.module.billing.api.model.BillExemption;
import org.openmrs.module.billing.api.model.ExemptionType;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BillExemptionServiceImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String EXEMPTION_UUID_1 = "3386610d-d272-43a9-9083-6c2a5272ade9";
	
	private BillExemptionService service;
	
	private ConceptService conceptService;
	
	@BeforeEach
	public void setup() {
		service = Context.getService(BillExemptionService.class);
		conceptService = Context.getConceptService();
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillExemptionTest.xml");
	}
	
	/**
	 * @see BillExemptionService#getBillingExemptionById(Integer)
	 */
	@Test
	public void getBillingExemptionById_shouldReturnExemptionWithSpecifiedId() {
		BillExemption exemption = service.getBillingExemptionById(1);
		
		assertNotNull(exemption);
		assertEquals(1, exemption.getExemptionId());
		assertEquals("Service Exemption 1", exemption.getName());
	}
	
	/**
	 * @see BillExemptionService#getBillingExemptionById(Integer)
	 */
	@Test
	public void getBillingExemptionById_shouldReturnNullForInvalidId() {
		BillExemption exemption = service.getBillingExemptionById(999);
		
		assertNull(exemption);
	}
	
	/**
	 * @see BillExemptionService#getBillingExemptionByUuid(String)
	 */
	@Test
	public void getBillingExemptionByUuid_shouldReturnExemptionWithSpecifiedUuid() {
		BillExemption exemption = service.getBillingExemptionByUuid(EXEMPTION_UUID_1);
		
		assertNotNull(exemption);
		assertEquals(EXEMPTION_UUID_1, exemption.getUuid());
		assertEquals("Service Exemption 1", exemption.getName());
	}
	
	/**
	 * @see BillExemptionService#getExemptionsByConcept(Concept, ExemptionType, boolean)
	 */
	@Test
	public void getExemptionsByConcept_shouldReturnExemptionsForConcept() {
		Concept concept = conceptService.getConcept(100);
		
		List<BillExemption> exemptions = service.getExemptionsByConcept(concept, null, false);
		
		assertNotNull(exemptions);
		assertFalse(exemptions.isEmpty());
		assertEquals("Service Exemption 1", exemptions.get(0).getName());
	}
	
	/**
	 * @see BillExemptionService#getExemptionsByItemType(ExemptionType, boolean)
	 */
	@Test
	public void getExemptionsByItemType_shouldReturnServiceExemptions() {
		List<BillExemption> serviceExemptions = service.getExemptionsByItemType(ExemptionType.SERVICE, false);
		
		assertNotNull(serviceExemptions);
		assertFalse(serviceExemptions.isEmpty());
		assertEquals(ExemptionType.SERVICE, serviceExemptions.get(0).getExemptionType());
	}
	
	/**
	 * @see BillExemptionService#getExemptionsByItemType(ExemptionType, boolean)
	 */
	@Test
	public void getExemptionsByItemType_shouldReturnCommodityExemptions() {
		List<BillExemption> commodityExemptions = service.getExemptionsByItemType(ExemptionType.COMMODITY, false);
		
		assertNotNull(commodityExemptions);
		assertEquals(1, commodityExemptions.size());
		assertEquals(ExemptionType.COMMODITY, commodityExemptions.get(0).getExemptionType());
	}
	
	/**
	 * @see BillExemptionService#save(BillExemption)
	 */
	@Test
	public void save_shouldUpdateExemption() {
		BillExemption exemption = service.getBillingExemptionById(1);
		assertNotNull(exemption);
		
		exemption.setName("Updated Name");
		BillExemption updated = service.save(exemption);
		
		assertNotNull(updated);
		assertEquals("Updated Name", updated.getName());
	}
}
