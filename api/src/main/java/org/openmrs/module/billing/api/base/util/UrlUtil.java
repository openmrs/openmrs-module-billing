/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.base.util;

/**
 * Utility class for URLs
 */
public class UrlUtil {
	
	private UrlUtil() {
	}
	
	/**
	 * Adds the '.form' ending to the specified page, if it does not already exist.
	 *
	 * @param page The page to add the form ending to.
	 * @return The page with '.form' appended to the end.
	 */
	public static String formUrl(String page) {
		return page.endsWith(".form") ? page : page + ".form";
	}
	
	/**
	 * Creates the redirect url for the specified page.
	 *
	 * @param page The page to redirect to.
	 * @return The redirect url.
	 */
	public static String redirectUrl(String page) {
		return "redirect:" + formUrl(page);
	}
}
