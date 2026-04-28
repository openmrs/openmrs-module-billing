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

import javax.servlet.http.HttpServletRequest;

import org.openmrs.module.billing.web.base.controller.HeaderController;
import org.openmrs.module.billing.ModuleSettings;
import org.openmrs.module.billing.api.ISequentialReceiptNumberGeneratorService;
import org.openmrs.module.billing.api.ReceiptNumberGeneratorFactory;
import org.openmrs.module.billing.api.SequentialReceiptNumberGenerator;
import org.openmrs.module.billing.api.base.util.UrlUtil;
import org.openmrs.module.billing.api.model.SequentialReceiptNumberGeneratorModel;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

/**
 * Abstract sequential receipt number generator functionality
 */
public abstract class AbstractSequentialReceiptNumberGenerator {
	
	public abstract ISequentialReceiptNumberGeneratorService getService();
	
	public abstract String getReceiptNumberGeneratorUrl();
	
	@RequestMapping(method = RequestMethod.GET)
	public void render(ModelMap modelMap, HttpServletRequest request) {
		SequentialReceiptNumberGeneratorModel model = getService().getOnly();
		
		modelMap.addAttribute("generator", model);
		
		modelMap.addAttribute("settings", ModuleSettings.loadSettings());
		
		HeaderController.render(modelMap, request);
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public String post(@ModelAttribute("generator") SequentialReceiptNumberGeneratorModel generator, WebRequest request) {
		if (generator.getSeparator().equals("<space>")) {
			generator.setSeparator(" ");
		}
		
		// The check digit checkbox value is only bound if checked
		if (request.getParameter("includeCheckDigit") == null) {
			generator.setIncludeCheckDigit(false);
		}
		
		// Save the generator settings
		getService().save(generator);
		
		// Set the system generator
		ReceiptNumberGeneratorFactory.setGenerator(new SequentialReceiptNumberGenerator());
		return UrlUtil.redirectUrl(getReceiptNumberGeneratorUrl());
	}
}
