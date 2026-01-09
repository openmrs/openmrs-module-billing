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
package org.openmrs.module.billing.db;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.db.BillableServiceDAO;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.BillableServiceStatus;
import org.openmrs.module.billing.api.search.BillableServiceSearch;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

public class HibernateBillableServiceDAOTest extends BaseModuleContextSensitiveTest {
	
	private BillableServiceDAO billableServiceDAO;
	
	private ConceptService conceptService;
	
	@BeforeEach
	public void setup() {
		billableServiceDAO = Context.getRegisteredComponent("billableServiceDAO", BillableServiceDAO.class);
		conceptService = Context.getConceptService();
		
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillableServiceTest.xml");
	}
	
	@Test
	public void getBillableService_shouldReturnBillableServiceById() {
		BillableService service = billableServiceDAO.getBillableService(0);
		assertNotNull(service);
		assertEquals(0, service.getId());
		assertEquals("General Consultation", service.getName());
	}
	
	@Test
	public void getBillableService_shouldReturnNullIfBillableServiceNotFound() {
		BillableService service = billableServiceDAO.getBillableService(999);
		assertNull(service);
	}
	
	@Test
	public void getBillableServiceByUuid_shouldReturnBillableServiceByUuid() {
		BillableService service = billableServiceDAO.getBillableService(0);
		assertNotNull(service);
		String uuid = service.getUuid();
		
		BillableService foundService = billableServiceDAO.getBillableServiceByUuid(uuid);
		assertNotNull(foundService);
		assertEquals(uuid, foundService.getUuid());
		assertEquals(0, foundService.getId());
	}
	
	@Test
	public void getBillableServiceByUuid_shouldReturnNullIfUuidNotFound() {
		BillableService service = billableServiceDAO.getBillableServiceByUuid("nonexistent-uuid");
		assertNull(service);
	}
	
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
		
		BillableService savedService = billableServiceDAO.saveBillableService(newService);
		Context.flushSession();
		
