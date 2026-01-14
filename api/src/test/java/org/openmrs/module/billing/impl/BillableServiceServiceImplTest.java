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

package org.openmrs.module.billing.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.BillableServiceService;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.impl.BillableServiceServiceImpl;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.BillableServiceStatus;
import org.openmrs.module.billing.api.search.BillableServiceSearch;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

public class BillableServiceServiceImplTest extends BaseModuleContextSensitiveTest {
	
	private BillableServiceService billableServiceService;
	
	private ConceptService conceptService;
	
	@BeforeEach
	public void setup() {
		billableServiceService = Context.getService(BillableServiceService.class);
		conceptService = Context.getConceptService();
		
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillableServiceTest.xml");
	}
	
	/**
	 * @see BillableServiceServiceImpl#saveBillableService(BillableService)
	 */
	@Test
	public void saveBillableService_shouldThrowNullPointerExceptionIfBillableServiceIsNull() {
		assertThrows(NullPointerException.class, () -> billableServiceService.saveBillableService(null));
	}
	
	/**
	 * @see BillableServiceServiceImpl#getBillableService(Integer)
	 */
	@Test
	public void getBillableService_shouldReturnBillableServiceWithSpecifiedId() {
		BillableService service = billableServiceService.getBillableService(0);
		assertNotNull(service);
		assertEquals(0, service.getId());
		assertEquals("General Consultation", service.getName());
	}
	
	/**
	 * @see BillableServiceServiceImpl#getBillableService(Integer)
	 */
	@Test
	public void getBillableService_shouldReturnNullIfIdNotFound() {
		BillableService service = billableServiceService.getBillableService(999);
		assertNull(service);
	}
	
	/**
	 * @see BillableServiceServiceImpl#getBillableService(Integer)
	 */
	@Test
	public void getBillableService_shouldReturnNullIfIdIsNull() {
		BillableService service = billableServiceService.getBillableService(null);
		assertNull(service);
	}
	
	/**
	 * @see BillableServiceServiceImpl#getBillableServiceByUuid(String)
	 */
	@Test
	public void getBillableServiceByUuid_shouldReturnBillableServiceWithSpecifiedUuid() {
		BillableService service = billableServiceService.getBillableService(0);
		assertNotNull(service);
		String uuid = service.getUuid();
		
		BillableService foundService = billableServiceService.getBillableServiceByUuid(uuid);
		assertNotNull(foundService);
		assertEquals(uuid, foundService.getUuid());
		assertEquals(0, foundService.getId());
	}
	
	/**
	 * @see BillableServiceServiceImpl#getBillableServiceByUuid(String)
	 */
	@Test
	public void getBillableServiceByUuid_shouldReturnNullIfUuidNotFound() {
		BillableService service = billableServiceService.getBillableServiceByUuid("nonexistent-uuid");
		assertNull(service);
	}
	
	/**
	 * @see BillableServiceServiceImpl#getBillableServiceByUuid(String)
	 */
	@Test
	public void getBillableServiceByUuid_shouldReturnNullIfUuidIsEmpty() {
		BillableService service = billableServiceService.getBillableServiceByUuid("");
		assertNull(service);
	}
	
	/**
	 * @see BillableServiceServiceImpl#saveBillableService(BillableService)
	 */
	@Test
	public void saveBillableService_shouldCreateNewBillableService() {
		Concept concept = conceptService.getConcept(1002);
		Concept serviceType = conceptService.getConcept(1000);
		Concept serviceCategory = conceptService.getConcept(1001);
		
		assertNotNull(concept);
		assertNotNull(serviceType);
		assertNotNull(serviceCategory);
		
		BillableService newService = new BillableService();
		newService.setName("New Test Service");
		newService.setShortName("New Test");
		newService.setConcept(concept);
		newService.setServiceType(serviceType);
		newService.setServiceCategory(serviceCategory);
		newService.setServiceStatus(BillableServiceStatus.ENABLED);
		newService.setUuid(UUID.randomUUID().toString());
		
		BillableService savedService = billableServiceService.saveBillableService(newService);
		
		assertNotNull(savedService);
		assertNotNull(savedService.getId());
		assertEquals("New Test Service", savedService.getName());
		assertEquals(BillableServiceStatus.ENABLED, savedService.getServiceStatus());
		
		BillableService retrievedService = billableServiceService.getBillableService(savedService.getId());
		assertNotNull(retrievedService);
		assertEquals("New Test Service", retrievedService.getName());
	}
	
