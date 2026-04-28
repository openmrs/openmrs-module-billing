/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.web.legacyweb;

import org.openmrs.module.billing.web.CashierWebConstants;
import org.openmrs.module.webservices.rest.web.RestConstants;

/**
 * Constants class for REST urls.
 */
public class CashierRestConstants extends CashierWebConstants {
	
	public static final String CASHIER_REST_ROOT = RestConstants.VERSION_2 + "/billing";
	
	public static final String CASH_POINT_RESOURCE = CASHIER_REST_ROOT + "cashPoint";
}
