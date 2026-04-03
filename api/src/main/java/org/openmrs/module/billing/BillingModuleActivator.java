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
package org.openmrs.module.billing;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.openmrs.api.context.Context;
import org.openmrs.event.Event;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.DaemonTokenAware;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.billing.api.billing.BillingEventListener;
import org.openmrs.module.billing.web.CashierWebConstants;
import org.openmrs.module.web.WebModuleUtil;

/**
 * This class contains the logic that is run every time this module is either started or stopped.
 */
@Slf4j
@Setter
public class BillingModuleActivator extends BaseModuleActivator implements DaemonTokenAware {
	
	private DaemonToken daemonToken;
	
	private final List<BillingEventListener> subscribedListeners = new ArrayList<>();
	
	/**
	 * @see BaseModuleActivator#contextRefreshed()
	 */
	@Override
	public void contextRefreshed() {
		log.info("OpenMRS Billing Module refreshed");
	}
	
	/**
	 * @see BaseModuleActivator#started()
	 */
	@Override
	public void started() {
		log.info("OpenMRS Billing Module started");
		
		subscribeBillingEventListeners();
	}
	
	/**
	 * @see BaseModuleActivator#stopped()
	 */
	@Override
	public void stopped() {
		unsubscribeBillingEventListeners();
		
		Module module = ModuleFactory.getModuleById(CashierWebConstants.OPENHMIS_CASHIER_MODULE_ID);
		WebModuleUtil.unloadFilters(module);
		
		log.info("OpenMRS Billing Module stopped");
	}
	
	private void subscribeBillingEventListeners() {
		if (daemonToken == null) {
			log.error("Cannot subscribe billing event listeners: daemon token has not been set");
			return;
		}
		List<BillingEventListener> listeners = Context.getRegisteredComponents(BillingEventListener.class);
		for (BillingEventListener listener : listeners) {
			try {
				listener.setDaemonToken(daemonToken);
				Event.subscribe(listener.getSubscribedClass(), listener.getSubscribedAction().name(), listener);
				subscribedListeners.add(listener);
				log.info("Subscribed {} to {} {} events", listener.getClass().getSimpleName(),
				    listener.getSubscribedClass().getSimpleName(), listener.getSubscribedAction());
			}
			catch (Exception e) {
				log.error("Failed to subscribe {}", listener.getClass().getSimpleName(), e);
			}
		}
	}
	
	private void unsubscribeBillingEventListeners() {
		for (BillingEventListener listener : subscribedListeners) {
			try {
				Event.unsubscribe(listener.getSubscribedClass(), listener.getSubscribedAction(), listener);
				log.info("Unsubscribed {} from {} {} events", listener.getClass().getSimpleName(),
				    listener.getSubscribedClass().getSimpleName(), listener.getSubscribedAction());
			}
			catch (Exception e) {
				log.error("Failed to unsubscribe {}", listener.getClass().getSimpleName(), e);
			}
		}
		subscribedListeners.clear();
	}
}
