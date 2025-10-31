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
