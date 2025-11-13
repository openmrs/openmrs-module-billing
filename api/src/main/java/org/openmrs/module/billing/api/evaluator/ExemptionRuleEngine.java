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
