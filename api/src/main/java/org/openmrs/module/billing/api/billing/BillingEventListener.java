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

import org.openmrs.OpenmrsObject;
import org.openmrs.event.Event;
import org.openmrs.event.EventListener;
import org.openmrs.module.DaemonTokenAware;

/**
 * Marker interface for event listeners that should be auto-subscribed when the billing module
 * starts. Implementations declare which domain class and action they listen to. The module
 * activator discovers all registered {@code BillingEventListener} beans, sets the daemon token, and
 * subscribes them automatically.
 * <p>
 * To add a new listener, implement this interface and register the bean in
 * {@code moduleApplicationContext.xml} — no changes to the activator are needed.
 */
public interface BillingEventListener extends EventListener, DaemonTokenAware {
	
	/**
	 * The OpenMRS domain class this listener subscribes to (e.g. {@code Order.class}).
	 */
	Class<? extends OpenmrsObject> getSubscribedClass();
	
	/**
	 * The event action this listener subscribes to (e.g. {@link Event.Action#CREATED}).
	 */
	Event.Action getSubscribedAction();
}
