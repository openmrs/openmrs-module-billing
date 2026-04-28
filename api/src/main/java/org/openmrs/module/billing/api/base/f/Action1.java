/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.base.f;

/**
 * Represents a function with a single parameter and no return value.
 *
 * @param <TParm1> The first parameter class.
 */
public interface Action1<TParm1> {
	
	/**
	 * Executes the action.
	 *
	 * @param parameter The parameter.
	 */
	void apply(TParm1 parameter);
}
