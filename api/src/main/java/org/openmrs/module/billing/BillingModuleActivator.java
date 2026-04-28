/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
		
		subscribeBillingEventListeners();
	}
	
	/**
	 * @see BaseModuleActivator#started()
	 */
	@Override
	public void started() {
		log.info("OpenMRS Billing Module started");
	}
	
	/**
	 * @see BaseModuleActivator#stopped()
	 */
	@Override
	public void stopped() {
		Module module = ModuleFactory.getModuleById(CashierWebConstants.OPENHMIS_CASHIER_MODULE_ID);
		WebModuleUtil.unloadFilters(module);
		
		log.info("OpenMRS Billing Module stopped");
	}
	
	@Override
	public void willRefreshContext() {
		unsubscribeBillingEventListeners();
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