		assertNotNull(savedService);
		assertNotNull(savedService.getId());
		assertEquals("New Test Service", savedService.getName());
		assertEquals(BillableServiceStatus.ENABLED, savedService.getServiceStatus());
	}
	
	@Test
	public void saveBillableService_shouldUpdateExistingBillableService() {
		BillableService existingService = billableServiceDAO.getBillableService(1);
		assertNotNull(existingService);
		
		String newName = "Updated Consultation";
		existingService.setName(newName);
		
		billableServiceDAO.saveBillableService(existingService);
		Context.flushSession();
		Context.clearSession();
		
		BillableService updatedService = billableServiceDAO.getBillableService(1);
		assertEquals(newName, updatedService.getName());
	}
	
	@Test
	public void getBillableServices_shouldReturnAllBillableServicesWhenSearchIsEmpty() {
		BillableServiceSearch search = new BillableServiceSearch();
		List<BillableService> services = billableServiceDAO.getBillableServices(search, null);
		
		assertNotNull(services);
		assertFalse(services.isEmpty());
		// Should return 3 non-retired services (0, 1, 2)
		assertEquals(3, services.size());
	}
	
	@Test
	public void getBillableServices_shouldExcludeRetiredServicesByDefault() {
		BillableServiceSearch search = new BillableServiceSearch();
		search.setIncludeRetired(false);
		
		List<BillableService> services = billableServiceDAO.getBillableServices(search, null);
		assertNotNull(services);
		
		for (BillableService service : services) {
			assertFalse(service.getRetired());
		}
	}
	
	@Test
	public void getBillableServices_shouldIncludeRetiredServicesWhenRequested() {
		BillableServiceSearch search = new BillableServiceSearch();
		search.setIncludeRetired(true);
		
		List<BillableService> services = billableServiceDAO.getBillableServices(search, null);
		assertNotNull(services);
		// Should return all 4 services including retired one
		assertTrue(services.size() >= 4);
	}
	
	@Test
	public void getBillableServices_shouldFilterByServiceStatus() {
		BillableServiceSearch search = new BillableServiceSearch();
		search.setServiceStatus(BillableServiceStatus.DISABLED);
		
		List<BillableService> services = billableServiceDAO.getBillableServices(search, null);
		assertNotNull(services);
		assertFalse(services.isEmpty());
		
		for (BillableService service : services) {
			assertEquals(BillableServiceStatus.DISABLED, service.getServiceStatus());
		}
	}
	
	@Test
	public void getBillableServices_shouldFilterByServiceTypeUuid() {
		Concept serviceType = conceptService.getConcept(1000);
		assertNotNull(serviceType);
		
		BillableServiceSearch search = new BillableServiceSearch();
		search.setServiceTypeUuid(serviceType.getUuid());
		
		List<BillableService> services = billableServiceDAO.getBillableServices(search, null);
		assertNotNull(services);
		assertFalse(services.isEmpty());
		
		for (BillableService service : services) {
			assertEquals(serviceType.getUuid(), service.getServiceType().getUuid());
		}
	}
	
	@Test
	public void getBillableServices_shouldFilterByServiceCategoryUuid() {
		Concept serviceCategory = conceptService.getConcept(1001);
		assertNotNull(serviceCategory);
		
		BillableServiceSearch search = new BillableServiceSearch();
		search.setServiceCategoryUuid(serviceCategory.getUuid());
		
		List<BillableService> services = billableServiceDAO.getBillableServices(search, null);
		assertNotNull(services);
		assertFalse(services.isEmpty());
		
		for (BillableService service : services) {
			assertEquals(serviceCategory.getUuid(), service.getServiceCategory().getUuid());
		}
	}
	
	@Test
	public void getBillableServices_shouldFilterByConceptUuid() {
		Concept concept = conceptService.getConcept(1002);
		assertNotNull(concept);
		
		BillableServiceSearch search = new BillableServiceSearch();
		search.setConceptUuid(concept.getUuid());
		
		List<BillableService> services = billableServiceDAO.getBillableServices(search, null);
		assertNotNull(services);
		assertFalse(services.isEmpty());
		
		for (BillableService service : services) {
			assertEquals(concept.getUuid(), service.getConcept().getUuid());
		}
	}
	
	@Test
	public void getBillableServices_shouldFilterByNamePartialMatch() {
		BillableServiceSearch search = new BillableServiceSearch();
		search.setName("consultation");
		
		List<BillableService> services = billableServiceDAO.getBillableServices(search, null);
		assertNotNull(services);
		assertFalse(services.isEmpty());
		
		for (BillableService service : services) {
			assertTrue(service.getName().toLowerCase().contains("consultation"));
		}
	}
	
	@Test
	public void getBillableServices_shouldReturnEmptyListWhenSearchReturnsNoResults() {
		BillableServiceSearch search = new BillableServiceSearch();
		search.setName("NonexistentService");
		
		List<BillableService> services = billableServiceDAO.getBillableServices(search, null);
		assertNotNull(services);
		assertTrue(services.isEmpty());
	}
	
	@Test
	public void getBillableServices_shouldApplyPagingCorrectly() {
		BillableServiceSearch search = new BillableServiceSearch();
		PagingInfo pagingInfo = new PagingInfo(1, 2);
		
		List<BillableService> services = billableServiceDAO.getBillableServices(search, pagingInfo);
		assertNotNull(services);
		assertTrue(services.size() <= 2);
		assertNotNull(pagingInfo.getTotalRecordCount());
		assertTrue(pagingInfo.getTotalRecordCount() >= 3);
	}
	
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
		
		BillableService savedService = billableServiceDAO.saveBillableService(newService);
		Context.flushSession();
		
		Integer serviceId = savedService.getId();
		assertNotNull(serviceId);
		
		billableServiceDAO.purgeBillableService(savedService);
		Context.flushSession();
		Context.clearSession();
		
		BillableService deletedService = billableServiceDAO.getBillableService(serviceId);
		assertNull(deletedService);
	}
}
