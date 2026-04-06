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

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import java.util.List;

import lombok.Setter;
import org.springframework.core.OrderComparator;
import lombok.extern.slf4j.Slf4j;
import org.openmrs.OpenmrsObject;
import org.openmrs.Order;
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
		List<OrderBillingStrategy> strategies = Context.getRegisteredComponents(OrderBillingStrategy.class);
		OrderComparator.sort(strategies);
		
		for (OrderBillingStrategy strategy : strategies) {
			if (strategy.supports(order)) {
				BillingResult result = strategy.generateBill(order);
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
