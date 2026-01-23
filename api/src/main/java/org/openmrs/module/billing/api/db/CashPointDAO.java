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
package org.openmrs.module.billing.api.db;

import java.util.List;

import javax.annotation.Nonnull;

import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.api.search.CashPointSearch;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data access object for {@link CashPoint} entities.
 */
public interface CashPointDAO {
	
	/**
	 * Gets the cash point with the specified id.
	 *
	 * @param id the cash point id
	 * @return the cash point or {@code null} if not found
	 */
	CashPoint getCashPoint(@Nonnull Integer id);
	
	/**
	 * Gets the cash point with the specified uuid.
	 *
	 * @param uuid the cash point uuid
	 * @return the cash point or {@code null} if not found
	 */
	CashPoint getCashPointByUuid(@Nonnull String uuid);
	
	/**
	 * Searches for cash points using the specified search criteria.
	 *
	 * @param cashPointSearch the search criteria
	 * @param pagingInfo optional paging information
	 * @return a list of cash points matching the criteria, or an empty list if none found
	 */
	List<CashPoint> getCashPoints(@Nonnull CashPointSearch cashPointSearch, PagingInfo pagingInfo);
	
	/**
	 * Saves or updates the specified cash point.
	 *
	 * @param cashPoint the cash point to save
	 * @return the saved cash point
	 */
	CashPoint saveCashPoint(@Nonnull CashPoint cashPoint);
	
	/**
	 * Permanently deletes the specified cash point from the database.
	 *
	 * @param cashPoint the cash point to purge
	 */
	void purgeCashPoint(@Nonnull CashPoint cashPoint);
}
