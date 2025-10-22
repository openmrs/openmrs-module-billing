package org.openmrs.module.billing.api.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AgeExemptionMatcher {
	
	/**
	 * Checks if a patient's age matches an age-based exemption condition. Supported formats: - "age<N"
	 * - Age less than N (e.g., "age<5" for children under 5) - "age>N" - Age greater than N (e.g.,
	 * "age>65" for seniors over 65) - "age<=N" - Age less than or equal to N - "age>=N" - Age greater
	 * than or equal to N - "age==N" or "age=N" - Age equals N - "age:MIN-MAX" - Age range from MIN to
	 * MAX (inclusive)
	 *
	 * @param patientAge The patient's age
	 * @param exemptionKey The exemption key (e.g., "age<5", "age:18-65")
	 * @return true if the patient's age matches the condition, false otherwise
	 */
	public static boolean matchesAgeCondition(Integer patientAge, String exemptionKey) {
		if (patientAge == null || exemptionKey == null || !exemptionKey.startsWith("age")) {
			return false;
		}
		
		try {
			if (exemptionKey.contains(":") && exemptionKey.contains("-")) {
				String rangePart = exemptionKey.substring(exemptionKey.indexOf(":") + 1);
				String[] parts = rangePart.split("-");
				if (parts.length == 2) {
					int minAge = Integer.parseInt(parts[0].trim());
					int maxAge = Integer.parseInt(parts[1].trim());
					return patientAge >= minAge && patientAge <= maxAge;
				}
			}
			
			if (exemptionKey.startsWith("age<=")) {
				int threshold = Integer.parseInt(exemptionKey.substring(5).trim());
				return patientAge <= threshold;
			} else if (exemptionKey.startsWith("age>=")) {
				int threshold = Integer.parseInt(exemptionKey.substring(5).trim());
				return patientAge >= threshold;
			} else if (exemptionKey.startsWith("age<")) {
				int threshold = Integer.parseInt(exemptionKey.substring(4).trim());
				return patientAge < threshold;
			} else if (exemptionKey.startsWith("age>")) {
				int threshold = Integer.parseInt(exemptionKey.substring(4).trim());
				return patientAge > threshold;
			} else if (exemptionKey.startsWith("age==") || exemptionKey.matches("age=\\d+")) {
				String numberPart = exemptionKey.startsWith("age==") ? exemptionKey.substring(5) : exemptionKey.substring(4);
				int threshold = Integer.parseInt(numberPart.trim());
				return patientAge.equals(threshold);
			}
		}
		catch (NumberFormatException e) {
			log.warn("Invalid age exemption key format: {}. Unable to parse age threshold.", exemptionKey, e);
		}
		catch (Exception e) {
			log.error("Error evaluating age condition for exemption key: {}", exemptionKey, e);
		}
		return false;
	}
}
