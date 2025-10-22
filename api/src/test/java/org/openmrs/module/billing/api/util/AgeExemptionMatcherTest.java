package org.openmrs.module.billing.api.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openmrs.module.billing.api.util.AgeExemptionMatcher.matchesAgeCondition;

/**
 * Test for {@link AgeExemptionMatcher}
 */
public class AgeExemptionMatcherTest {
	
	@Test
	public void matchesAgeCondition_shouldReturnTrueForAgeLessThanThreshold() {
		assertTrue(matchesAgeCondition(4, "age<5"));
		assertTrue(matchesAgeCondition(0, "age<5"));
		assertTrue(matchesAgeCondition(64, "age<65"));
	}
	
	@Test
	public void matchesAgeCondition_shouldReturnFalseForAgeEqualOrGreaterThanThreshold() {
		assertFalse(matchesAgeCondition(5, "age<5"));
		assertFalse(matchesAgeCondition(6, "age<5"));
		assertFalse(matchesAgeCondition(65, "age<65"));
	}
	
	@Test
	public void matchesAgeCondition_shouldReturnTrueForAgeGreaterThanThreshold() {
		assertTrue(matchesAgeCondition(66, "age>65"));
		assertTrue(matchesAgeCondition(100, "age>65"));
		assertTrue(matchesAgeCondition(6, "age>5"));
	}
	
	@Test
	public void matchesAgeCondition_shouldReturnFalseForAgeEqualOrLessThanThreshold() {
		assertFalse(matchesAgeCondition(65, "age>65"));
		assertFalse(matchesAgeCondition(64, "age>65"));
		assertFalse(matchesAgeCondition(5, "age>5"));
	}
	
	@Test
	public void matchesAgeCondition_shouldReturnTrueForAgeLessThanOrEqualToThreshold() {
		assertTrue(matchesAgeCondition(5, "age<=5"));
		assertTrue(matchesAgeCondition(4, "age<=5"));
		assertTrue(matchesAgeCondition(0, "age<=5"));
	}
	
	@Test
	public void matchesAgeCondition_shouldReturnFalseForAgeGreaterThanThresholdWithLessOrEqual() {
		assertFalse(matchesAgeCondition(6, "age<=5"));
		assertFalse(matchesAgeCondition(66, "age<=65"));
	}
	
	@Test
	public void matchesAgeCondition_shouldReturnTrueForAgeGreaterThanOrEqualToThreshold() {
		assertTrue(matchesAgeCondition(65, "age>=65"));
		assertTrue(matchesAgeCondition(66, "age>=65"));
		assertTrue(matchesAgeCondition(100, "age>=65"));
	}
	
	@Test
	public void matchesAgeCondition_shouldReturnFalseForAgeLessThanThresholdWithGreaterOrEqual() {
		assertFalse(matchesAgeCondition(64, "age>=65"));
		assertFalse(matchesAgeCondition(4, "age>=5"));
	}
	
	@Test
	public void matchesAgeCondition_shouldReturnTrueForAgeEqualToThresholdWithDoubleEquals() {
		assertTrue(matchesAgeCondition(5, "age==5"));
		assertTrue(matchesAgeCondition(65, "age==65"));
		assertTrue(matchesAgeCondition(0, "age==0"));
	}
	
	@Test
	public void matchesAgeCondition_shouldReturnTrueForAgeEqualToThresholdWithSingleEquals() {
		assertTrue(matchesAgeCondition(5, "age=5"));
		assertTrue(matchesAgeCondition(65, "age=65"));
	}
	
	@Test
	public void matchesAgeCondition_shouldReturnFalseForAgeNotEqualToThreshold() {
		assertFalse(matchesAgeCondition(4, "age==5"));
		assertFalse(matchesAgeCondition(6, "age==5"));
		assertFalse(matchesAgeCondition(64, "age=65"));
	}
	
	@Test
	public void matchesAgeCondition_shouldReturnTrueForAgeWithinRange() {
		assertTrue(matchesAgeCondition(18, "age:18-65"));
		assertTrue(matchesAgeCondition(65, "age:18-65"));
		assertTrue(matchesAgeCondition(40, "age:18-65"));
		assertTrue(matchesAgeCondition(5, "age:0-10"));
	}
	
	@Test
	public void matchesAgeCondition_shouldReturnFalseForAgeOutsideRange() {
		assertFalse(matchesAgeCondition(17, "age:18-65"));
		assertFalse(matchesAgeCondition(66, "age:18-65"));
		assertFalse(matchesAgeCondition(0, "age:18-65"));
		assertFalse(matchesAgeCondition(100, "age:18-65"));
	}
	
	@Test
	public void matchesAgeCondition_shouldReturnFalseForNullPatientAge() {
		assertFalse(matchesAgeCondition(null, "age<5"));
		assertFalse(matchesAgeCondition(null, "age>65"));
		assertFalse(matchesAgeCondition(null, "age:18-65"));
	}
	
	@Test
	public void matchesAgeCondition_shouldReturnFalseForNullExemptionKey() {
		assertFalse(matchesAgeCondition(25, null));
	}
	
	@Test
	public void matchesAgeCondition_shouldReturnFalseForEmptyExemptionKey() {
		assertFalse(matchesAgeCondition(25, ""));
	}
	
	@Test
	public void matchesAgeCondition_shouldReturnFalseForExemptionKeyNotStartingWithAge() {
		assertFalse(matchesAgeCondition(25, "location:clinic"));
		assertFalse(matchesAgeCondition(25, "program:HIV"));
	}
	
	@Test
	public void matchesAgeCondition_shouldReturnFalseForInvalidNumberFormat() {
		// These should not throw exceptions, just return false and log warning
		assertFalse(matchesAgeCondition(25, "age<abc"));
		assertFalse(matchesAgeCondition(25, "age>xyz"));
		assertFalse(matchesAgeCondition(25, "age:abc-xyz"));
	}
	
	@Test
	public void matchesAgeCondition_shouldHandleWhitespaceInExemptionKey() {
		assertTrue(matchesAgeCondition(4, "age< 5"));
		assertTrue(matchesAgeCondition(66, "age> 65"));
		assertTrue(matchesAgeCondition(40, "age: 18 - 65"));
	}
	
	@Test
	public void matchesAgeCondition_shouldReturnFalseForMalformedRange() {
		assertFalse(matchesAgeCondition(25, "age:18"));
		assertFalse(matchesAgeCondition(25, "age:18-"));
		assertFalse(matchesAgeCondition(25, "age:-65"));
	}
	
	@Test
	public void matchesAgeCondition_shouldHandleZeroAge() {
		assertTrue(matchesAgeCondition(0, "age<1"));
		assertTrue(matchesAgeCondition(0, "age<=0"));
		assertTrue(matchesAgeCondition(0, "age==0"));
		assertFalse(matchesAgeCondition(0, "age>0"));
	}
	
	@Test
	public void matchesAgeCondition_shouldHandleVeryHighAge() {
		assertTrue(matchesAgeCondition(150, "age>100"));
		assertTrue(matchesAgeCondition(150, "age>=100"));
		assertFalse(matchesAgeCondition(150, "age<100"));
	}
}
