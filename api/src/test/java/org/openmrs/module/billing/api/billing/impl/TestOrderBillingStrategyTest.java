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
package org.openmrs.module.billing.api.billing.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.TestOrder;
import org.openmrs.module.billing.api.BillExemptionService;
import org.openmrs.module.billing.api.BillableServiceService;
import org.openmrs.module.billing.api.ItemPriceService;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.openmrs.module.billing.api.search.BillableServiceSearch;

@ExtendWith(MockitoExtension.class)
public class TestOrderBillingStrategyTest {
	
	@Mock
	private BillableServiceService billableServiceService;
	
	@Mock
	private ItemPriceService itemPriceService;
	
	@Mock
	private BillExemptionService billExemptionService;
	
	@InjectMocks
	private TestOrderBillingStrategy strategy;
	
	private Concept concept;
	
	private TestOrder testOrder;
	
	private BillableService billableService;
	
	@BeforeEach
	public void setup() {
		concept = new Concept();
		concept.setUuid("test-concept-uuid");
		
		testOrder = new TestOrder();
		testOrder.setConcept(concept);
		testOrder.setUuid("test-order-uuid");
		
		billableService = new BillableService();
		billableService.setUuid("test-service-uuid");
	}
	
	@Test
	public void supportsOrder_shouldReturnTrueForTestOrder() {
		assertTrue(strategy.supportsOrder(testOrder));
	}
	
	@Test
	public void supportsOrder_shouldReturnFalseForDrugOrder() {
		assertFalse(strategy.supportsOrder(new DrugOrder()));
	}
	
	@Test
	public void createBillLineItem_shouldCreateLineItemWithServicePrice() {
		CashierItemPrice servicePrice = new CashierItemPrice();
		servicePrice.setPrice(new BigDecimal("75.00"));
		
		when(billableServiceService.getBillableServices(any(BillableServiceSearch.class), isNull()))
		        .thenReturn(Collections.singletonList(billableService));
		when(itemPriceService.getServicePrice(billableService)).thenReturn(Collections.singletonList(servicePrice));
		
		Optional<BillLineItem> result = strategy.createBillLineItem(testOrder);
		
		assertTrue(result.isPresent());
		BillLineItem lineItem = result.get();
		assertEquals(new BigDecimal("75.00"), lineItem.getPrice());
		assertEquals(1, lineItem.getQuantity());
		assertEquals(BillStatus.PENDING, lineItem.getPaymentStatus());
		assertEquals(billableService, lineItem.getBillableService());
	}
	
	@Test
	public void createBillLineItem_shouldReturnZeroPriceWhenNoPriceConfigured() {
		when(billableServiceService.getBillableServices(any(BillableServiceSearch.class), isNull()))
		        .thenReturn(Collections.singletonList(billableService));
		when(itemPriceService.getServicePrice(billableService)).thenReturn(Collections.emptyList());
		
		Optional<BillLineItem> result = strategy.createBillLineItem(testOrder);
		
		assertTrue(result.isPresent());
		assertEquals(BigDecimal.ZERO, result.get().getPrice());
	}
	
	@Test
	public void createBillLineItem_shouldReturnEmptyWhenNoBillableServiceFound() {
		when(billableServiceService.getBillableServices(any(BillableServiceSearch.class), isNull()))
		        .thenReturn(Collections.emptyList());
		
		Optional<BillLineItem> result = strategy.createBillLineItem(testOrder);
		
		assertFalse(result.isPresent());
	}
	
	@Test
	public void createBillLineItem_shouldReturnEmptyWhenConceptIsNull() {
		testOrder.setConcept(null);
		
		Optional<BillLineItem> result = strategy.createBillLineItem(testOrder);
		
		assertFalse(result.isPresent());
	}
}
