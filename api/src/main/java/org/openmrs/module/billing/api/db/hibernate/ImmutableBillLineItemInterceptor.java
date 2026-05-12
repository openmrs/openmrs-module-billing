/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.db.hibernate;

import java.io.Serializable;

import org.hibernate.type.Type;
import org.openmrs.api.db.hibernate.ImmutableEntityInterceptor;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.springframework.stereotype.Component;

/**
 * Interceptor that prevents modifications to BillLineItem entities when the parent Bill is not
 * editable (i.e., not in PENDING status).
 */
@Component("immutableBillLineItemInterceptor")
public class ImmutableBillLineItemInterceptor extends ImmutableEntityInterceptor {
	
	private static final String[] MUTABLE_PROPERTY_NAMES = new String[] { "voided", "dateVoided", "voidedBy", "voidReason",
	        "status", "changedBy", "dateChanged" };
	
	@Override
	protected Class<?> getSupportedType() {
		return BillLineItem.class;
	}
	
	@Override
	protected String[] getMutablePropertyNames() {
		return MUTABLE_PROPERTY_NAMES;
	}
	
	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
	        String[] propertyNames, Type[] types) {
		if (!BillLineItem.class.isAssignableFrom(entity.getClass())) {
			return false;
		}
		
		BillLineItem lineItem = (BillLineItem) entity;
		Bill bill = lineItem.getBill();
		
		// If the bill is editable, allow all modifications
		if (bill == null || bill.editable()) {
			return false;
		}
		
		// Bill is not editable - only allow mutable properties to be changed
		return super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
	}
}
