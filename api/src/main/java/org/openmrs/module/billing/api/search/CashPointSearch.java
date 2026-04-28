/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.search;

import lombok.Builder;
import lombok.Data;

/**
 * Search criteria for querying {@link org.openmrs.module.billing.api.model.CashPoint} entities.
 */
@Data
@Builder
public class CashPointSearch {
	
	/**
	 * The UUID of the location to filter cash points by.
	 */
	private String locationUuid;
	
	/**
	 * The name pattern to search for (partial match).
	 */
	private String name;
	
	/**
	 * Whether to include retired cash points in the results.
	 */
	private boolean includeRetired;
	
}
