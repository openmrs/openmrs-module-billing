/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.base;

import java.util.Collection;

import org.openmrs.Provider;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;

/**
 * Utility class for {@link org.openmrs.Provider}s.
 */
public class ProviderUtil {
	
	protected ProviderUtil() {
	}
	
	public static Provider getCurrentProvider() {
		return getCurrentProvider(Context.getProviderService());
	}
	
	public static Provider getCurrentProvider(ProviderService providerService) {
		Collection<Provider> providers = providerService.getProvidersByPerson(Context.getAuthenticatedUser().getPerson());
		if (!providers.isEmpty()) {
			return providers.iterator().next();
		}
		
		return null;
	}
}
