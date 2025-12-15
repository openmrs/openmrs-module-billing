package org.openmrs.module.billing.exemptions;

import java.util.Map;
import java.util.Set;

public class BillingExemptionsConfig {
	
	private Map<String, Set<Integer>> services;
	
	private Map<String, Set<Integer>> commodities;
	
	// Getters and setters
	public Map<String, Set<Integer>> getServices() {
		return services;
	}
	
	public void setServices(Map<String, Set<Integer>> services) {
		this.services = services;
	}
	
	public Map<String, Set<Integer>> getCommodities() {
		return commodities;
	}
	
	public void setCommodities(Map<String, Set<Integer>> commodities) {
		this.commodities = commodities;
	}
}
