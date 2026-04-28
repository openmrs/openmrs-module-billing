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

import org.openmrs.module.billing.web.CashierWebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * Controller to manage the Message Render page.
 */
@Controller
@RequestMapping(CashierWebConstants.MESSAGE_PROPERTIES_JS_URI)
public class CashierMessageRenderController {
	
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView render(HttpServletRequest request) {
		// object to store keys from cashier and backboneforms
		
		// locate and retrieve cashier messages
		Locale locale = RequestContextUtils.getLocale(request);
		ResourceBundle resourceBundle = ResourceBundle.getBundle("messages", locale);
		
		// store cashier message keys in the vector object
		List<String> keys = new ArrayList<>(resourceBundle.keySet());
		
		// retrieve backboneforms messages
		/**
		 * BackboneMessageRenderController backboneController = new BackboneMessageRenderController();
		 * ModelAndView modelAndView = backboneController.render(request); // store backboneforms message
		 * keys in the vector object for (Map.Entry<String, Object> messageKeys :
		 * modelAndView.getModel().entrySet()) { Enumeration<String> messageKey =
		 * (Enumeration<String>)messageKeys.getValue(); while (messageKey.hasMoreElements()) { String key =
		 * messageKey.nextElement(); if (!keys.contains(key)) keys.add(key); } }
		 */
		
		return new ModelAndView(CashierWebConstants.MESSAGE_PAGE, "keys", keys);
	}
}
