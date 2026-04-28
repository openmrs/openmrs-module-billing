/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.base.web;

/**
 * Base constants used to store module web resources and urls.
 */
public class WebConstants {
	
	protected static final String MODULE_BASE = "/module/billing/";
	
	protected static final String MODULE_RESOURCE_BASE = "/moduleResources/billing/";
	
	public static final String REPORT_DOWNLOAD_URL = "/moduleServlet/jasperreport/jreportDownload";
	
	protected WebConstants() {
	}
}
