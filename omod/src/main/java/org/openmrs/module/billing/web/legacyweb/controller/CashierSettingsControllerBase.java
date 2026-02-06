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

import org.openmrs.module.billing.web.base.controller.HeaderController;
import org.openmrs.module.billing.ModuleSettings;
import org.openmrs.module.billing.api.model.CashierSettings;
import org.openmrs.web.WebConstants;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Base Controller to manage the settings pages.
 */
public abstract class CashierSettingsControllerBase {
	
	@RequestMapping(method = RequestMethod.GET)
	public void render(ModelMap modelMap, HttpServletRequest request) {
		modelMap.addAttribute("cashierSettings", ModuleSettings.loadSettings());
		HeaderController.render(modelMap, request);
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public void submit(HttpServletRequest request, CashierSettings cashierSettings, ModelMap modelMap) {
		ModuleSettings.saveSettings(cashierSettings);
		
		HttpSession session = request.getSession();
		session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "openhmis.cashier.settings.saved");
		
		render(modelMap, request);
	}
}
