/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.db;

import java.util.List;

import javax.annotation.Nonnull;

import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.api.search.CashPointSearch;

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
