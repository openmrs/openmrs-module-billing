/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.billing.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.TestOrder;
import org.openmrs.Visit;
import org.openmrs.module.billing.api.BillExemptionService;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.BillableServiceService;
import org.openmrs.module.billing.api.CashPointService;
import org.openmrs.module.billing.api.ItemPriceService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.CashPoint;
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
	
	@Mock
	private BillService billService;
	
	@Mock
	private CashPointService cashPointService;
	
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
	
	@Test
	public void createBill_shouldInheritVisitFromOrderEncounter() {
		Patient patient = mock(Patient.class);
		Order order = mock(Order.class);
		Encounter encounter = mock(Encounter.class);
		Visit visit = mock(Visit.class);
		Provider cashier = mock(Provider.class);
		CashPoint cashPoint = mock(CashPoint.class);
		BillLineItem lineItem = mock(BillLineItem.class);
		
		when(order.getEncounter()).thenReturn(encounter);
		when(encounter.getVisit()).thenReturn(visit);
		when(order.getOrderer()).thenReturn(cashier);
		when(cashPointService.getAllCashPoints(false)).thenReturn(Collections.singletonList(cashPoint));
		when(billService.saveBill(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));
		
		strategy.createBill(patient, lineItem, order);
		
		ArgumentCaptor<Bill> captor = ArgumentCaptor.forClass(Bill.class);
		verify(billService).saveBill(captor.capture());
		assertEquals(visit, captor.getValue().getVisit());
	}
	
	@Test
	public void createBill_shouldLeaveVisitNullWhenEncounterHasNoVisit() {
		Patient patient = mock(Patient.class);
		Order order = mock(Order.class);
		Encounter encounter = mock(Encounter.class);
		Provider cashier = mock(Provider.class);
		CashPoint cashPoint = mock(CashPoint.class);
		BillLineItem lineItem = mock(BillLineItem.class);
		
		lenient().when(order.getEncounter()).thenReturn(encounter);
		lenient().when(encounter.getVisit()).thenReturn(null);
		when(order.getOrderer()).thenReturn(cashier);
		when(cashPointService.getAllCashPoints(false)).thenReturn(Collections.singletonList(cashPoint));
		when(billService.saveBill(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));
		
		strategy.createBill(patient, lineItem, order);
		
		ArgumentCaptor<Bill> captor = ArgumentCaptor.forClass(Bill.class);
		verify(billService).saveBill(captor.capture());
		assertNull(captor.getValue().getVisit());
	}
}
