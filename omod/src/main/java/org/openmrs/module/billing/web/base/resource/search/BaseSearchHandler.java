/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.web.base.resource.search;

import org.apache.commons.lang.StringUtils;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.billing.api.base.entity.IObjectDataService;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;

/**
 * Provides helper methods for search handlers.
 */
public abstract class BaseSearchHandler implements SearchHandler {
	
	/**
	 * Gets an optional entity by uuid.
	 *
	 * @param service The entity service.
	 * @param uuid The entity uuid.
	 * @param <T> The entity class.
	 * @return The entity object or {@code null} if not defined or found.
	 */
	protected <T extends OpenmrsObject> T getOptionalEntityByUuid(IObjectDataService<T> service, String uuid) {
		T entity = null;
		if (!StringUtils.isEmpty(uuid)) {
			entity = service.getByUuid(uuid);
		}
		
		return entity;
	}
}
