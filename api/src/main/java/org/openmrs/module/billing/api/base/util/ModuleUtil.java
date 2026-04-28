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

import org.openmrs.module.ModuleFactory;

/**
 * Utility class for working with modules.
 */
public class ModuleUtil {
	
	public static final String IDGEN_MODULE_ID = "idgen";
	
	public static Boolean isLoaded(String moduleId) {
		return ModuleFactory.getStartedModuleById(moduleId) != null;
	}
}
