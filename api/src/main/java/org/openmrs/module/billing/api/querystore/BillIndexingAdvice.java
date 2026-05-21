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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.querystore.bridge.AbstractIndexingAdvice;

public class BillIndexingAdvice extends AbstractIndexingAdvice<Bill> {
	
	static final Set<String> TRIGGER_METHODS = new HashSet<>(
	        Arrays.asList("saveBill", "voidBill", "unvoidBill", "purgeBill"));
	
	static final Set<String> PURGE_METHODS = Collections.singleton("purgeBill");
	
	@Override
	protected Class<Bill> getSupportedType() {
		return Bill.class;
	}
	
	@Override
	protected BillRecordSerializer serializer() {
		return Context.getRegisteredComponent("billing.querystore.serializer.bill", BillRecordSerializer.class);
	}
	
	@Override
	protected Set<String> triggerMethods() {
		return TRIGGER_METHODS;
	}
	
	@Override
	protected Set<String> purgeMethods() {
		return PURGE_METHODS;
	}
}
