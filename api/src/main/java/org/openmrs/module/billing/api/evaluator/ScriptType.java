package org.openmrs.module.billing.api.evaluator;

public enum ScriptType {
	
	JAVASCRIPT("js");
	
	private final String engineName;
	
	ScriptType(String engineName) {
		this.engineName = engineName;
	}
	
	public String getEngineName() {
		return engineName;
	}
}