	/**
	 * @see BillableServiceServiceImpl#saveBillableService(BillableService)
	 */
	@Test
	public void saveBillableService_shouldUpdateExistingBillableService() {
		BillableService existingService = billableServiceService.getBillableService(1);
		assertNotNull(existingService);
		
		String newName = "Updated Specialist Consultation";
		existingService.setName(newName);
		
		billableServiceService.saveBillableService(existingService);
		
		BillableService updatedService = billableServiceService.getBillableService(1);
		assertEquals(newName, updatedService.getName());
	}
	
	/**
	 * @see BillableServiceServiceImpl#getBillableServices(BillableServiceSearch, PagingInfo)
	 */
	@Test
	public void getBillableServices_shouldReturnAllBillableServicesWhenSearchIsEmpty() {
		BillableServiceSearch search = new BillableServiceSearch();
		List<BillableService> services = billableServiceService.getBillableServices(search, null);
		
		assertNotNull(services);
		assertFalse(services.isEmpty());
	}
	
	/**
	 * @see BillableServiceServiceImpl#getBillableServices(BillableServiceSearch, PagingInfo)
	 */
	@Test
	public void getBillableServices_shouldReturnEmptyListWhenSearchIsNull() {
		List<BillableService> services = billableServiceService.getBillableServices(null, null);
		assertNotNull(services);
		assertTrue(services.isEmpty());
	}
	
	/**
	 * @see BillableServiceServiceImpl#getBillableServices(BillableServiceSearch, PagingInfo)
	 */
	@Test
	public void getBillableServices_shouldFilterByServiceStatus() {
		BillableServiceSearch search = new BillableServiceSearch();
		search.setServiceStatus(BillableServiceStatus.DISABLED);
		
		List<BillableService> services = billableServiceService.getBillableServices(search, null);
		assertNotNull(services);
		assertFalse(services.isEmpty());
		
		for (BillableService service : services) {
			assertEquals(BillableServiceStatus.DISABLED, service.getServiceStatus());
		}
	}
	
	/**
	 * @see BillableServiceServiceImpl#getBillableServices(BillableServiceSearch, PagingInfo)
	 */
	@Test
	public void getBillableServices_shouldFilterByServiceTypeUuid() {
		Concept serviceType = conceptService.getConcept(1000);
		assertNotNull(serviceType);
		
		BillableServiceSearch search = new BillableServiceSearch();
		search.setServiceTypeUuid(serviceType.getUuid());
		
		List<BillableService> services = billableServiceService.getBillableServices(search, null);
		assertNotNull(services);
		assertFalse(services.isEmpty());
		
		for (BillableService service : services) {
			assertEquals(serviceType.getUuid(), service.getServiceType().getUuid());
		}
	}
	
	/**
	 * @see BillableServiceServiceImpl#getBillableServices(BillableServiceSearch, PagingInfo)
	 */
	@Test
	public void getBillableServices_shouldFilterByName() {
		BillableServiceSearch search = new BillableServiceSearch();
		search.setName("consultation");
		
		List<BillableService> services = billableServiceService.getBillableServices(search, null);
		assertNotNull(services);
		assertFalse(services.isEmpty());
		
		for (BillableService service : services) {
			assertTrue(service.getName().toLowerCase().contains("consultation"));
		}
	}
	
	/**
	 * @see BillableServiceServiceImpl#getBillableServices(BillableServiceSearch, PagingInfo)
	 */
	@Test
	public void getBillableServices_shouldExcludeRetiredServicesByDefault() {
		BillableServiceSearch search = new BillableServiceSearch();
		search.setIncludeRetired(false);
		
		List<BillableService> services = billableServiceService.getBillableServices(search, null);
		assertNotNull(services);
		
		for (BillableService service : services) {
			assertFalse(service.getRetired());
		}
	}
	
