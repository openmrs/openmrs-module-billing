package org.openmrs.module.billing.api.evaluator;

import java.util.Map;

public interface ExemptionEvaluator {
	
	ScriptType getSupportedType();
	
	boolean evaluate(String script, Map<String, Object> variables);
	
}
