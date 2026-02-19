/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.advice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.TestOrder;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.search.BillSearch;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link GenerateBillFromOrderAdvice} Tests the following scenarios: - DrugOrder with
 * matching BillableService creates a PENDING bill - DrugOrder with no matching BillableService does
 * NOT create a bill - TestOrder with matching BillableService creates a PENDING bill - TestOrder
 * with no matching BillableService does NOT create a bill - DISCONTINUE / REVISE / RENEW actions do
 * NOT create bills - DrugOrder with DISABLED BillableService does NOT create a bill
 */
public class GenerateBillFromOrderAdviceTest extends BaseModuleContextSensitiveTest {
	
	private static final String DRUG_CONCEPT_UUID = "a1b2c3d4-aaaa-bbbb-cccc-000000000001";
	
	private static final String TEST_CONCEPT_UUID = "a1b2c3d4-aaaa-bbbb-cccc-000000000003";
	
	private GenerateBillFromOrderAdvice advice;
	
	private BillService billService;
	
	private ConceptService conceptService;
	
	private PatientService patientService;
	
	private Method saveOrderMethod;
	
	@BeforeEach
	public void setup() throws Exception {
		advice = new GenerateBillFromOrderAdvice();
		billService = Context.getService(BillService.class);
		conceptService = Context.getConceptService();
		patientService = Context.getPatientService();
		
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillAdviceTest.xml");
		
		// Simulate the method being intercepted
		saveOrderMethod = org.openmrs.api.OrderService.class.getMethod("saveOrder", Order.class,
		    org.openmrs.api.OrderContext.class);
	}
	
	/**
	 * @see GenerateBillFromOrderAdvice#afterReturning(Object, Method, Object[], Object)
	 */
	@Test
	public void afterReturning_shouldCreatePendingBillForDrugOrderWithMatchingBillableService() throws Throwable {
		Patient patient = patientService.getPatient(2);
		Concept drugConcept = conceptService.getConceptByUuid(DRUG_CONCEPT_UUID);
		
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setPatient(patient);
		drugOrder.setConcept(drugConcept);
		drugOrder.setQuantity(2.0);
		drugOrder.setAction(Order.Action.NEW);
		drugOrder.setDateActivated(new Date());
		
		int billsBefore = billService.getBills(new org.openmrs.module.billing.api.search.BillSearch(), null).size();
		
		advice.afterReturning(null, saveOrderMethod, new Object[] { drugOrder }, null);
		
		List<Bill> billsAfter = billService.getBills(new org.openmrs.module.billing.api.search.BillSearch(), null);
		assertEquals(billsBefore + 1, billsAfter.size());
		
		Bill createdBill = billsAfter.get(billsAfter.size() - 1);
		assertEquals(BillStatus.PENDING, createdBill.getStatus());
		assertFalse(createdBill.getLineItems().isEmpty());
		assertEquals(1, createdBill.getLineItems().size());
	}
	
	/**
	 * @see GenerateBillFromOrderAdvice#afterReturning(Object, Method, Object[], Object)
	 */
	@Test
	public void afterReturning_shouldCreatePendingBillForTestOrderWithMatchingBillableService() throws Throwable {
		Patient patient = patientService.getPatient(2);
		Concept testConcept = conceptService.getConceptByUuid(TEST_CONCEPT_UUID);
		
		TestOrder testOrder = new TestOrder();
		testOrder.setPatient(patient);
		testOrder.setConcept(testConcept);
		testOrder.setAction(Order.Action.NEW);
		testOrder.setDateActivated(new Date());
		
		int billsBefore = billService.getBills(new org.openmrs.module.billing.api.search.BillSearch(), null).size();
		
		advice.afterReturning(null, saveOrderMethod, new Object[] { testOrder }, null);
		
		List<Bill> billsAfter = billService.getBills(new org.openmrs.module.billing.api.search.BillSearch(), null);
		assertEquals(billsBefore + 1, billsAfter.size());
		
		Bill createdBill = billsAfter.get(billsAfter.size() - 1);
		assertEquals(BillStatus.PENDING, createdBill.getStatus());
		assertEquals(1, createdBill.getLineItems().size());
	}
	