	/**
	 * @see BillableServiceServiceImpl#getBillableServices(BillableServiceSearch, PagingInfo)
	 */
	@Test
	public void getBillableServices_shouldReturnEmptyListWhenSearchReturnsNoResults() {
		BillableServiceSearch search = new BillableServiceSearch();
		search.setName("NonexistentService");
		
		List<BillableService> services = billableServiceService.getBillableServices(search, null);
		assertNotNull(services);
		assertTrue(services.isEmpty());
	}
	
	/**
	 * @see BillableServiceServiceImpl#getBillableServices(BillableServiceSearch, PagingInfo)
	 */
	@Test
	public void getBillableServices_shouldApplyPagingCorrectly() {
		BillableServiceSearch search = new BillableServiceSearch();
		PagingInfo pagingInfo = new PagingInfo(1, 2);
		
		List<BillableService> services = billableServiceService.getBillableServices(search, pagingInfo);
		assertNotNull(services);
		assertTrue(services.size() <= 2);
		assertNotNull(pagingInfo.getTotalRecordCount());
	}
	
	/**
	 * @see BillableServiceServiceImpl#purgeBillableService(BillableService)
	 */
	@Test
	public void purgeBillableService_shouldThrowNullPointerExceptionIfBillableServiceIsNull() {
		assertThrows(NullPointerException.class, () -> billableServiceService.purgeBillableService(null));
	}
	
	/**
	 * @see BillableServiceServiceImpl#purgeBillableService(BillableService)
	 */
	@Test
	public void purgeBillableService_shouldDeleteBillableService() {
		Concept concept = conceptService.getConcept(1002);
		Concept serviceType = conceptService.getConcept(1000);
		Concept serviceCategory = conceptService.getConcept(1001);
		
		BillableService newService = new BillableService();
		newService.setName("Service To Delete");
		newService.setShortName("ToDelete");
		newService.setConcept(concept);
		newService.setServiceType(serviceType);
		newService.setServiceCategory(serviceCategory);
		newService.setServiceStatus(BillableServiceStatus.ENABLED);
		newService.setUuid(UUID.randomUUID().toString());
		
		BillableService savedService = billableServiceService.saveBillableService(newService);
		
		Integer serviceId = savedService.getId();
		assertNotNull(serviceId);
		
		billableServiceService.purgeBillableService(savedService);
		
		BillableService deletedService = billableServiceService.getBillableService(serviceId);
		assertNull(deletedService);
	}
	
	/**
	 * @see BillableServiceServiceImpl#retireBillableService(BillableService, String)
	 */
	@Test
	public void retireBillableService_shouldRetireBillableService() {
		BillableService service = billableServiceService.getBillableService(0);
		assertNotNull(service);
		assertFalse(service.getRetired());
		
		String retireReason = "No longer in use";
		BillableService retiredService = billableServiceService.retireBillableService(service, retireReason);
		
		assertTrue(retiredService.getRetired());
		assertEquals(retireReason, retiredService.getRetireReason());
	}
	
	/**
	 * @see BillableServiceServiceImpl#retireBillableService(BillableService, String)
	 */
	@Test
	public void retireBillableService_shouldThrowExceptionIfReasonIsEmpty() {
		BillableService service = billableServiceService.getBillableService(0);
		
		assertThrows(IllegalArgumentException.class, () -> billableServiceService.retireBillableService(service, ""));
	}
	
	/**
	 * @see BillableServiceServiceImpl#unretireBillableService(BillableService)
	 */
	@Test
	public void unretireBillableService_shouldUnretireRetiredService() {
		BillableService service = billableServiceService.getBillableService(3);
		assertNotNull(service);
		assertTrue(service.getRetired());
		
		BillableService unretiredService = billableServiceService.unretireBillableService(service);
		
		assertFalse(unretiredService.getRetired());
		assertNull(unretiredService.getRetireReason());
	}
}
