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
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.TestOrder;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.BillLineItemService;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

public class OrderBillingEventListenerTest extends BaseModuleContextSensitiveTest {
	
	private BillService billService;
	
	private BillLineItemService lineItemService;
	
	private OrderService orderService;
	
	private ConceptService conceptService;
	
	private EncounterService encounterService;
	
	private OrderBillingEventListener listener;
	
	@BeforeEach
	public void setup() {
		billService = Context.getService(BillService.class);
		lineItemService = Context.getService(BillLineItemService.class);
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
		assertEquals(savedOrder.getId(), lineItem.getOrder().getId());
	}
	
	@Test
	public void shouldNotCreateBillWhenNoBillableServiceMatchesConcept() {
		Concept unmappedConcept = conceptService.getConcept(5089);
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
	public void shouldVoidLineItemWhenOrderIsDiscontinued() {
		Concept testConcept = conceptService.getConcept(5497);
		Encounter encounter = encounterService.getEncounter(3);
		Patient patient = encounter.getPatient();
		
		// 1. Save and bill the original order
		Order originalOrder = saveNewTestOrder(patient, testConcept, encounter);
		listener.processOrder(originalOrder);
		Context.flushSession();
		
		List<Bill> bills = billService.getBillsByPatientUuid(patient.getUuid(), null);
		assertFalse(bills.isEmpty());
		BillLineItem originalLineItem = bills.get(0).getLineItems().get(0);
		assertFalse(originalLineItem.getVoided());
		
		// 2. Discontinue the order
		TestOrder discontinueOrder = new TestOrder();
		discontinueOrder.setPatient(patient);
		discontinueOrder.setConcept(testConcept);
		discontinueOrder.setEncounter(encounter);
		discontinueOrder.setOrderer(Context.getProviderService().getProvider(1));
		discontinueOrder.setCareSetting(orderService.getCareSetting(1));
		discontinueOrder.setOrderType(orderService.getOrderType(2));
		discontinueOrder.setAction(Order.Action.DISCONTINUE);
		discontinueOrder.setPreviousOrder(originalOrder);
		discontinueOrder.setDateActivated(new Date());
		
		Order savedDiscontinue = orderService.saveOrder(discontinueOrder, null);
		Context.flushSession();
		
		// 3. Process the DISCONTINUE order
		listener.processOrder(savedDiscontinue);
		Context.flushSession();
		Context.clearSession();
		
		// 4. Verify the original line item is voided
		BillLineItem reloaded = lineItemService.getBillLineItemByUuid(originalLineItem.getUuid());
		assertNotNull(reloaded);
		assertTrue(reloaded.getVoided(), "Line item should be voided after order is discontinued");
		assertEquals("Order discontinued", reloaded.getVoidReason());
	}
	
	@Test
	public void shouldVoidOldAndCreateNewLineItemWhenOrderIsRevised() {
		Concept testConcept = conceptService.getConcept(5497);
		Encounter encounter = encounterService.getEncounter(3);
		Patient patient = encounter.getPatient();
		
		// 1. Save and bill the original order
		Order originalOrder = saveNewTestOrder(patient, testConcept, encounter);
		listener.processOrder(originalOrder);
		Context.flushSession();
		
		List<Bill> bills = billService.getBillsByPatientUuid(patient.getUuid(), null);
		assertFalse(bills.isEmpty());
		String originalLineItemUuid = bills.get(0).getLineItems().get(0).getUuid();
		
		// 2. Revise the order
		TestOrder reviseOrder = new TestOrder();
		reviseOrder.setPatient(patient);
		reviseOrder.setConcept(testConcept);
		reviseOrder.setEncounter(encounter);
		reviseOrder.setOrderer(Context.getProviderService().getProvider(1));
		reviseOrder.setCareSetting(orderService.getCareSetting(1));
		reviseOrder.setOrderType(orderService.getOrderType(2));
		reviseOrder.setAction(Order.Action.REVISE);
		reviseOrder.setPreviousOrder(originalOrder);
		reviseOrder.setDateActivated(new Date());
		
		Order savedRevise = orderService.saveOrder(reviseOrder, null);
		Context.flushSession();
		
		// 3. Process the REVISE order
		listener.processOrder(savedRevise);
		Context.flushSession();
		Context.clearSession();
		
		// 4. Verify the original line item is voided
		BillLineItem voidedLineItem = lineItemService.getBillLineItemByUuid(originalLineItemUuid);
		assertNotNull(voidedLineItem);
		assertTrue(voidedLineItem.getVoided(), "Original line item should be voided after revision");
		assertEquals("Order revised", voidedLineItem.getVoidReason());
		
		// 5. Verify a new bill was created with a new line item
		List<Bill> updatedBills = billService.getBillsByPatientUuid(patient.getUuid(), null);
		assertTrue(updatedBills.size() >= 2, "A new bill should be created for the revised order");
		
		Bill newBill = updatedBills.stream().filter(b -> !b.getId().equals(bills.get(0).getId())).findFirst().orElse(null);
		assertNotNull(newBill);
		assertFalse(newBill.getLineItems().isEmpty());
		
		BillLineItem newLineItem = newBill.getLineItems().get(0);
		assertFalse(newLineItem.getVoided());
		assertEquals(savedRevise.getId(), newLineItem.getOrder().getId());
		assertEquals(new BigDecimal("75.00"), newLineItem.getPrice());
	}
	
	@Test
	public void shouldHandleDiscontinueGracefullyWhenNoOriginalBillExists() {
		Concept testConcept = conceptService.getConcept(5497);
		Encounter encounter = encounterService.getEncounter(3);
		Patient patient = encounter.getPatient();
		
		// Save a NEW order but do NOT process it through billing
		Order originalOrder = saveNewTestOrder(patient, testConcept, encounter);
		
		TestOrder discontinueOrder = new TestOrder();
		discontinueOrder.setPatient(patient);
		discontinueOrder.setConcept(testConcept);
		discontinueOrder.setEncounter(encounter);
		discontinueOrder.setOrderer(Context.getProviderService().getProvider(1));
		discontinueOrder.setCareSetting(orderService.getCareSetting(1));
		discontinueOrder.setOrderType(orderService.getOrderType(2));
		discontinueOrder.setAction(Order.Action.DISCONTINUE);
		discontinueOrder.setPreviousOrder(originalOrder);
		discontinueOrder.setDateActivated(new Date());
		
		Order savedDiscontinue = orderService.saveOrder(discontinueOrder, null);
		Context.flushSession();
		
		// Should not throw — just log a warning
		listener.processOrder(savedDiscontinue);
		Context.flushSession();
		
		List<Bill> bills = billService.getBillsByPatientUuid(patient.getUuid(), null);
		assertTrue(bills == null || bills.isEmpty(), "No bill should exist");
	}
	
	@Test
	public void strategyLookup_shouldFindRegisteredStrategies() {
		List<OrderBillingStrategy> strategies = Context.getRegisteredComponents(OrderBillingStrategy.class);
		assertNotNull(strategies);
		assertFalse(strategies.isEmpty());
		
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
	
	@Test
	public void shouldCreateBillWhenDrugOrderIsSaved() {
		Encounter encounter = encounterService.getEncounter(3);
		Patient patient = encounter.getPatient();
		Drug drug = Context.getConceptService().getDrug(2); // Triomune-30, linked to stock_item_id=100
		
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setPatient(patient);
		drugOrder.setDrug(drug);
		drugOrder.setConcept(drug.getConcept());
		drugOrder.setEncounter(encounter);
		drugOrder.setOrderer(Context.getProviderService().getProvider(1));
		drugOrder.setCareSetting(orderService.getCareSetting(1));
		drugOrder.setOrderType(orderService.getOrderType(1));
		drugOrder.setDateActivated(new Date());
		drugOrder.setQuantity(5.0);
		drugOrder.setQuantityUnits(conceptService.getConcept(51));
		drugOrder.setDose(1.0);
		drugOrder.setDoseUnits(conceptService.getConcept(51));
		drugOrder.setRoute(conceptService.getConcept(22));
		drugOrder.setFrequency(orderService.getOrderFrequency(1));
		drugOrder.setDosingType(org.openmrs.SimpleDosingInstructions.class);
		drugOrder.setNumRefills(0);
		
		Order savedOrder = orderService.saveOrder(drugOrder, null);
		assertNotNull(savedOrder.getId());
		Context.flushSession();
		
		listener.processOrder(savedOrder);
		Context.flushSession();
		
		List<Bill> bills = billService.getBillsByPatientUuid(patient.getUuid(), null);
		assertNotNull(bills);
		assertFalse(bills.isEmpty(), "A bill should have been created for the drug order");
		
		Bill bill = bills.get(0);
		assertEquals(BillStatus.PENDING, bill.getStatus());
		
		BillLineItem lineItem = bill.getLineItems().get(0);
		assertNotNull(lineItem.getItem(), "Line item should reference a stock item");
		assertEquals(BillStatus.PENDING, lineItem.getPaymentStatus());
		assertEquals(5, lineItem.getQuantity());
		assertEquals(new BigDecimal("150.00"), lineItem.getPrice());
		assertEquals(savedOrder.getId(), lineItem.getOrder().getId());
	}
	
	@Test
	public void shouldNotCreateBillForDrugOrderWhenNoStockItemExists() {
		Encounter encounter = encounterService.getEncounter(3);
		Patient patient = encounter.getPatient();
		Drug drug = Context.getConceptService().getDrug(3); // Aspirin — no stock item in test data
		
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setPatient(patient);
		drugOrder.setDrug(drug);
		drugOrder.setConcept(drug.getConcept());
		drugOrder.setEncounter(encounter);
		drugOrder.setOrderer(Context.getProviderService().getProvider(1));
		drugOrder.setCareSetting(orderService.getCareSetting(2));
		drugOrder.setOrderType(orderService.getOrderType(1));
		drugOrder.setDateActivated(new Date());
		drugOrder.setQuantity(2.0);
		drugOrder.setQuantityUnits(conceptService.getConcept(51));
		drugOrder.setDose(1.0);
		drugOrder.setDoseUnits(conceptService.getConcept(51));
		drugOrder.setRoute(conceptService.getConcept(22));
		drugOrder.setFrequency(orderService.getOrderFrequency(1));
		drugOrder.setDosingType(org.openmrs.SimpleDosingInstructions.class);
		drugOrder.setNumRefills(0);
		
		Order savedOrder = orderService.saveOrder(drugOrder, null);
		Context.flushSession();
		
		listener.processOrder(savedOrder);
		Context.flushSession();
		
		List<Bill> bills = billService.getBillsByPatientUuid(patient.getUuid(), null);
		assertTrue(bills == null || bills.isEmpty(), "No bill should be created when no stock item matches the drug");
	}
	
	@Test
	public void shouldNotCreateDuplicateBillForSameOrder() {
		Concept testConcept = conceptService.getConcept(5497);
		Encounter encounter = encounterService.getEncounter(3);
		Patient patient = encounter.getPatient();
		
		Order savedOrder = saveNewTestOrder(patient, testConcept, encounter);
		
		// Process the same order twice
		listener.processOrder(savedOrder);
		Context.flushSession();
		listener.processOrder(savedOrder);
		Context.flushSession();
		
		List<Bill> bills = billService.getBillsByPatientUuid(patient.getUuid(), null);
		assertNotNull(bills);
		assertEquals(1, bills.size(), "Only one bill should exist — second call should be idempotent");
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