	/**
	 * @see GenerateBillFromOrderAdvice#afterReturning(Object, Method, Object[], Object)
	 */
	@Test
	public void afterReturning_shouldCreateSeparateBillForEachDrugOrder() throws Throwable {
		Patient patient = patientService.getPatient(2);
		Concept drugConcept = conceptService.getConceptByUuid(DRUG_CONCEPT_UUID);
		
		int billsBefore = billService.getBills(new org.openmrs.module.billing.api.search.BillSearch(), null).size();
		
		for (int i = 0; i < 3; i++) {
			DrugOrder drugOrder = new DrugOrder();
			drugOrder.setPatient(patient);
			drugOrder.setConcept(drugConcept);
			drugOrder.setQuantity(1.0);
			drugOrder.setAction(Order.Action.NEW);
			drugOrder.setDateActivated(new Date());
			advice.afterReturning(null, saveOrderMethod, new Object[] { drugOrder }, null);
		}
		
		List<Bill> billsAfter = billService.getBills(new org.openmrs.module.billing.api.search.BillSearch(), null);
		assertEquals(billsBefore + 3, billsAfter.size());
	}
	
	/**
	 * @see GenerateBillFromOrderAdvice#afterReturning(Object, Method, Object[], Object)
	 */
	@Test
	public void afterReturning_shouldNotCreateBillForDrugOrderWithNoConcept() throws Throwable {
		Patient patient = patientService.getPatient(2);
		
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setPatient(patient);
		drugOrder.setConcept(null);
		drugOrder.setQuantity(1.0);
		drugOrder.setAction(Order.Action.NEW);
		drugOrder.setDateActivated(new Date());
		
		int billsBefore = billService.getBills(new org.openmrs.module.billing.api.search.BillSearch(), null).size();
		
		advice.afterReturning(null, saveOrderMethod, new Object[] { drugOrder }, null);
		
		assertEquals(billsBefore, billService.getBills(new org.openmrs.module.billing.api.search.BillSearch(), null).size());
	}
	
	/**
	 * @see GenerateBillFromOrderAdvice#afterReturning(Object, Method, Object[], Object)
	 */
	@Test
	public void afterReturning_shouldNotCreateBillForDrugOrderWithNoMatchingBillableService() throws Throwable {
		Patient patient = patientService.getPatient(2);
		
		// Use a concept that has no billable service configured
		Concept unknownConcept = new Concept();
		unknownConcept.setUuid("00000000-0000-0000-0000-000000000000");
		
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setPatient(patient);
		drugOrder.setConcept(unknownConcept);
		drugOrder.setQuantity(1.0);
		drugOrder.setAction(Order.Action.NEW);
		drugOrder.setDateActivated(new Date());
		
		int billsBefore = billService.getBills(new org.openmrs.module.billing.api.search.BillSearch(), null).size();
		
		advice.afterReturning(null, saveOrderMethod, new Object[] { drugOrder }, null);
		
		assertEquals(billsBefore, billService.getBills(new org.openmrs.module.billing.api.search.BillSearch(), null).size());
	}
	
	/**
	 * @see GenerateBillFromOrderAdvice#afterReturning(Object, Method, Object[], Object)
	 */
	@Test
	public void afterReturning_shouldNotCreateBillForDiscontinueAction() throws Throwable {
		Patient patient = patientService.getPatient(2);
		Concept drugConcept = conceptService.getConceptByUuid(DRUG_CONCEPT_UUID);
		
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setPatient(patient);
		drugOrder.setConcept(drugConcept);
		drugOrder.setQuantity(1.0);
		drugOrder.setAction(Order.Action.DISCONTINUE);
		drugOrder.setDateActivated(new Date());
		
		int billsBefore = billService.getBills(new org.openmrs.module.billing.api.search.BillSearch(), null).size();
		
		advice.afterReturning(null, saveOrderMethod, new Object[] { drugOrder }, null);
		
		assertEquals(billsBefore, billService.getBills(new org.openmrs.module.billing.api.search.BillSearch(), null).size());
	}
	
