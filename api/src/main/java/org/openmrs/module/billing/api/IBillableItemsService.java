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
package org.openmrs.module.billing.api;

import java.util.List;

import org.openmrs.Concept;
import org.openmrs.module.billing.api.base.entity.IMetadataDataService;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.search.BillableServiceSearch;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface IBillableItemsService extends IMetadataDataService<BillableService> {
	
	List<BillableService> findServices(final BillableServiceSearch search);
	
	/**
	 * Find a billable service by name (case-insensitive).
	 * 
	 * @param name The name to search for
	 * @return The billable service with the given name, or null if not found
	 */
	@Transactional(readOnly = true)
	BillableService findByName(String name);
	
	/**
	 * Find a billable service by short name (case-insensitive).
	 * 
	 * @param shortName The short name to search for
	 * @return The billable service with the given short name, or null if not found
	 */
	@Transactional(readOnly = true)
	BillableService findByShortName(String shortName);
	
	/**
	 * Find a billable service by service type.
	 * 
	 * @param serviceType The service type concept to search for
	 * @return The billable service with the given service type, or null if not found
	 */
	@Transactional(readOnly = true)
	BillableService findByServiceType(Concept serviceType);
}
