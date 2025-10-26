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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.IBillService;
import org.openmrs.module.billing.api.IBillableItemsService;
import org.openmrs.module.billing.api.ICashPointService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.base.BaseModuleContextTest;

/**
 * Tests for {@link org.openmrs.module.billing.api.impl.BillServiceImpl}.
 */
public class BillServiceImplTest extends BaseModuleContextTest {
	
	private IBillService billService;
	
	private IBillableItemsService billableItemsService;
	
	private ICashPointService cashPointService;
	
	private ProviderService providerService;
	
	private PatientService patientService;
	
	@Before
	public void before() throws Exception {
		billService = Context.getService(IBillService.class);
		billableItemsService = Context.getService(IBillableItemsService.class);
		cashPointService = Context.getService(ICashPointService.class);
		providerService = Context.getProviderService();
		patientService = Context.getPatientService();
		
		executeDataSet(TestConstants.CORE_DATASET);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillTest.xml");
	}
	
	/**
	 * Test for O3-5147: Verifies that after the fix, saving a Bill with multiple line items does NOT
	 * throw a ConcurrentModificationException when editing line items (previously it did due to
	 * iterating and modifying a collection simultaneously in the save method).
	 * 
	 * @verifies not throw ConcurrentModificationException when editing line items
	 */
	@Test
	public void save_shouldNotThrowConcurrentModificationExceptionWhenEditingLineItems() throws Exception {
		// Create a valid Bill entity with at least two line items
		Bill bill = createBillWithMultipleLineItems();
		
		// Save the initial bill
		Bill savedBill = billService.save(bill);
		Context.flushSession();
		Context.clearSession();
		
		// Reload the bill
		Bill reloadedBill = billService.getById(savedBill.getId());
		Assert.assertNotNull(reloadedBill);
		Assert.assertEquals(3, reloadedBill.getLineItems().size());
		
		// Update the quantity of one line item
		BillLineItem firstItem = reloadedBill.getLineItems().get(0);
		int originalQuantity = firstItem.getQuantity();
		firstItem.setQuantity(originalQuantity + 5);
		
		// Add a new line item
		List<BillableService> services = getOrCreateBillableServices();
		BillLineItem newLineItem = new BillLineItem();
		newLineItem.setBillableService(services.get(0));
		newLineItem.setPrice(BigDecimal.valueOf(75.00));
		newLineItem.setPriceName("New Service Price");
		newLineItem.setQuantity(2);
		newLineItem.setPaymentStatus(BillStatus.PENDING);
		newLineItem.setBill(reloadedBill);
		reloadedBill.addLineItem(newLineItem);
		
		// Remove one line item
		BillLineItem itemToRemove = reloadedBill.getLineItems().get(1);
		reloadedBill.removeLineItem(itemToRemove);
		
		// Save the bill - this should NOT throw ConcurrentModificationException
		// The fix uses a temporary list to avoid modifying the collection while iterating
		Bill updatedBill = null;
		try {
			updatedBill = billService.save(reloadedBill);
			Context.flushSession();
		}
		catch (java.util.ConcurrentModificationException e) {
			Assert.fail("ConcurrentModificationException should not be thrown after O3-5147 fix: " + e.getMessage());
		}
		
		// Verify no exceptions were thrown and the bill was saved successfully
		Assert.assertNotNull("Bill should be saved successfully", updatedBill);
		
		// Flush and reload to verify persistence
		Context.clearSession();
		Bill finalBill = billService.getById(updatedBill.getId());
		
		// Assert that the line items have the correct count
		// Started with 3, removed 1, added 1 = 3 total
		Assert.assertNotNull(finalBill);
		Assert.assertEquals("Line items should have correct count after modifications", 3, finalBill.getLineItems().size());
		
		// Assert that the updated quantity is persisted
		boolean foundUpdatedItem = false;
		for (BillLineItem item : finalBill.getLineItems()) {
			if (item.getId().equals(firstItem.getId())) {
				Assert.assertEquals("Updated quantity should be persisted", originalQuantity + 5,
				    item.getQuantity().intValue());
				foundUpdatedItem = true;
				break;
			}
		}
		Assert.assertTrue("Should find the updated line item", foundUpdatedItem);
	}
	
	/**
	 * Helper method to create a Bill with multiple line items for testing.
	 * 
	 * @return A Bill entity with at least 3 line items
	 */
	private Bill createBillWithMultipleLineItems() {
		Bill bill = new Bill();
		
		// Set required fields
		Provider cashier = providerService.getProvider(0);
		Patient patient = patientService.getPatient(0);
		CashPoint cashPoint = cashPointService.getById(0);
		
		bill.setCashier(cashier);
		bill.setPatient(patient);
		bill.setCashPoint(cashPoint);
		bill.setReceiptNumber("TEST-RN-" + System.currentTimeMillis());
		bill.setStatus(BillStatus.PENDING);
		
		// Initialize line items list
		bill.setLineItems(new ArrayList<BillLineItem>());
		
		// Create and add multiple line items using BillableService
		// We'll create the services if they don't exist
		List<BillableService> services = getOrCreateBillableServices();
		
		// Add first line item
		BillLineItem lineItem1 = createLineItemForService(services.get(0), 2, BigDecimal.valueOf(50.00));
		lineItem1.setBill(bill);
		bill.addLineItem(lineItem1);
		
		// Add second line item
		BillLineItem lineItem2 = createLineItemForService(services.get(1), 1, BigDecimal.valueOf(100.00));
		lineItem2.setBill(bill);
		bill.addLineItem(lineItem2);
		
		// Add third line item
		BillLineItem lineItem3 = createLineItemForService(services.get(2), 3, BigDecimal.valueOf(25.00));
		lineItem3.setBill(bill);
		bill.addLineItem(lineItem3);
		
		return bill;
	}
	
	/**
	 * Helper method to get or create test BillableServices.
	 * 
	 * @return A list of at least 3 BillableServices
	 */
	private List<BillableService> getOrCreateBillableServices() {
		List<BillableService> services = billableItemsService.getAll(false);
		
		// If we don't have enough services, create them
		while (services.size() < 3) {
			BillableService service = new BillableService();
			service.setName("Test Service " + (services.size() + 1));
			service.setShortName("TS" + (services.size() + 1));
			service = billableItemsService.save(service);
			services.add(service);
		}
		
		Context.flushSession();
		return services;
	}
	
	/**
	 * Helper method to create a BillLineItem for a given BillableService.
	 * 
	 * @param service The BillableService
	 * @param quantity The quantity
	 * @param price The price
	 * @return A configured BillLineItem
	 */
	private BillLineItem createLineItemForService(BillableService service, int quantity, BigDecimal price) {
		BillLineItem lineItem = new BillLineItem();
		lineItem.setBillableService(service);
		lineItem.setPrice(price);
		lineItem.setPriceName("Standard Price");
		lineItem.setQuantity(quantity);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		
		return lineItem;
	}
}
