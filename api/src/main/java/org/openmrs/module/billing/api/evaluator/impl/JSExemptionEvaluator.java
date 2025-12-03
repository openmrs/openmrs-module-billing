package org.openmrs.module.billing.api.evaluator.impl;

import org.apache.commons.lang.StringEscapeUtils;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.openmrs.module.billing.api.evaluator.ExemptionEvaluator;
import org.openmrs.module.billing.api.evaluator.ScriptType;

import java.util.Collections;
import java.util.Map;

public class JSExemptionEvaluator implements ExemptionEvaluator {
	
	@Override
	public ScriptType getSupportedType() {
		return ScriptType.JAVASCRIPT;
	}
	
	@Override
	public boolean evaluate(String script, Map<String, Object> variables) {
		try (Context context = Context.newBuilder("js").allowAllAccess(false).allowHostClassLookup(className -> false)
		        .build()) {
			Value bindings = context.getBindings("js");
			
			Map<String, Object> safeVars = (variables != null ? variables : Collections.emptyMap());
			
			Value varsObject = convertMapToJSObject(context, safeVars);
			bindings.putMember("vars", varsObject);
			
			for (Map.Entry<String, Object> entry : safeVars.entrySet()) {
				Object value = entry.getValue();
				if (value instanceof Map) {
					value = convertMapToJSObject(context, (Map<?, ?>) value);
				}
				bindings.putMember(entry.getKey(), value);
			}
			
			Value result = context.eval("js", script);
			
			if (result.isBoolean()) {
				return result.asBoolean();
			}
			if (result.isNull()) {
				return false;
			}
			return Boolean.parseBoolean(result.toString());
		}
		catch (Exception e) {
			throw new RuntimeException("Error evaluating JS exemption script: " + script, e);
		}
	}
	
	private Value convertMapToJSObject(Context context, Map<?, ?> map) {
		Value jsObject = context.eval("js", "({})");
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			String key = entry.getKey().toString();
			Object value = entry.getValue();
			
			if (value instanceof Map) {
				value = convertMapToJSObject(context, (Map<?, ?>) value);
			}
			
			jsObject.putMember(key, value);
		}
		return jsObject;
	}
}
