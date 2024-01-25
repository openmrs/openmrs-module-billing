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
package org.openmrs.module.cashier.web.legacyweb;

import org.openmrs.module.cashier.web.CashierWebConstants;
import org.openmrs.module.webservices.rest.web.RestConstants;

/**
 * Constants class for REST urls.
 */
public class CashierRestConstants extends CashierWebConstants {
	public static final String CASHIER_REST_ROOT = RestConstants.VERSION_2 + "/cashier/";

	public static final String CASH_POINT_RESOURCE = CASHIER_REST_ROOT + "cashPoint";
}
