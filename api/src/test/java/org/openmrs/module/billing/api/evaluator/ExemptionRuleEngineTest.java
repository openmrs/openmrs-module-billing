/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.evaluator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.BillExemptionService;
import org.openmrs.module.billing.api.evaluator.impl.JSExemptionEvaluator;
import org.openmrs.module.billing.api.model.BillExemption;
import org.openmrs.module.billing.api.model.BillExemptionRule;
import org.openmrs.module.billing.api.model.ExemptionType;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExemptionRuleEngineTest extends BaseModuleContextSensitiveTest {
	
	private ExemptionRuleEngine ruleEngine;
	
	private BillExemptionService billExemptionService;
	
	private ConceptService conceptService;
	
	private PatientService patientService;
	
	@BeforeEach
	public void setup() {
		List<ExemptionEvaluator> evaluators = new ArrayList<>();
		evaluators.add(new JSExemptionEvaluator());
		ruleEngine = new ExemptionRuleEngine(evaluators);
		
		billExemptionService = Context.getService(BillExemptionService.class);
		conceptService = Context.getConceptService();
		patientService = Context.getPatientService();
		
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillExemptionTest.xml");
	}
	
	/**
	 * @see ExemptionRuleEngine#evaluateRule(BillExemptionRule, Map)
	 */
	@Test
	public void evaluateRule_shouldEvaluateSimpleRule() {
		BillExemptionRule rule = new BillExemptionRule();
		rule.setScriptType(ScriptType.JAVASCRIPT);
		rule.setScript("age < 18");
		
		Map<String, Object> variables = new HashMap<>();
		variables.put("age", 10);
		
		boolean result = ruleEngine.evaluateRule(rule, variables);
		
		assertTrue(result);
	}
	
	/**
	 * @see ExemptionRuleEngine#evaluateRule(BillExemptionRule, Map)
	 */
	@Test
	public void evaluateRule_shouldReturnFalseWhenRuleFails() {
		BillExemptionRule rule = new BillExemptionRule();
		rule.setScriptType(ScriptType.JAVASCRIPT);
		rule.setScript("age < 18");
		
		Map<String, Object> variables = new HashMap<>();
		variables.put("age", 25);
		
		boolean result = ruleEngine.evaluateRule(rule, variables);
		
		assertFalse(result);
	}
	
	/**
	 * @see ExemptionRuleEngine#isExemptionApplicable(BillExemption, Map)
	 */
	@Test
	public void isExemptionApplicable_shouldReturnTrueWhenAnyRuleMatches() {
		BillExemption exemption = billExemptionService.getBillingExemptionById(1);
		assertNotNull(exemption);
		assertNotNull(exemption.getRules());
		assertFalse(exemption.getRules().isEmpty());
		
		Map<String, Object> variables = new HashMap<>();
		variables.put("patientAge", 3);
		
		boolean result = ruleEngine.isExemptionApplicable(exemption, variables);
		
		assertTrue(result);
	}
	
	/**
	 * @see ExemptionRuleEngine#isExemptionApplicable(BillExemption, Map)
	 */
	@Test
	public void isExemptionApplicable_shouldReturnFalseWhenNoRuleMatches() {
		BillExemption exemption = billExemptionService.getBillingExemptionById(1);
		assertNotNull(exemption);
		
		Map<String, Object> variables = new HashMap<>();
		variables.put("patientAge", 25);
		
		boolean result = ruleEngine.isExemptionApplicable(exemption, variables);
		
		assertFalse(result);
	}
	
	/**
	 * Integration test mimicking actual order exemption check
	 */
	@Test
	public void checkIfOrderIsExempted_shouldExemptChildrenUnderFive() {
		Patient patient = patientService.getPatient(2);
		assertNotNull(patient);
		
		Concept concept = conceptService.getConcept(100);
		assertNotNull(concept);
		
		Order order = new Order();
		order.setPatient(patient);
		order.setConcept(concept);
		
		List<BillExemption> exemptions = billExemptionService.getExemptionsByConcept(concept, ExemptionType.SERVICE, false);
		
		assertNotNull(exemptions);
		assertFalse(exemptions.isEmpty());
		
		Map<String, Object> variables = buildVariablesMapForOrder(order);
		
		boolean isExempted = false;
		for (BillExemption exemption : exemptions) {
			if (ruleEngine.isExemptionApplicable(exemption, variables)) {
				isExempted = true;
				break;
			}
		}
		
		assertTrue(isExempted);
	}
	
	/**
	 * Integration test with active programs
	 */
	@Test
	public void checkIfOrderIsExempted_shouldCheckActivePrograms() {
		Patient patient = patientService.getPatient(2);
		assertNotNull(patient);
		
		Concept concept = conceptService.getConcept(100);
		assertNotNull(concept);
		
		Order order = new Order();
		order.setPatient(patient);
		order.setConcept(concept);
		
		Set<String> activePrograms = new HashSet<>();
		activePrograms.add("HIV Program");
		activePrograms.add("TB Program");
		
		Map<String, Object> variables = new HashMap<>();
		variables.put("order", order);
		variables.put("patient", patient);
		variables.put("patientAge", 4);
		variables.put("activePrograms", activePrograms);
		
		List<BillExemption> exemptions = billExemptionService.getExemptionsByConcept(concept, ExemptionType.SERVICE, false);
		
		boolean isExempted = false;
		for (BillExemption exemption : exemptions) {
			if (ruleEngine.isExemptionApplicable(exemption, variables)) {
				isExempted = true;
				break;
			}
		}
		
		assertTrue(isExempted);
	}
	
	/**
	 * Test with elderly patient (>= 65 years)
	 */
	@Test
	public void checkIfOrderIsExempted_shouldExemptElderlyPatients() {
		Patient patient = patientService.getPatient(2);
		assertNotNull(patient);
		
		Concept commodityConcept = conceptService.getConcept(102);
		assertNotNull(commodityConcept);
		
		Order order = new Order();
		order.setPatient(patient);
		order.setConcept(commodityConcept);
		
		List<BillExemption> exemptions = billExemptionService.getExemptionsByConcept(commodityConcept,
		    ExemptionType.COMMODITY, false);
		
		assertNotNull(exemptions);
		assertFalse(exemptions.isEmpty());
		
		Map<String, Object> variables = new HashMap<>();
		variables.put("order", order);
		variables.put("patientAge", 70);
		
		boolean isExempted = false;
		for (BillExemption exemption : exemptions) {
			if (ruleEngine.isExemptionApplicable(exemption, variables)) {
				isExempted = true;
				break;
			}
		}
		
		assertTrue(isExempted);
	}
	
	/**
	 * Test that non-exempted orders return false
	 */
	@Test
	public void checkIfOrderIsExempted_shouldNotExemptNonQualifyingOrders() {
		Patient patient = patientService.getPatient(2);
		assertNotNull(patient);
		
		Concept concept = conceptService.getConcept(100);
		assertNotNull(concept);
		
		Order order = new Order();
		order.setPatient(patient);
		order.setConcept(concept);
		
		Map<String, Object> variables = new HashMap<>();
		variables.put("order", order);
		variables.put("patientAge", 30);
		
		List<BillExemption> exemptions = billExemptionService.getExemptionsByConcept(concept, ExemptionType.SERVICE, false);
		
		boolean isExempted = false;
		for (BillExemption exemption : exemptions) {
			if (ruleEngine.isExemptionApplicable(exemption, variables)) {
				isExempted = true;
				break;
			}
		}
		
		assertFalse(isExempted);
	}
	
	private Map<String, Object> buildVariablesMapForOrder(Order order) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("order", order);
		variables.put("patientAge", 4);
		
		Set<String> activePrograms = new HashSet<>();
		variables.put("activePrograms", activePrograms);
		
		return variables;
	}
}
