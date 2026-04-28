/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.util;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.Provider;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.ModuleSettings;
import org.openmrs.module.billing.api.ITimesheetService;
import org.openmrs.module.billing.api.base.ProviderUtil;
import org.openmrs.module.billing.api.model.Timesheet;

/**
 * Utility class fo {@link Timesheet}
 */
@Slf4j
public class TimesheetUtil {
	
	protected TimesheetUtil() {
	}
	
	public static Timesheet getCurrentTimesheet() {
		Provider provider;
		Timesheet timesheet;
		ProviderService providerService = Context.getProviderService();
		try {
			provider = ProviderUtil.getCurrentProvider(providerService);
		}
		catch (Exception e) {
			throw new APIException("Error retrieving provider for current user.", e);
		}
		
		ITimesheetService tsService = Context.getService(ITimesheetService.class);
		try {
			timesheet = tsService.getCurrentTimesheet(provider);
		}
		catch (Exception e) {
			log.error("Error occured while trying to get the current timesheet{}", String.valueOf(e));
			return null;
		}
		
		return timesheet;
	}
	
	public static boolean isTimesheetRequired() {
		AdministrationService adminService = Context.getAdministrationService();
		boolean timesheetRequired;
		try {
			timesheetRequired = Boolean
			        .parseBoolean(adminService.getGlobalProperty(ModuleSettings.TIMESHEET_REQUIRED_PROPERTY));
		}
		catch (Exception e) {
			log.error("Error occured while trying to parse the boolean value{}", String.valueOf(e));
			timesheetRequired = false;
		}
		return timesheetRequired;
	}
}
