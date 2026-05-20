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
import org.openmrs.module.billing.api.model.BillDiscount;
import org.openmrs.module.querystore.bridge.AbstractIndexingAdvice;

public class BillDiscountIndexingAdvice extends AbstractIndexingAdvice<BillDiscount> {
	
	// BillDiscountService exposes only saveBillDiscount — approve / reject / void all happen by
	// mutating the entity and calling saveBillDiscount, routed through AbstractIndexingAdvice's
	// per-node voided policy (voided rows go to delete on the resave path). No purge method
	// exists, so PURGE_METHODS is empty rather than aspirationally listing a name AOP can never
	// match — see IndexingAdviceConfigTest.
	static final Set<String> TRIGGER_METHODS = Collections.singleton("saveBillDiscount");
	
	static final Set<String> PURGE_METHODS = Collections.emptySet();
	
	@Override
	protected Class<BillDiscount> getSupportedType() {
		return BillDiscount.class;
	}
	
	@Override
	protected BillDiscountRecordSerializer serializer() {
		return Context.getRegisteredComponent("billing.querystore.serializer.bill_discount",
		    BillDiscountRecordSerializer.class);
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
