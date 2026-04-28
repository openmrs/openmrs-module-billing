/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
