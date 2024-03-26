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
