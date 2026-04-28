/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.web.base.resource;

import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.webservices.rest.web.RequestContext;

/**
 * Utility class for extracting paging information from a request
 */
public class PagingUtil {
	
	private PagingUtil() {
	}
	
	public static PagingInfo getPagingInfoFromContext(RequestContext context) {
		int page = (context.getStartIndex() / context.getLimit()) + 1;
		return new PagingInfo(page, context.getLimit());
	}
}
