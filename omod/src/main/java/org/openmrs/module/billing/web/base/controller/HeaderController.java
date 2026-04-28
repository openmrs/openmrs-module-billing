/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.web.base.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.context.Context;
import org.springframework.ui.ModelMap;

/**
 * Retrieves locations for the logged in user and sets the list in session.
 */
public class HeaderController {
	
	public static void render(ModelMap model, HttpServletRequest request) {
		
		HttpSession session = request.getSession();
		Integer locationId = (Integer) session.getAttribute("emrContext.sessionLocationId");
		model.addAttribute("sessionLocationId", locationId);
		model.addAttribute("sessionLocationName", Context.getLocationService().getLocation(locationId).getName());
		
		LocationTag locationTag = Context.getLocationService().getLocationTagByName("Login Location");
		
		List<Location> loginLocations = Context.getLocationService().getLocationsByTag(locationTag);
		
		model.addAttribute("loginLocations", loginLocations);
		model.addAttribute("multipleLoginLocations", loginLocations.size() > 1);
		
	}
}
