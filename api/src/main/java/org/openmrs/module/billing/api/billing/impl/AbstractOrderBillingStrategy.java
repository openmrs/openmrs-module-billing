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
package org.openmrs.module.billing.api.billing.impl;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.Order;
import org.openmrs.Provider;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.module.billing.api.billing.BillingResult;
import org.openmrs.module.billing.api.billing.OrderBillingStrategy;
import org.openmrs.module.billing.api.model.CashPoint;
import org.springframework.core.Ordered;

/**
 * Minimal base class for {@link OrderBillingStrategy} implementations. Provides the framework for
 * deproxying orders, filtering by supported actions, and routing by order action. Subclasses that
 * want the default bill creation, voiding, and exemption logic should extend
 * {@link AbstractDefaultOrderBillingStrategy} instead.
 */
@Slf4j
public abstract class AbstractOrderBillingStrategy implements OrderBillingStrategy {
	
	@Override
	public BillingResult handleOrder(Order order) {
		try {
			switch (order.getAction()) {
				case NEW:
					return handleNewOrder(order);
				case REVISE:
					return handleRevisedOrder(order);
				case DISCONTINUE:
					return handleDiscontinuedOrder(order);
				default:
					return BillingResult.skipped("Unsupported order action: " + order.getAction());
			}
		}
		catch (Exception e) {
			log.error("Error processing order (action={}): {}", order.getAction(), e.getMessage(), e);
			return BillingResult.skipped(e.getMessage());
		}
	}
	
	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}
	
	@Override
	public final boolean supports(Order order) {
		Order realOrder = HibernateUtil.getRealObjectFromProxy(order);
		Order.Action action = realOrder.getAction();
		boolean supportedAction = action == Order.Action.NEW || action == Order.Action.REVISE
		        || action == Order.Action.DISCONTINUE;
		return supportedAction && supportsOrder(realOrder);
	}
	
	/**
	 * Whether this strategy handles the given (already deproxied) order. Subclasses only need to check
	 * the order type here — the base class handles action filtering and deproxying.
	 */
	protected abstract boolean supportsOrder(Order order);
	
	protected abstract BillingResult handleNewOrder(Order order);
	
	protected BillingResult handleRevisedOrder(Order order) {
		return handleNewOrder(order);
	}
	
	protected BillingResult handleDiscontinuedOrder(Order order) {
		return BillingResult.skipped("No discontinue handler implemented");
	}
	
	@Override
	public Provider resolveCashier(Order order) {
		return null;
	}
	
	@Override
	public CashPoint resolveCashPoint() {
		return null;
	}
}
