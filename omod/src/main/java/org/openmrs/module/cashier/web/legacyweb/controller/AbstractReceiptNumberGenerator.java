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
package org.openmrs.module.cashier.web.legacyweb.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.cashier.web.base.controller.HeaderController;
import org.openmrs.module.cashier.ModuleSettings;
import org.openmrs.module.cashier.api.IReceiptNumberGenerator;
import org.openmrs.module.cashier.api.ReceiptNumberGeneratorFactory;
import org.openmrs.module.cashier.api.base.util.UrlUtil;
import org.openmrs.module.cashier.api.util.PrivilegeConstants;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Abstract Receipt Number Generator Functionality.
 */
public abstract class AbstractReceiptNumberGenerator {

	private static final Log LOG = LogFactory.getLog(AbstractReceiptNumberGenerator.class);

	public abstract String getReceiptNumberGeneratorUrl();

	@RequestMapping(method = RequestMethod.GET)
	@Authorized(PrivilegeConstants.MANAGE_BILLS)
	public void render(ModelMap model, HttpServletRequest request) throws IOException {
		IReceiptNumberGenerator currentGenerator = ReceiptNumberGeneratorFactory.getGenerator();
		IReceiptNumberGenerator[] generators = ReceiptNumberGeneratorFactory.locateGenerators();

		model.addAttribute("currentGenerator", currentGenerator);
		model.addAttribute("generators", generators);

		model.addAttribute("settings", ModuleSettings.loadSettings());

		HeaderController.render(model, request);
	}

	@RequestMapping(method = RequestMethod.POST)
	@Authorized(PrivilegeConstants.MANAGE_BILLS)
	public String submit(ModelMap model, @RequestParam(value = "selectedGenerator", required = true) String generatorName) {
		IReceiptNumberGenerator[] generators = ReceiptNumberGeneratorFactory.locateGenerators();
		IReceiptNumberGenerator selectedGenerator = null;

		// If no generator has been defined then remove the current one
		if (StringUtils.isEmpty(generatorName)) {
			ReceiptNumberGeneratorFactory.setGenerator(null);
		} else {
			// Get the selected generator
			for (IReceiptNumberGenerator generator : generators) {
				if (generator.getName().equals(generatorName)) {
					selectedGenerator = generator;
				}
			}

			// Load the generator configuration page, if defined
			if (selectedGenerator == null) {
				LOG.warn("Could not locate a receipt number generator named '" + generatorName + "'.");
			} else if (StringUtils.isEmpty(selectedGenerator.getConfigurationPage())) {
				// There is no generator configuration page so just set the system generator and reload the page
				ReceiptNumberGeneratorFactory.setGenerator(selectedGenerator);
			} else {
				// The configuration page should set the system generator when saved so it is not done here
				String configurationPage = selectedGenerator.getConfigurationPage();
				if (StringUtils.contains(getReceiptNumberGeneratorUrl(), "2x")) {
					configurationPage += "2x";
				}
				return UrlUtil.redirectUrl(configurationPage);
			}
		}

		// By default, the page will simply reload with the selected generator
		model.addAttribute("currentGenerator", selectedGenerator);
		model.addAttribute("generators", generators);

		return UrlUtil.redirectUrl(getReceiptNumberGeneratorUrl());
	}
}
