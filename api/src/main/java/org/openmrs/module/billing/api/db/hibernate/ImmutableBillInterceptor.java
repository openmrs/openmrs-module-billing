package org.openmrs.module.billing.api.db.hibernate;

import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.type.Type;
import org.openmrs.api.UnchangeableObjectException;
import org.openmrs.api.db.hibernate.ImmutableEntityInterceptor;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillStatus;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * <p>
 * This implements the semi-immutable rules for a Bill.
 * </p>
 * <p>
 * Specifically, the business logic is this:
 * <ol>
 * <li>When initially created, a bill is PENDING. In the PENDING status, any property is
 * editable</li>
 * <li>Once a bill has been moved into a non-PENDING state, only payments or adjustments can be made
 * to the bill</li>
 * </ol>
 * </p>
 */
@Component("immutableBillInterceptor")
public class ImmutableBillInterceptor extends ImmutableEntityInterceptor {
	
	private static final String[] MUTABLE_PROPERTY_NAMES = new String[] { "changedBy", "dateChanged", "voided", "dateVoided",
	        "voidedBy", "voidReason", "payment", "billAdjusted", "adjustmentReason", "adjustedBy", "receiptPrinted",
	        "status", "receiptNumber" };
	
	@Override
	protected Class<?> getSupportedType() {
		return Bill.class;
	}
	
	@Override
	protected String[] getMutablePropertyNames() {
		return MUTABLE_PROPERTY_NAMES;
	}
	
	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
	        String[] propertyNames, Type[] types) {
		boolean isBill = Bill.class.isAssignableFrom(entity.getClass());
		if (isBill) {
			Bill bill = (Bill) entity;
			if (bill.editable()) {
				return false;
			}
		}
		
		boolean result = super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
		
		if (isBill) {
			int idx = ArrayUtils.indexOf(propertyNames, "status");
			if (idx > 0) {
				BillStatus previousStatus = (BillStatus) previousState[idx];
				BillStatus currentStatus = (BillStatus) currentState[idx];
				
				if (currentStatus == BillStatus.PENDING && previousStatus != BillStatus.PENDING) {
					throw new UnchangeableObjectException("editing.fields.not.allowed", new Object[] { "status" });
				}
			}
		}
		
		return result;
	}
}
