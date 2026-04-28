/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.billing.impl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.openmrs.Order;
import org.openmrs.Provider;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.module.billing.api.billing.BillingResult;
import org.openmrs.module.billing.api.billing.OrderBillingStrategy;
import org.openmrs.module.billing.api.model.CashPoint;
import org.springframework.core.Ordered;

import java.util.Set;

/**
 * Minimal base class for {@link OrderBillingStrategy} implementations. Provides the framework for
 * deproxying orders, filtering by supported actions, and routing by order action. Subclasses that
 * want the default bill creation, voiding, and exemption logic should extend
 * {@link AbstractDefaultOrderBillingStrategy} instead.
 */
@Slf4j
public abstract class AbstractOrderBillingStrategy implements OrderBillingStrategy {
	
	@Getter
	@Setter(AccessLevel.PROTECTED)
	private Set<Order.Action> supportedActions;
	
	@Override
	public BillingResult handleOrder(Order order) {
		try {
			switch (order.getAction()) {
				case NEW:
					return handleNewOrder(order);
				case RENEW:
					return handleRenewOrder(order);
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
		return supportedActions.contains(realOrder.getAction()) && supportsOrder(realOrder);
	}
	
	/**
	 * Whether this strategy handles the given (already deproxied) order. Subclasses only need to check
	 * the order type here — the base class handles action filtering and deproxying.
	 */
	protected abstract boolean supportsOrder(Order order);
	
	protected abstract BillingResult handleNewOrder(Order order);
	
	protected BillingResult handleRenewOrder(Order order) {
		return handleNewOrder(order);
	}
	
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
