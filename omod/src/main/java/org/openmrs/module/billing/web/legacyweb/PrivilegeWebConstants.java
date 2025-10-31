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
