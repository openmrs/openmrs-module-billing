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
package org.openmrs.module.billing;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.IBillableItemsService;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.BillableServiceStatus;
import org.openmrs.module.billing.base.entity.IEntityDataServiceTest;

/**
 * Tests for {@link IBillableItemsService}.
 */
public class IBillableItemsServiceTest extends IEntityDataServiceTest<IBillableItemsService, BillableService> {
	
	public static final String BILLABLE_SERVICE_DATASET = TestConstants.BASE_DATASET_DIR + "BillableServiceTest.xml";
	
	@Override
	public void before() throws Exception {
		super.before();
		
		executeDataSet(TestConstants.CORE_DATASET);
		executeDataSet(BILLABLE_SERVICE_DATASET);
	}
	
	@Override
	public BillableService createEntity(boolean valid) {
		BillableService service = new BillableService();
		
		if (valid) {
			service.setName("Test Service " + System.currentTimeMillis());
			service.setShortName("TS" + System.currentTimeMillis());
			service.setServiceStatus(BillableServiceStatus.ENABLED);
		}
		
		return service;
	}
	
	@Override
	protected int getTestEntityCount() {
		return 3;
	}
	
	@Override
	protected void updateEntityFields(BillableService service) {
		service.setName(service.getName() + " updated");
		service.setShortName(service.getShortName() + "_upd");
		service.setServiceStatus(BillableServiceStatus.DISABLED);
	}
	
	/**
	 * @verifies find a billable service by name
	 * @see IBillableItemsService#findByName(String)
	 */
	@Test
	public void findByName_shouldFindBillableServiceByName() throws Exception {
		BillableService result = service.findByName("General Consultation");
		
		Assert.assertNotNull(result);
		Assert.assertEquals("General Consultation", result.getName());
	}
	
	/**
	 * @verifies return null when name not found
	 * @see IBillableItemsService#findByName(String)
	 */
	@Test
	public void findByName_shouldReturnNullWhenNameNotFound() throws Exception {
		BillableService result = service.findByName("Non-existent Service");
		
		Assert.assertNull(result);
	}
	
	/**
	 * @verifies be case insensitive
	 * @see IBillableItemsService#findByName(String)
	 */
	@Test
	public void findByName_shouldBeCaseInsensitive() throws Exception {
		BillableService result = service.findByName("general consultation");
		
		Assert.assertNotNull(result);
		Assert.assertEquals("General Consultation", result.getName());
	}
	
	/**
	 * @verifies return null when name is blank
	 * @see IBillableItemsService#findByName(String)
	 */
	@Test
	public void findByName_shouldReturnNullWhenNameIsBlank() throws Exception {
		BillableService result = service.findByName("");
		
		Assert.assertNull(result);
	}
	
	/**
	 * @verifies find a billable service by short name
	 * @see IBillableItemsService#findByShortName(String)
	 */
	@Test
	public void findByShortName_shouldFindBillableServiceByShortName() throws Exception {
		BillableService result = service.findByShortName("GC");
		
		Assert.assertNotNull(result);
		Assert.assertEquals("GC", result.getShortName());
	}
	
	/**
	 * @verifies return null when short name not found
	 * @see IBillableItemsService#findByShortName(String)
	 */
	@Test
	public void findByShortName_shouldReturnNullWhenShortNameNotFound() throws Exception {
		BillableService result = service.findByShortName("NE");
		
		Assert.assertNull(result);
	}
	
	/**
	 * @verifies be case insensitive
	 * @see IBillableItemsService#findByShortName(String)
	 */
	@Test
	public void findByShortName_shouldBeCaseInsensitive() throws Exception {
		BillableService result = service.findByShortName("gc");
		
		Assert.assertNotNull(result);
		Assert.assertEquals("GC", result.getShortName());
	}
	
	/**
	 * @verifies return null when service type is null
	 * @see IBillableItemsService#findByServiceType(Concept)
	 */
	@Test
	public void findByServiceType_shouldReturnNullWhenServiceTypeIsNull() throws Exception {
		BillableService result = service.findByServiceType(null);
		
		Assert.assertNull(result);
	}
	
	// Service type tests commented out - requires proper concept setup in test data
	// Can be enabled once concepts are properly configured in BillableServiceTest.xml
	
	/**
	 * @verifies throw APIException when saving duplicate name
	 * @see IBillableItemsService#save(BillableService)
	 */
	@Test(expected = APIException.class)
	public void save_shouldThrowAPIExceptionWhenSavingDuplicateName() throws Exception {
		BillableService billableService = new BillableService();
		billableService.setName("General Consultation");
		billableService.setShortName("UniqueShortName");
		billableService.setServiceStatus(BillableServiceStatus.ENABLED);
		
		service.save(billableService);
	}
	
