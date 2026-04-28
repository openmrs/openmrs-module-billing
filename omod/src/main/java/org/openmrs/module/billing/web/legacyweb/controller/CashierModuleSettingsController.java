/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.web.legacyweb.controller;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.model.Timesheet;
import org.openmrs.module.billing.api.util.TimesheetUtil;
import org.openmrs.module.billing.web.CashierWebConstants;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Type of a RestController to check up setting values in the Cashier Module Settings.
 */
@Controller(value = "cashierModuleSettings")
@RequestMapping(CashierWebConstants.MODULE_SETTINGS_PAGE)
public class CashierModuleSettingsController {
	
	public CashierModuleSettingsController() {
		
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	public SimpleObject get(@RequestParam("setting") String setting) {
		SimpleObject results = new SimpleObject();
		if (StringUtils.isNotEmpty(setting)) {
			if (StringUtils.equalsIgnoreCase(setting, "timesheet")) {
				results.put("isTimeSheetRequired", TimesheetUtil.isTimesheetRequired());
				Timesheet currentTimesheet = getCurrentTimesheet();
				if (currentTimesheet != null) {
					SimpleObject cashPoint = new SimpleObject();
					cashPoint.put("name", currentTimesheet.getCashPoint().getName());
					cashPoint.put("uuid", currentTimesheet.getCashPoint().getUuid());
					results.put("cashPoint", cashPoint);
					results.put("cashier", currentTimesheet.getCashier().getName());
				}
			} else {
				results.put("results", Context.getAdministrationService().getGlobalProperty(setting));
			}
		}
		return results;
	}
	
	private Timesheet getCurrentTimesheet() {
		Timesheet timesheet;
		try {
			timesheet = TimesheetUtil.getCurrentTimesheet();
		}
		catch (Exception e) {
			timesheet = null;
		}
		return timesheet;
	}
}
