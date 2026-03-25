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
package org.openmrs.module.billing.api.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.TestOrder;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

public class OrderBillingEventListenerTest extends BaseModuleContextSensitiveTest {
	
	private BillService billService;
	
	private OrderService orderService;
	
	private ConceptService conceptService;
	
	private EncounterService encounterService;
	
	private OrderBillingEventListener listener;
	
	@BeforeEach
	public void setup() {
		billService = Context.getService(BillService.class);
		orderService = Context.getOrderService();
		conceptService = Context.getConceptService();
		encounterService = Context.getEncounterService();
		listener = Context.getRegisteredComponent("orderBillingEventListener", OrderBillingEventListener.class);
		
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "StockOperationType.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "OrderBillingTest.xml");
	}
	
	@Test
	public void shouldCreateBillWhenTestOrderIsSaved() {
		Concept testConcept = conceptService.getConcept(5497);
		assertNotNull(testConcept);
		
		Encounter encounter = encounterService.getEncounter(3);
		Patient patient = encounter.getPatient();
		
		// 1. Save a test order
		Order savedOrder = saveNewTestOrder(patient, testConcept, encounter);
		
		// 2. Process through the billing pipeline
		listener.processOrder(savedOrder);
		Context.flushSession();
		
		// 3. Verify a bill was created
		List<Bill> bills = billService.getBillsByPatientUuid(patient.getUuid(), null);
		assertNotNull(bills);
		assertFalse(bills.isEmpty(), "A bill should have been created for the patient");
		
		Bill bill = bills.get(0);
		assertEquals(BillStatus.PENDING, bill.getStatus());
		assertEquals(patient.getId(), bill.getPatient().getId());
		
		// 4. Verify the line item
		assertFalse(bill.getLineItems().isEmpty());
		BillLineItem lineItem = bill.getLineItems().get(0);
		assertNotNull(lineItem.getBillableService());
		assertEquals(BillStatus.PENDING, lineItem.getPaymentStatus());
		assertEquals(1, lineItem.getQuantity());
		assertEquals(new BigDecimal("75.00"), lineItem.getPrice());
	}
	
	@Test
	public void shouldNotCreateBillWhenNoBillableServiceMatchesConcept() {
		Concept unmappedConcept = conceptService.getConcept(5089);
		assertNotNull(unmappedConcept);
		
		Encounter encounter = encounterService.getEncounter(3);
		Patient patient = encounter.getPatient();
		
		Order savedOrder = saveNewTestOrder(patient, unmappedConcept, encounter);
		
		listener.processOrder(savedOrder);
		Context.flushSession();
		
		List<Bill> bills = billService.getBillsByPatientUuid(patient.getUuid(), null);
		assertTrue(bills == null || bills.isEmpty(),
		    "No bill should be created when no billable service matches the concept");
	}
	
	@Test
	public void shouldNotCreateBillForDiscontinuedOrder() {
		Concept testConcept = conceptService.getConcept(5497);
		Encounter encounter = encounterService.getEncounter(3);
		Patient patient = encounter.getPatient();
		
		// Save a NEW order first
		Order savedOriginal = saveNewTestOrder(patient, testConcept, encounter);
		
		// Create a DISCONTINUE order
		TestOrder discontinueOrder = new TestOrder();
		discontinueOrder.setPatient(patient);
		discontinueOrder.setConcept(testConcept);
		discontinueOrder.setEncounter(encounter);
		discontinueOrder.setOrderer(Context.getProviderService().getProvider(1));
		discontinueOrder.setCareSetting(orderService.getCareSetting(1));
		discontinueOrder.setOrderType(orderService.getOrderType(2));
		discontinueOrder.setAction(Order.Action.DISCONTINUE);
		discontinueOrder.setPreviousOrder(savedOriginal);
		discontinueOrder.setDateActivated(new Date());
		
		Order savedDiscontinue = orderService.saveOrder(discontinueOrder, null);
		Context.flushSession();
		
		// Process only the DISCONTINUE order — should be ignored
		listener.processOrder(savedDiscontinue);
		Context.flushSession();
		
		List<Bill> bills = billService.getBillsByPatientUuid(patient.getUuid(), null);
		assertTrue(bills == null || bills.isEmpty(), "No bill should be created for a DISCONTINUE order");
	}
	
	@Test
	public void strategyLookup_shouldFindRegisteredStrategies() {
		List<OrderBillingStrategy> strategies = Context.getRegisteredComponents(OrderBillingStrategy.class);
		assertNotNull(strategies);
		assertFalse(strategies.isEmpty(), "At least one billing strategy should be registered");
		
		boolean hasDrugStrategy = false;
		boolean hasTestStrategy = false;
		for (OrderBillingStrategy strategy : strategies) {
			if (strategy.getClass().getSimpleName().equals("DrugOrderBillingStrategy")) {
				hasDrugStrategy = true;
			}
			if (strategy.getClass().getSimpleName().equals("TestOrderBillingStrategy")) {
				hasTestStrategy = true;
			}
		}
		assertTrue(hasDrugStrategy, "DrugOrderBillingStrategy should be registered");
		assertTrue(hasTestStrategy, "TestOrderBillingStrategy should be registered");
	}
	
	private Order saveNewTestOrder(Patient patient, Concept concept, Encounter encounter) {
		TestOrder testOrder = new TestOrder();
		testOrder.setPatient(patient);
		testOrder.setConcept(concept);
		testOrder.setEncounter(encounter);
		testOrder.setOrderer(Context.getProviderService().getProvider(1));
		testOrder.setCareSetting(orderService.getCareSetting(1));
		testOrder.setOrderType(orderService.getOrderType(2));
		testOrder.setDateActivated(new Date());
		
		Order savedOrder = orderService.saveOrder(testOrder, null);
		assertNotNull(savedOrder.getId());
		Context.flushSession();
		return savedOrder;
	}
}
