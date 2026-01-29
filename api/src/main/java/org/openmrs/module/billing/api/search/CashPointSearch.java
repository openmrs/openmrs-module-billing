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
