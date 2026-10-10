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

import org.openmrs.module.DaemonTokenAware;

/**
 * Marker interface for the billing module's event listeners. A listener reacts to core's entity
 * events (for example with a
 * {@link org.springframework.transaction.event.TransactionalEventListener} method), and the module
 * activator hands every registered {@code BillingEventListener} bean the module's daemon token
 * whenever the context is refreshed, so the listener can do its work as the daemon user.
 * <p>
 * To add a new listener, implement this interface and register the bean in
 * {@code moduleApplicationContext.xml} — no changes to the activator are needed.
 */
public interface BillingEventListener extends DaemonTokenAware {}
