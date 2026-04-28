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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.idgen.IdentifierSource;
import org.openmrs.module.idgen.service.IdentifierSourceService;

/**
 * Utility class for generating identifiers using the Idgen module.
 */
@Slf4j
public class IdgenUtil {
	
	private IdgenUtil() {
	}
	
	/**
	 * Gets the {@link org.openmrs.module.idgen.IdentifierSource} with the id in the specified global
	 * property.
	 *
	 * @param propertyName The global property name.
	 * @return The IdentifierSource object.
	 */
	public static IdentifierSource getIdentifierSource(String propertyName) {
		if (StringUtils.isEmpty(propertyName)) {
			throw new IllegalArgumentException("The property name for the identifier source must be defined.");
		}
		
		AdministrationService administrationService = Context.getAdministrationService();
		IdentifierSourceService service = Context.getService(IdentifierSourceService.class);
		
		IdentifierSource source = null;
		String property = administrationService.getGlobalProperty(propertyName);
		int sourceId;
		try {
			sourceId = Integer.parseInt(property);
			
			source = service.getIdentifierSource(sourceId);
		}
		catch (Exception ex) {
			log.warn("Could not convert '{}' into an integer.", property);
		}
		
		return source;
	}
	
	/**
	 * Generates a new identifier for the {@link org.openmrs.module.idgen.IdentifierSource} defined in
	 * the specified global property name.
	 *
	 * @param generatorSourcePropertyName The global property name.
	 * @return The new identifier.
	 */
	public static String generateId(String generatorSourcePropertyName) {
		IdentifierSource source = getIdentifierSource(generatorSourcePropertyName);
		
		return generateId(source);
	}
	
	/**
	 * Generates a new identifier for the specified {@link org.openmrs.module.idgen.IdentifierSource}.
	 *
	 * @param source The IdentifierSource object.
	 * @return The new identifier.
	 */
	public static String generateId(IdentifierSource source) {
		if (source == null) {
			throw new IllegalArgumentException("The identifier source to generate the new identifier from is required.");
		}
		
		IdentifierSourceService service = Context.getService(IdentifierSourceService.class);
		return service.generateIdentifier(source, "Generating stock operation number.");
	}
}
