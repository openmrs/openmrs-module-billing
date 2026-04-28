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
 * Represents a function with two parameters and no return value.
 *
 * @param <TParm1> The first parameter class.
 * @param <TParm2> The second parameter class
 */
public interface Action2<TParm1, TParm2> {
	
	/**
	 * Executes the action.
	 *
	 * @param parameter1 The first parameter.
	 * @param parameter2 The second parameter.
	 */
	void apply(TParm1 parameter1, TParm2 parameter2);
}
