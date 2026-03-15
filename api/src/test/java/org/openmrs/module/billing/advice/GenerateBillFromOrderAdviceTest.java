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
package org.openmrs.module.billing.advice;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.TestOrder;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link GenerateBillFromOrderAdvice}.
 * <p>
 * The advice is invoked directly (not via Spring AOP proxy) so that tests run without requiring the
 * module's config.xml advice registration. Services are resolved from the live Spring context, and
 * assertions query the H2 in-memory database.
 * <p>
 * Test data requirements (loaded in {@link #setup()}):
 * <ul>
 * <li>CoreTest-2.0.xml — patients (id=0,1), providers, locations
 * <li>StockOperationType.xml — stock-management tables required at runtime
 * <li>CashPointTest.xml — at least one cash point for bill creation
 * <li>GenerateBillFromOrderAdviceTest.xml — provider linked to admin person, CBC billable service
 * </ul>
 */
public class GenerateBillFromOrderAdviceTest extends BaseModuleContextSensitiveTest {
	
	/** UUID of the lab concept defined in GenerateBillFromOrderAdviceTest.xml. */
	private static final String LAB_CONCEPT_UUID = "cbc00000-0000-0000-0000-000000000001";
	
	/** Expected price for the CBC BillableService defined in the test dataset. */
	private static final BigDecimal EXPECTED_PRICE = new BigDecimal("500.00");
	
	private BillService billService;
	
	private PatientService patientService;
	
	private ConceptService conceptService;
	
	private GenerateBillFromOrderAdvice advice;
	
	private Method saveOrderMethod;
	
	private Patient patient;
	
	private Concept labConcept;
	
	@BeforeEach
	public void setup() throws Exception {
		billService = Context.getService(BillService.class);
		patientService = Context.getPatientService();
		conceptService = Context.getConceptService();
		
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "StockOperationType.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "GenerateBillFromOrderAdviceTest.xml");
		
		advice = new GenerateBillFromOrderAdvice();
		
		// Locate saveOrder on OrderService — the advice matches on method name only,
		// so any overload will do.
		saveOrderMethod = Arrays.stream(OrderService.class.getMethods()).filter(m -> m.getName().equals("saveOrder"))
		        .findFirst().orElseThrow(() -> new IllegalStateException("saveOrder not found on OrderService"));
		
		patient = patientService.getPatient(0);
		labConcept = conceptService.getConceptByUuid(LAB_CONCEPT_UUID);
	}
	
	/**
	 * @see GenerateBillFromOrderAdvice#afterReturning(Object, Method, Object[], Object)
	 */
	@Test
	public void afterReturning_withTestOrderAndMatchingBillableService_shouldCreateOnePendingBill() {
		TestOrder testOrder = new TestOrder();
		testOrder.setAction(Order.Action.NEW);
		testOrder.setPatient(patient);
		testOrder.setConcept(labConcept);
		testOrder.setDateActivated(new Date());
		
		advice.afterReturning(null, saveOrderMethod, new Object[] { testOrder }, null);
		Context.flushSession();
		Context.clearSession();
		
		List<Bill> bills = billService.getBillsByPatientUuid(patient.getUuid(), null);
		
		assertEquals(1, bills.size(), "Expected exactly one bill to be created for the TestOrder");
		
		Bill bill = bills.get(0);
		assertEquals(BillStatus.PENDING, bill.getStatus());
		assertNotNull(bill.getCashier(), "Bill must have a cashier");
		assertNotNull(bill.getCashPoint(), "Bill must have a cash point");
		
		assertEquals(1, bill.getLineItems().size(), "Bill must have exactly one line item");
		
		BillLineItem lineItem = bill.getLineItems().get(0);
		assertEquals(0, EXPECTED_PRICE.compareTo(lineItem.getPrice()),
		    "Line item price must match the configured CashierItemPrice");
		assertEquals(1, lineItem.getQuantity().intValue(), "TestOrder quantity must be 1");
		assertEquals(BillStatus.PENDING, lineItem.getPaymentStatus());
		assertNotNull(lineItem.getBillableService(), "Line item must reference the BillableService");
		assertEquals(LAB_CONCEPT_UUID, lineItem.getBillableService().getConcept().getUuid());
	}
	
	/**
	 * @see GenerateBillFromOrderAdvice#afterReturning(Object, Method, Object[], Object)
	 */
	@Test
	public void afterReturning_withTestOrderAndNoMatchingBillableService_shouldNotCreateBill() {
		// Use an in-memory Concept with a UUID that has no BillableService in the DB.
		Concept unknownConcept = new Concept();
		unknownConcept.setUuid("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
		
		TestOrder testOrder = new TestOrder();
		testOrder.setAction(Order.Action.NEW);
		testOrder.setPatient(patient);
		testOrder.setConcept(unknownConcept);
		testOrder.setDateActivated(new Date());
		
		advice.afterReturning(null, saveOrderMethod, new Object[] { testOrder }, null);
		Context.flushSession();
		
		List<Bill> bills = billService.getBillsByPatientUuid(patient.getUuid(), null);
		assertTrue(bills.isEmpty(), "No bill should be created when no BillableService matches the concept");
	}
	
	/**
	 * @see GenerateBillFromOrderAdvice#afterReturning(Object, Method, Object[], Object)
	 */
	@Test
	public void afterReturning_withDiscontinueTestOrder_shouldNotCreateBill() {
		TestOrder testOrder = new TestOrder();
		testOrder.setAction(Order.Action.DISCONTINUE);
		testOrder.setPatient(patient);
		testOrder.setConcept(labConcept);
		testOrder.setDateActivated(new Date());
		
		advice.afterReturning(null, saveOrderMethod, new Object[] { testOrder }, null);
		Context.flushSession();
		
		List<Bill> bills = billService.getBillsByPatientUuid(patient.getUuid(), null);
		assertTrue(bills.isEmpty(), "DISCONTINUE orders must be skipped — no bill should be created");
	}
	
	/**
	 * Regression test: the DrugOrder code path must not throw and must not create a spurious bill when
	 * the drug has no matching StockItem (e.g. drug is null, or drug is not in stock management).
	 * <p>
	 * A full positive DrugOrder test (drug → StockItem → bill) requires additional stock-management
	 * test data and is tracked separately from O3-5371, which targets lab/TestOrder billing.
	 *
	 * @see GenerateBillFromOrderAdvice#afterReturning(Object, Method, Object[], Object)
	 */
	@Test
	public void afterReturning_withDrugOrderAndNoMatchingStockItem_shouldNotCreateBill() {
		// drug == null → drugID = 0 → getStockItemByDrug(0) returns empty → advice skips bill creation
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setAction(Order.Action.NEW);
		drugOrder.setPatient(patient);
		drugOrder.setDateActivated(new Date());
		// intentionally leave drug as null
		
		advice.afterReturning(null, saveOrderMethod, new Object[] { drugOrder }, null);
		Context.flushSession();
		
		List<Bill> bills = billService.getBillsByPatientUuid(patient.getUuid(), null);
		assertTrue(bills.isEmpty(), "No bill should be created when the drug has no matching StockItem");
	}
}
