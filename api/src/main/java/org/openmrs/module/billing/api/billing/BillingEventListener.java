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
