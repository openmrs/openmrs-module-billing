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
package org.openmrs.module.billing.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

public class BillLineItemServiceTest extends BaseModuleContextSensitiveTest {
	
	private BillLineItemService lineItemService;
	
	private BillService billService;
	
	private OrderService orderService;
	
	private ConceptService conceptService;
	
	private EncounterService encounterService;
	
	private CashPointService cashPointService;
	
	@BeforeEach
	public void setup() {
		lineItemService = Context.getService(BillLineItemService.class);
		billService = Context.getService(BillService.class);
		orderService = Context.getOrderService();
		conceptService = Context.getConceptService();
		encounterService = Context.getEncounterService();
		cashPointService = Context.getService(CashPointService.class);
		
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "StockOperationType.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "OrderBillingTest.xml");
	}
	
	@Test
	public void voidBillLineItem_shouldVoidLineItem() {
		Bill bill = createBillWithLineItem();
		BillLineItem lineItem = bill.getLineItems().get(0);
		assertFalse(lineItem.getVoided());
		
		lineItemService.voidBillLineItem(lineItem, "Test void reason");
		
		BillLineItem reloaded = lineItemService.getBillLineItemByUuid(lineItem.getUuid());
		assertNotNull(reloaded);
		assertTrue(reloaded.getVoided());
		assertEquals("Test void reason", reloaded.getVoidReason());
		assertNotNull(reloaded.getDateVoided());
		assertNotNull(reloaded.getVoidedBy());
	}
	
	@Test
	public void voidBillLineItem_shouldThrowWhenReasonIsBlank() {
		Bill bill = createBillWithLineItem();
		BillLineItem lineItem = bill.getLineItems().get(0);
		
		assertThrows(IllegalArgumentException.class, () -> lineItemService.voidBillLineItem(lineItem, ""));
		assertThrows(IllegalArgumentException.class, () -> lineItemService.voidBillLineItem(lineItem, null));
	}
	
	@Test
	public void voidBillLineItem_shouldMakeLineItemNotFoundByOrder() {
		Encounter encounter = encounterService.getEncounter(3);
		Patient patient = encounter.getPatient();
		Order order = saveNewTestOrder(patient, conceptService.getConcept(5497), encounter);
		
		Bill bill = createBillWithLineItemForOrder(order);
		BillLineItem lineItem = bill.getLineItems().get(0);
		
		BillLineItem found = lineItemService.getBillLineItemByOrder(order);
		assertNotNull(found);
		
		lineItemService.voidBillLineItem(lineItem, "Testing void");
		
		BillLineItem afterVoid = lineItemService.getBillLineItemByOrder(order);
		assertNull(afterVoid);
	}
	
	@Test
	public void getBillLineItemByOrder_shouldReturnLineItemForOrder() {
		Encounter encounter = encounterService.getEncounter(3);
		Patient patient = encounter.getPatient();
		Order order = saveNewTestOrder(patient, conceptService.getConcept(5497), encounter);
		
		createBillWithLineItemForOrder(order);
		
		BillLineItem found = lineItemService.getBillLineItemByOrder(order);
		assertNotNull(found);
		assertEquals(order.getId(), found.getOrder().getId());
	}
	
	@Test
	public void getBillLineItemByOrder_shouldReturnNullWhenNoLineItemExists() {
		Encounter encounter = encounterService.getEncounter(3);
		Patient patient = encounter.getPatient();
		Order order = saveNewTestOrder(patient, conceptService.getConcept(5497), encounter);
		
		BillLineItem found = lineItemService.getBillLineItemByOrder(order);
		assertNull(found);
	}
	
	private Bill createBillWithLineItem() {
		List<CashPoint> cashPoints = cashPointService.getAllCashPoints(false);
		
		BillLineItem lineItem = new BillLineItem();
		lineItem.setPrice(new BigDecimal("100.00"));
		lineItem.setQuantity(1);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		lineItem.setLineItemOrder(0);
		
		Bill bill = new Bill();
		bill.setPatient(Context.getPatientService().getPatient(2));
		bill.setStatus(BillStatus.PENDING);
		bill.setCashier(Context.getProviderService().getProvider(1));
		bill.setCashPoint(cashPoints.get(0));
		bill.addLineItem(lineItem);
		
		return billService.saveBill(bill);
	}
	
	private Bill createBillWithLineItemForOrder(Order order) {
		List<CashPoint> cashPoints = cashPointService.getAllCashPoints(false);
		
		BillLineItem lineItem = new BillLineItem();
		lineItem.setPrice(new BigDecimal("75.00"));
		lineItem.setQuantity(1);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		lineItem.setLineItemOrder(0);
		lineItem.setOrder(order);
		
		Bill bill = new Bill();
		bill.setPatient(order.getPatient());
		bill.setStatus(BillStatus.PENDING);
		bill.setCashier(Context.getProviderService().getProvider(1));
		bill.setCashPoint(cashPoints.get(0));
		bill.addLineItem(lineItem);
		
		return billService.saveBill(bill);
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
		
		return savedOrder;
	}
}
