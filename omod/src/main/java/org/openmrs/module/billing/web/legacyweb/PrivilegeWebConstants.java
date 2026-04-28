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

import org.openmrs.module.billing.api.util.PrivilegeConstants;

/**
 * Constants class for privileges required by web resources.
 */
public class PrivilegeWebConstants extends PrivilegeConstants {
	
	public static final String CASHPOINTS_PAGE_PRIVILEDGES = MANAGE_METADATA + "," + VIEW_METADATA;
	
	public static final String PAYMENTSMODES_PAGE_PRIVILEDGES = MANAGE_METADATA + "," + VIEW_METADATA;
	
	public static final String BILL_PAGE_PRIVILEDGES = MANAGE_METADATA + "," + VIEW_METADATA;
	
	public static final String SETTING_PAGE_PRIVILEGE = MANAGE_METADATA;
}
