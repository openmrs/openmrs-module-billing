/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.querystore;

import java.lang.reflect.Method;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.openmrs.module.billing.api.BillRefundService;
import org.openmrs.module.billing.api.BillService;

import static org.junit.jupiter.api.Assertions.assertTrue;

// AbstractIndexingAdvice matches advised invocations by method NAME against the configured
// trigger/purge sets. A typo in one of those names produces no compile error, no startup error,
// and no runtime exception — the advice simply never fires on that name, the document is never
// indexed or deleted, and the failure surfaces only as "stale rows in the read store" weeks
// later. These tests catch the typo class at unit-test time by reflecting against the target
// service interface declared in omod/config.xml's <advice><point>.
public class IndexingAdviceConfigTest {
	
	@Test
	public void billIndexingAdvice_triggerMethodsShouldAllExistOnBillService() {
		assertAllMethodsExist(BillIndexingAdvice.TRIGGER_METHODS, BillService.class, "BillIndexingAdvice");
	}
	
	@Test
	public void billIndexingAdvice_purgeMethodsShouldBeSubsetOfTriggerMethods() {
		assertSubset(BillIndexingAdvice.PURGE_METHODS, BillIndexingAdvice.TRIGGER_METHODS, "BillIndexingAdvice");
	}
	
	@Test
	public void billRefundIndexingAdvice_triggerMethodsShouldAllExistOnBillRefundService() {
		assertAllMethodsExist(BillRefundIndexingAdvice.TRIGGER_METHODS, BillRefundService.class, "BillRefundIndexingAdvice");
	}
	
	@Test
	public void billRefundIndexingAdvice_purgeMethodsShouldBeSubsetOfTriggerMethods() {
		assertSubset(BillRefundIndexingAdvice.PURGE_METHODS, BillRefundIndexingAdvice.TRIGGER_METHODS,
		    "BillRefundIndexingAdvice");
	}
	
	private static void assertAllMethodsExist(Set<String> methodNames, Class<?> iface, String adviceName) {
		for (String name : methodNames) {
			assertTrue(hasMethod(iface, name), adviceName + " references method '" + name + "' but no such method exists on "
			        + iface.getSimpleName() + " — AOP would silently never fire on this name");
		}
	}
	
	private static void assertSubset(Set<String> subset, Set<String> superset, String adviceName) {
		assertTrue(superset.containsAll(subset),
		    adviceName + ": PURGE_METHODS must be a subset of TRIGGER_METHODS per the AbstractIndexingAdvice "
		            + "contract — a name in purge but not in trigger never fires the advice, so the purge path "
		            + "is unreachable");
	}
	
	private static boolean hasMethod(Class<?> iface, String name) {
		for (Method m : iface.getMethods()) {
			if (m.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
}
