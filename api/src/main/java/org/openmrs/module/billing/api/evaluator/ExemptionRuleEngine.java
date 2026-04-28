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

import org.openmrs.module.billing.api.model.BillExemption;
import org.openmrs.module.billing.api.model.BillExemptionRule;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ExemptionRuleEngine {
	
	private final Map<ScriptType, ExemptionEvaluator> evaluatorsByType = new EnumMap<>(ScriptType.class);
	
	public ExemptionRuleEngine(List<ExemptionEvaluator> evaluators) {
		for (ExemptionEvaluator evaluator : evaluators) {
			evaluatorsByType.put(evaluator.getSupportedType(), evaluator);
		}
	}
	
	public boolean evaluateRule(BillExemptionRule rule, Map<String, Object> variables) {
		ExemptionEvaluator evaluator = evaluatorsByType.get(rule.getScriptType());
		if (evaluator == null) {
			throw new IllegalArgumentException("Unsupported script type: " + rule.getScriptType());
		}
		return evaluator.evaluate(rule.getScript(), variables);
	}
	
	public boolean isExemptionApplicable(BillExemption exemption, Map<String, Object> variables) {
		if (exemption.getRules() == null || exemption.getRules().isEmpty()) {
			return false;
		}
		
		return exemption.getRules().stream().filter(r -> !r.getVoided()).anyMatch(r -> evaluateRule(r, variables));
	}
}
