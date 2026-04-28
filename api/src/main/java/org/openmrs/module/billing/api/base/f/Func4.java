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
 * Represents a function with four parameters and a return value.
 *
 * @param <TParm1> The first parameter class.
 * @param <TParm2> The second parameter class.
 * @param <TParm3> The third parameter class.
 * @param <TParm4> The fourth parameter class.
 * @param <TResult> The return value class.
 */
public interface Func4<TParm1, TParm2, TParm3, TParm4, TResult> {
	
	TResult apply(TParm1 parameter1, TParm2 parameter2, TParm3 parameter3, TParm4 parameter4);
}
