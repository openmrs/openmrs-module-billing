/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.billing.api.billing;

import org.openmrs.Order;
import org.openmrs.Provider;
import org.openmrs.module.billing.api.model.CashPoint;
import org.springframework.core.Ordered;

/**
 * Strategy for generating a bill from an order. Implementations are Spring beans discovered via
 * {@code Context.getRegisteredComponents(OrderBillingStrategy.class)} and sorted by
 * {@link Ordered#getOrder()} (lowest value = highest priority). The first strategy whose
 * {@link #supports(Order)} returns {@code true} handles the order. To override a default strategy,
 * register a bean with a lower order value so it is evaluated first.
 */
public interface OrderBillingStrategy extends Ordered {
	
	/**
	 * Whether this strategy can handle the given order.
	 *
	 * @param order the order to evaluate
	 * @return true if this strategy should process the order
	 */
	boolean supports(Order order);
	
	/**
	 * Generate and persist a bill for the given order. Implementations should check for duplicates
	 * (idempotency) before creating a new bill.
	 *
	 * @param order a persisted order (guaranteed to exist in the database)
	 * @return the billing result indicating what action was taken
	 */
	BillingResult handleOrder(Order order);
	
	/**
	 * Resolve the provider to set as the cashier on the bill.
	 *
	 * @param order the order being billed
	 * @return the cashier provider, or null if one cannot be determined
	 */
	Provider resolveCashier(Order order);
	
	/**
	 * Resolve the cash point for the bill.
	 *
	 * @return the cash point, or null if one cannot be determined
	 */
	CashPoint resolveCashPoint();
}
