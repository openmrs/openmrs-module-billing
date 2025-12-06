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
