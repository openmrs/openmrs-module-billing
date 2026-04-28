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

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import java.util.List;

import lombok.Setter;
import org.springframework.core.OrderComparator;
import lombok.extern.slf4j.Slf4j;
import org.openmrs.OpenmrsObject;
import org.openmrs.Order;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.event.Event;
import org.openmrs.module.DaemonToken;

/**
 * Listens for Order CREATED events from the OpenMRS Event module and delegates billing to the
 * appropriate {@link OrderBillingStrategy}.
 */
@Slf4j
@Setter
public class OrderBillingEventListener implements BillingEventListener {
	
	private DaemonToken daemonToken;
	
	@Override
	public Class<? extends OpenmrsObject> getSubscribedClass() {
		return Order.class;
	}
	
	@Override
	public Event.Action getSubscribedAction() {
		return Event.Action.CREATED;
	}
	
	@Override
	public void onMessage(Message message) {
		if (daemonToken == null) {
			log.error("Cannot process order billing event: daemon token not set");
			return;
		}
		
		Daemon.runInDaemonThread(() -> {
			try {
				processMessage(message);
			}
			catch (Exception e) {
				log.error("Error processing order billing event", e);
			}
		}, daemonToken);
	}
	
	private void processMessage(Message message) throws JMSException {
		MapMessage mapMessage = (MapMessage) message;
		String uuid = mapMessage.getString("uuid");
		String action = mapMessage.getString("action");
		
		if (!"CREATED".equals(action)) {
			return;
		}
		
		Order order = Context.getOrderService().getOrderByUuid(uuid);
		if (order == null) {
			log.warn("Order not found for UUID: {}", uuid);
			return;
		}
		
		processOrder(order);
	}
	
	/**
	 * Process a single order through the billing strategy chain. Package-visible so that integration
	 * tests can invoke the billing pipeline directly without requiring a JMS broker.
	 *
	 * @param order a persisted order
	 */
	void processOrder(Order order) {
		Order realOrder = HibernateUtil.getRealObjectFromProxy(order);
		List<OrderBillingStrategy> strategies = Context.getRegisteredComponents(OrderBillingStrategy.class);
		OrderComparator.sort(strategies);
		
		for (OrderBillingStrategy strategy : strategies) {
			if (strategy.supports(realOrder)) {
				BillingResult result = strategy.handleOrder(realOrder);
				switch (result.getAction()) {
					case CREATED:
						log.info("Bill {} created for order {} by {}", result.getBill().getUuid(), order.getUuid(),
						    strategy.getClass().getSimpleName());
						break;
					case UPDATED:
						log.info("Bill {} updated for order {} by {}", result.getBill().getUuid(), order.getUuid(),
						    strategy.getClass().getSimpleName());
						break;
					case DISCONTINUED:
						log.info("Line item voided for order {} by {}", order.getUuid(),
						    strategy.getClass().getSimpleName());
						break;
					case SKIPPED:
						log.info("Order {} skipped by {}: {}", order.getUuid(), strategy.getClass().getSimpleName(),
						    result.getReason());
						break;
				}
				return;
			}
		}
		
		log.debug("No billing strategy found for order type: {}", order.getClass().getSimpleName());
	}
}