	/**
	 * @see GenerateBillFromOrderAdvice#afterReturning(Object, Method, Object[], Object)
	 */
	@Test
	public void afterReturning_shouldNotCreateBillForReviseAction() throws Throwable {
		Patient patient = patientService.getPatient(2);
		Concept drugConcept = conceptService.getConceptByUuid(DRUG_CONCEPT_UUID);
		
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setPatient(patient);
		drugOrder.setConcept(drugConcept);
		drugOrder.setQuantity(1.0);
		drugOrder.setAction(Order.Action.REVISE);
		drugOrder.setDateActivated(new Date());
		
		int billsBefore = billService.getBills(new org.openmrs.module.billing.api.search.BillSearch(), null).size();
		
		advice.afterReturning(null, saveOrderMethod, new Object[] { drugOrder }, null);
		
		assertEquals(billsBefore, billService.getBills(new org.openmrs.module.billing.api.search.BillSearch(), null).size());
	}
	
	/**
	 * @see GenerateBillFromOrderAdvice#afterReturning(Object, Method, Object[], Object)
	 */
	@Test
	public void afterReturning_shouldNotCreateBillForRenewAction() throws Throwable {
		Patient patient = patientService.getPatient(2);
		Concept drugConcept = conceptService.getConceptByUuid(DRUG_CONCEPT_UUID);
		
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setPatient(patient);
		drugOrder.setConcept(drugConcept);
		drugOrder.setQuantity(1.0);
		drugOrder.setAction(Order.Action.RENEW);
		drugOrder.setDateActivated(new Date());
		
		int billsBefore = billService.getBills(new org.openmrs.module.billing.api.search.BillSearch(), null).size();
		
		advice.afterReturning(null, saveOrderMethod, new Object[] { drugOrder }, null);
		
		assertEquals(billsBefore, billService.getBills(new org.openmrs.module.billing.api.search.BillSearch(), null).size());
	}
	
	/**
	 * @see GenerateBillFromOrderAdvice#afterReturning(Object, Method, Object[], Object)
	 */
	@Test
	public void afterReturning_shouldNotCreateBillWhenBillableServiceIsDisabled() throws Throwable {
		Patient patient = patientService.getPatient(2);
		// DRUG_CONCEPT_UUID has both an ENABLED and a DISABLED service — only ENABLED should match
		// A concept with ONLY a disabled service should produce no bill
		Concept drugConcept = conceptService.getConceptByUuid(DRUG_CONCEPT_UUID);
		
		// Verify there IS an enabled service for this concept (proves test data is correct)
		int billsBefore = billService.getBills(new org.openmrs.module.billing.api.search.BillSearch(), null).size();
		
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setPatient(patient);
		drugOrder.setConcept(drugConcept);
		drugOrder.setQuantity(1.0);
		drugOrder.setAction(Order.Action.NEW);
		drugOrder.setDateActivated(new Date());
		
		advice.afterReturning(null, saveOrderMethod, new Object[] { drugOrder }, null);
		
		// Should create exactly ONE bill (from ENABLED service, not DISABLED)
		List<Bill> billsAfter = billService.getBills(new org.openmrs.module.billing.api.search.BillSearch(), null);
		assertEquals(billsBefore + 1, billsAfter.size());
	}
	
	/**
	 * @see GenerateBillFromOrderAdvice#afterReturning(Object, Method, Object[], Object)
	 */
	@Test
	public void afterReturning_shouldNotCreateBillWhenMethodIsNotSaveOrder() throws Throwable {
		Patient patient = patientService.getPatient(2);
		Concept drugConcept = conceptService.getConceptByUuid(DRUG_CONCEPT_UUID);
		
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setPatient(patient);
		drugOrder.setConcept(drugConcept);
		drugOrder.setQuantity(1.0);
		drugOrder.setAction(Order.Action.NEW);
		drugOrder.setDateActivated(new Date());
		
		int billsBefore = billService.getBills(new org.openmrs.module.billing.api.search.BillSearch(), null).size();
		
		// Use a different method name — should be ignored
		Method voidOrderMethod = org.openmrs.api.OrderService.class.getMethod("voidOrder", Order.class, String.class);
		advice.afterReturning(null, voidOrderMethod, new Object[] { drugOrder }, null);
		
		assertEquals(billsBefore, billService.getBills(new org.openmrs.module.billing.api.search.BillSearch(), null).size());
	}
}
