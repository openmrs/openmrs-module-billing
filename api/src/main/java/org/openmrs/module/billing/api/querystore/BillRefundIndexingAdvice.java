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

import java.util.Collections;
import java.util.Set;

import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.model.BillRefund;
import org.openmrs.module.querystore.bridge.AbstractIndexingAdvice;

public class BillRefundIndexingAdvice extends AbstractIndexingAdvice<BillRefund> {
	
	// BillRefundService exposes only saveBillRefund — void/unvoid happen by setting the voided
	// flag on the entity and calling saveBillRefund (AbstractIndexingAdvice's per-node voided
	// policy routes voided records to delete on the resave path). There is no purgeBillRefund
	// method, so PURGE_METHODS is empty rather than aspirationally listing a name AOP can never
	// match — if hard-delete becomes a real path it should land on a service method first.
	static final Set<String> TRIGGER_METHODS = Collections.singleton("saveBillRefund");
	
	static final Set<String> PURGE_METHODS = Collections.emptySet();
	
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
