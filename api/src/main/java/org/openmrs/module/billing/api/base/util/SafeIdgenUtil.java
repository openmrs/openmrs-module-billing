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

import java.util.ArrayList;
import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.base.entity.model.SafeIdentifierSource;
import org.openmrs.module.idgen.IdentifierSource;
import org.openmrs.module.idgen.service.IdentifierSourceService;

/**
 * Idgen Utility class that does not directly reference the idgen module.
 */
public class SafeIdgenUtil {
	
	protected SafeIdgenUtil() {
	}
	
	/**
	 * Gets the identifier source information with the id in the specified global property.
	 *
	 * @param propertyName The global property name.
	 * @return The identifier source information or {@code null} if not defined.
	 */
	public static SafeIdentifierSource getIdentifierSourceInfo(String propertyName) {
		SafeIdentifierSource result = null;
		
		IdentifierSource source = IdgenUtil.getIdentifierSource(propertyName);
		if (source != null) {
			result = new SafeIdentifierSource(source.getId(), source.getUuid(), source.getName());
		}
		
		return result;
	}
	
	/**
	 * Gets the information for all defined identifier sources.
	 *
	 * @return A list containing the source information.
	 */
	public static List<SafeIdentifierSource> getAllIdentifierSourceInfo() {
		List<SafeIdentifierSource> results = new ArrayList<SafeIdentifierSource>();
		
		IdentifierSourceService service = Context.getService(IdentifierSourceService.class);
		List<IdentifierSource> sources = service.getAllIdentifierSources(false);
		if (sources != null && !sources.isEmpty()) {
			for (IdentifierSource source : sources) {
				results.add(new SafeIdentifierSource(source.getId(), source.getUuid(), source.getName()));
			}
		}
		
		return results;
	}
}
