/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.evaluator.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.module.billing.api.evaluator.ScriptType;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JSExemptionEvaluatorTest {
	
	private JSExemptionEvaluator evaluator;
	
	@BeforeEach
	public void setup() {
		evaluator = new JSExemptionEvaluator();
	}
	
	/**
	 * @see JSExemptionEvaluator#getSupportedType()
	 */
	@Test
	public void getSupportedType_shouldReturnJavaScript() {
		assertEquals(ScriptType.JAVASCRIPT, evaluator.getSupportedType());
	}
	
	/**
	 * @see JSExemptionEvaluator#evaluate(String, Map)
	 */
	@Test
	public void evaluate_shouldReturnTrueForTrueScript() {
		boolean result = evaluator.evaluate("true", null);
		assertTrue(result);
	}
	
	/**
	 * @see JSExemptionEvaluator#evaluate(String, Map)
	 */
	@Test
	public void evaluate_shouldReturnFalseForFalseScript() {
		boolean result = evaluator.evaluate("false", null);
		assertFalse(result);
	}
	
	/**
	 * @see JSExemptionEvaluator#evaluate(String, Map)
	 */
	@Test
	public void evaluate_shouldEvaluateSimpleComparison() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("age", 10);
		
		boolean result = evaluator.evaluate("age < 18", variables);
		assertTrue(result);
	}
	
	/**
	 * @see JSExemptionEvaluator#evaluate(String, Map)
	 */
	@Test
	public void evaluate_shouldReturnFalseWhenComparisonFails() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("age", 25);
		
		boolean result = evaluator.evaluate("age < 18", variables);
		assertFalse(result);
	}
	
	/**
	 * @see JSExemptionEvaluator#evaluate(String, Map)
	 */
	@Test
	public void evaluate_shouldHandleComplexExpressions() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("age", 5);
		variables.put("hasInsurance", false);
		
		boolean result = evaluator.evaluate("age < 18 && !hasInsurance", variables);
		assertTrue(result);
	}
	
	/**
	 * @see JSExemptionEvaluator#evaluate(String, Map)
	 */
	@Test
	public void evaluate_shouldReturnFalseForNullResult() {
		boolean result = evaluator.evaluate("null", null);
		assertFalse(result);
	}
	
	/**
	 * @see JSExemptionEvaluator#evaluate(String, Map)
	 */
	@Test
	public void evaluate_shouldThrowExceptionForInvalidScript() {
		assertThrows(RuntimeException.class, () -> {
			evaluator.evaluate("invalid javascript +++", null);
		});
	}
}
