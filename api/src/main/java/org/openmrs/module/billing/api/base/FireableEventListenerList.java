/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.base;

import javax.swing.event.EventListenerList;
import java.util.EventListener;

/**
 * An {@link EventListenerList} that provides a 'fire' helper method.
 */
public class FireableEventListenerList extends EventListenerList {
	
	public <T extends EventListener> void fire(Class<T> cls, EventRaiser<T> eventRaiser) {
		T[] listeners = this.getListeners(cls);
		
		for (T listener : listeners) {
			eventRaiser.fire(listener);
		}
	}
}
