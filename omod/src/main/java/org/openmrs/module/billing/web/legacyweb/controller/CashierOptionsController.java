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

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.ModuleSettings;
import org.openmrs.module.billing.api.ICashierOptionsService;
import org.openmrs.module.billing.api.model.CashierOptions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller to manage the Cashier Options page.
 */
@Controller
@RequestMapping("/module/billing/options")
public class CashierOptionsController {
	
	public CashierOptionsController() {
		
	}
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public CashierOptions options() {
		CashierOptions options = Context.getService(ICashierOptionsService.class).getOptions();
		
		AdministrationService adminService = Context.getAdministrationService();
		String roundingModeProperty = adminService.getGlobalProperty(ModuleSettings.ROUNDING_MODE_PROPERTY);
		String roundingItemId = adminService.getGlobalProperty(ModuleSettings.ROUNDING_ITEM_ID);
		if (StringUtils.isNotEmpty(roundingModeProperty)) {
			if (StringUtils.isEmpty(options.getRoundingItemUuid()) && StringUtils.isNotEmpty(roundingItemId)) {
				throw new APIException("Rounding item ID set in options but item not found. Make sure your user has the "
				        + "required rights and the item has the set ID in the database");
			}
			
			// Check to see if rounding has been enabled and throw exception if it has as a rounding item must be set
			if (StringUtils.isEmpty(roundingItemId) && options.getRoundToNearest() != null) {
				throw new APIException("Rounding enabled (nearest " + options.getRoundToNearest().toString()
				        + ") but no rounding item ID specified in options.");
			}
		}
		
		return options;
	}
}
