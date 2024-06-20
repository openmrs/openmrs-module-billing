package org.openmrs.module.billing.exemptions;

import java.util.Set;

public class BillingExemptionChecker {
	
	/**
	 * Checks if a given concept ID is exempted from billing under the provided category.
	 *
	 * @param category The category to check (e.g., "services" or "commodities")
	 * @param key The specific key within the category (e.g., "program:HIV")
	 * @param conceptId The concept ID to check for exemption
	 * @return true if the concept ID is exempted, false otherwise
	 */
	public boolean isExempted(String category, String key, Integer conceptId) {
		Set<Integer> exemptedConcepts;
		if (category.equals("services")) {
			exemptedConcepts = BillingExemptions.SERVICES.get(key);
		} else if (category.equals("commodities")) {
			exemptedConcepts = BillingExemptions.COMMODITIES.get(key);
		} else {
			return false;
		}
		return exemptedConcepts != null && exemptedConcepts.contains(conceptId);
	}
}
