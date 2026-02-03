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
	        "paymentStatus", "changedBy", "dateChanged" };
	
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
