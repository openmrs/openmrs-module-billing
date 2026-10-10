/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.TestOrder;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.billing.BillingModuleActivator;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.BillLineItemService;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillLineItemStatus;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.base.BaseModuleContextTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests that saving an order bills it once its transaction commits. These run without a
 * test-managed transaction, because the order is only billed after the transaction that saved it
 * commits, on a daemon thread with a session of its own. Everything a test commits is deleted after
 * it.
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OrderBillingOnCommitTest extends BaseModuleContextTest {
	
	private static final long BILLING_TIMEOUT_MILLIS = 10000;
	
	private static final long NO_BILLING_WAIT_MILLIS = 2000;
	
	private static final int BILLED_TEST_CONCEPT_ID = 5497;
	
	private OrderService orderService;
	
	private BillLineItemService lineItemService;
	
	@BeforeEach
	public void setup() throws SQLException {
		orderService = Context.getOrderService();
		lineItemService = Context.getService(BillLineItemService.class);
		
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "StockOperationType.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "OrderBillingTest.xml");
		getConnection().commit();
		
		// Start the module's listeners the way ModuleFactory does: hand the activator a daemon token, then refresh
		BillingModuleActivator activator = new BillingModuleActivator();
		Module module = new Module("billing");
		module.setModuleId("billing");
		module.setModuleActivator(activator);
		ReflectionTestUtils.invokeMethod(ModuleFactory.class, "passDaemonToken", module);
		activator.contextRefreshed();
	}
	
	@AfterEach
	public void deleteCommittedData() {
		deleteAllData();
	}
	
	@Test
	public void shouldBillANewOrderOnceItsTransactionCommits() {
		Order order = saveNewTestOrder();
		
		BillLineItem lineItem = awaitLineItemFor(order);
		
		assertNotNull(lineItem, "The committed order should have been billed");
		assertEquals(BillLineItemStatus.PENDING, lineItem.getStatus());
		assertEquals(BillStatus.PENDING, lineItem.getBill().getStatus());
		assertEquals(order.getPatient().getId(), lineItem.getBill().getPatient().getId());
		assertEquals(new BigDecimal("75.00"), lineItem.getPrice());
	}
	
	@Test
	public void shouldVoidTheLineItemOfAnOrderDiscontinuedThroughTheOrderService() {
		Order order = saveNewTestOrder();
		BillLineItem lineItem = awaitLineItemFor(order);
		assertNotNull(lineItem, "The committed order should have been billed");
		
		// discontinueOrder saves the new DISCONTINUE order inside OrderService, not through saveOrder
		Order saved = orderService.getOrderByUuid(order.getUuid());
		orderService.discontinueOrder(saved, "Not needed", null, Context.getProviderService().getProvider(1),
		    saved.getEncounter());
		
		BillLineItem voided = await(() -> {
			BillLineItem reloaded = lineItemService.getBillLineItemByUuid(lineItem.getUuid());
			return reloaded.getVoided() ? reloaded : null;
		});
		assertNotNull(voided, "Discontinuing the order should void its line item");
		assertEquals("Order discontinued", voided.getVoidReason());
	}
	
	@Test
	public void shouldNotBillAnOrderAgainWhenTheOrderIsUpdated() throws InterruptedException {
		Order order = saveNewTestOrder();
		BillLineItem lineItem = awaitLineItemFor(order);
		assertNotNull(lineItem, "The committed order should have been billed");
		lineItemService.voidBillLineItem(lineItem, "Voided by the test");
		
		// an update of an existing order, not a new one
		orderService.voidOrder(orderService.getOrderByUuid(order.getUuid()), "Voided by the test");
		
		Thread.sleep(NO_BILLING_WAIT_MILLIS);
		Context.clearSession();
		assertNull(lineItemService.getBillLineItemByOrder(orderService.getOrderByUuid(order.getUuid())),
		    "Updating an order must not bill it again");
	}
	
	private Order saveNewTestOrder() {
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		
		TestOrder testOrder = new TestOrder();
		testOrder.setPatient(encounter.getPatient());
		testOrder.setConcept(Context.getConceptService().getConcept(BILLED_TEST_CONCEPT_ID));
		testOrder.setEncounter(encounter);
		testOrder.setOrderer(Context.getProviderService().getProvider(1));
		testOrder.setCareSetting(orderService.getCareSetting(1));
		testOrder.setOrderType(orderService.getOrderType(2));
		testOrder.setDateActivated(new Date());
		
		return orderService.saveOrder(testOrder, null);
	}
	
	private BillLineItem awaitLineItemFor(Order order) {
		return await(() -> lineItemService.getBillLineItemByOrder(orderService.getOrderByUuid(order.getUuid())));
	}
	
	private static <T> T await(Supplier<T> probe) {
		long deadline = System.currentTimeMillis() + BILLING_TIMEOUT_MILLIS;
		while (true) {
			Context.clearSession();
			T result = probe.get();
			if (result != null || System.currentTimeMillis() > deadline) {
				return result;
			}
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			}
		}
	}
}