	/**
	 * @verifies throw APIException when saving duplicate short name
	 * @see IBillableItemsService#save(BillableService)
	 */
	@Test(expected = APIException.class)
	public void save_shouldThrowAPIExceptionWhenSavingDuplicateShortName() throws Exception {
		BillableService billableService = new BillableService();
		billableService.setName("Unique Service Name");
		billableService.setShortName("GC");
		billableService.setServiceStatus(BillableServiceStatus.ENABLED);
		
		service.save(billableService);
	}
	
	// Service type duplicate test commented out - requires proper concept setup in test data
	// Can be enabled once concepts are properly configured in BillableServiceTest.xml
	
	/**
	 * @verifies allow updating same entity without duplicate error
	 * @see IBillableItemsService#save(BillableService)
	 */
	@Test
	public void save_shouldAllowUpdatingSameEntityWithoutDuplicateError() throws Exception {
		BillableService billableService = service.findByName("General Consultation");
		Assert.assertNotNull(billableService);
		
		// Update the service (keeping the same name)
		billableService.setServiceStatus(BillableServiceStatus.DISABLED);
		
		// Should not throw exception
		BillableService updated = service.save(billableService);
		
		Assert.assertNotNull(updated);
		Assert.assertEquals(BillableServiceStatus.DISABLED, updated.getServiceStatus());
	}
	
	/**
	 * @verifies allow multiple services with null short name
	 * @see IBillableItemsService#save(BillableService)
	 */
	@Test
	public void save_shouldAllowMultipleServicesWithNullShortName() throws Exception {
		BillableService service1 = new BillableService();
		service1.setName("Service Without Short Name 1");
		service1.setShortName(null);
		service1.setServiceStatus(BillableServiceStatus.ENABLED);
		
		BillableService saved1 = service.save(service1);
		Assert.assertNotNull(saved1);
		
		BillableService service2 = new BillableService();
		service2.setName("Service Without Short Name 2");
		service2.setShortName(null);
		service2.setServiceStatus(BillableServiceStatus.ENABLED);
		
		// Should not throw exception
		BillableService saved2 = service.save(service2);
		Assert.assertNotNull(saved2);
	}
	
	/**
	 * @verifies allow multiple services with null service type
	 * @see IBillableItemsService#save(BillableService)
	 */
	@Test
	public void save_shouldAllowMultipleServicesWithNullServiceType() throws Exception {
		BillableService service1 = new BillableService();
		service1.setName("Service Without Type 1");
		service1.setShortName("SWOT1");
		service1.setServiceType(null);
		service1.setServiceStatus(BillableServiceStatus.ENABLED);
		
		BillableService saved1 = service.save(service1);
		Assert.assertNotNull(saved1);
		
		BillableService service2 = new BillableService();
		service2.setName("Service Without Type 2");
		service2.setShortName("SWOT2");
		service2.setServiceType(null);
		service2.setServiceStatus(BillableServiceStatus.ENABLED);
		
		// Should not throw exception
		BillableService saved2 = service.save(service2);
		Assert.assertNotNull(saved2);
	}
	
	/**
	 * @verifies be case insensitive when checking duplicates
	 * @see IBillableItemsService#save(BillableService)
	 */
	@Test(expected = APIException.class)
	public void save_shouldBeCaseInsensitiveWhenCheckingDuplicates() throws Exception {
		BillableService billableService = new BillableService();
		billableService.setName("general consultation");  // Different case
		billableService.setShortName("NewShortName");
		billableService.setServiceStatus(BillableServiceStatus.ENABLED);
		
		service.save(billableService);
	}
	
	/**
	 * @verifies not check voided services for duplicates
	 * @see IBillableItemsService#save(BillableService)
	 */
	@Test
	public void save_shouldNotCheckVoidedServicesForDuplicates() throws Exception {
		// Void an existing service
		BillableService existingService = service.findByName("General Consultation");
		service.voidEntity(existingService, "Testing voided duplicates");
		
		// Should allow creating a new service with the same name as the voided one
		BillableService newService = new BillableService();
		newService.setName("General Consultation");
		newService.setShortName("GC2");
		newService.setServiceStatus(BillableServiceStatus.ENABLED);
		
		BillableService saved = service.save(newService);
		Assert.assertNotNull(saved);
		Assert.assertEquals("General Consultation", saved.getName());
		Assert.assertFalse(saved.isVoided());
	}
}
