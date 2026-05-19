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
import org.openmrs.module.billing.api.model.BillRefund;
import org.openmrs.module.querystore.bridge.AbstractIndexingAdvice;

public class BillRefundIndexingAdvice extends AbstractIndexingAdvice<BillRefund> {
	
	static final Set<String> TRIGGER_METHODS = new HashSet<>(
	        Arrays.asList("saveBillRefund", "voidBillRefund", "unvoidBillRefund", "purgeBillRefund"));
	
	static final Set<String> PURGE_METHODS = Collections.singleton("purgeBillRefund");
	
	@Override
	protected Class<BillRefund> getSupportedType() {
		return BillRefund.class;
	}
	
	@Override
	protected BillRefundRecordSerializer serializer() {
		return Context.getRegisteredComponent("billing.querystore.serializer.bill_refund", BillRefundRecordSerializer.class);
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
