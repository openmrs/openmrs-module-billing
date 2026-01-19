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

import org.openmrs.Location;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.api.search.CashPointSearch;
import org.openmrs.module.billing.api.util.PrivilegeConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing {@link CashPoint} entities.
 */
public interface CashPointService extends OpenmrsService {
	
	/**
	 * Gets the cash point with the specified id.
	 *
	 * @param id the cash point id
	 * @return the cash point or {@code null} if not found
	 */
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.MANAGE_METADATA })
	CashPoint getCashPoint(Integer id);
	
	/**
	 * Gets the cash point with the specified uuid.
	 *
	 * @param uuid the cash point uuid
	 * @return the cash point or {@code null} if not found
	 */
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.MANAGE_METADATA })
	CashPoint getCashPointByUuid(String uuid);
	
	/**
	 * Searches for cash points using the specified search criteria.
	 *
	 * @param cashPointSearch the search criteria
	 * @param pagingInfo optional paging information
	 * @return a list of cash points matching the criteria, or an empty list if none found
	 */
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.MANAGE_METADATA })
	List<CashPoint> getCashPoints(CashPointSearch cashPointSearch, PagingInfo pagingInfo);
	
	/**
	 * Gets all cash points at the specified location.
	 *
	 * @param location the location to filter by
	 * @param includeRetired whether to include retired cash points
	 * @return a list of cash points at the location, or an empty list if none found
	 * @throws IllegalArgumentException if location is null
	 */
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.MANAGE_METADATA })
	List<CashPoint> getCashPointsByLocation(Location location, boolean includeRetired);
	
	/**
	 * Gets all cash points at the specified location with paging support.
	 *
	 * @param location the location to filter by
	 * @param includeRetired whether to include retired cash points
	 * @param pagingInfo optional paging information
	 * @return a list of cash points at the location, or an empty list if none found
	 * @throws IllegalArgumentException if location is null
	 */
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.MANAGE_METADATA })
	List<CashPoint> getCashPointsByLocation(Location location, boolean includeRetired, PagingInfo pagingInfo);
	
	/**
	 * Gets cash points at the specified location matching the given name.
	 *
	 * @param location the location to filter by
	 * @param name the name to search for (partial match)
	 * @param includeRetired whether to include retired cash points
	 * @return a list of matching cash points, or an empty list if none found
	 * @throws IllegalArgumentException if location is null, name is null/empty, or name exceeds 255
	 *             characters
	 */
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.MANAGE_METADATA })
	List<CashPoint> getCashPointsByLocationAndName(Location location, String name, boolean includeRetired);
	
	/**
	 * Gets cash points at the specified location matching the given name with paging support.
	 *
	 * @param location the location to filter by
	 * @param name the name to search for (partial match)
	 * @param includeRetired whether to include retired cash points
	 * @param pagingInfo optional paging information
	 * @return a list of matching cash points, or an empty list if none found
	 * @throws IllegalArgumentException if location is null, name is null/empty, or name exceeds 255
	 *             characters
	 */
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.MANAGE_METADATA })
	List<CashPoint> getCashPointsByLocationAndName(Location location, String name, boolean includeRetired,
	        PagingInfo pagingInfo);
	
	/**
	 * Gets all cash points.
	 *
	 * @param includeRetired whether to include retired cash points
	 * @return a list of all cash points, or an empty list if none found
	 */
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.MANAGE_METADATA })
	List<CashPoint> getAllCashPoints(boolean includeRetired);
	
	/**
	 * Saves or updates the specified cash point.
	 *
	 * @param cashPoint the cash point to save
	 * @return the saved cash point
	 * @throws IllegalArgumentException if cashPoint is null
	 */
	@Transactional
	@Authorized({ PrivilegeConstants.MANAGE_METADATA })
	CashPoint saveCashPoint(CashPoint cashPoint);
	
	/**
	 * Permanently deletes the specified cash point from the database.
	 *
	 * @param cashPoint the cash point to purge
	 * @throws IllegalArgumentException if cashPoint is null
	 */
	@Transactional
	@Authorized({ PrivilegeConstants.MANAGE_METADATA })
	void purgeCashPoint(CashPoint cashPoint);
	
	/**
	 * Retires the specified cash point with the given reason.
	 *
	 * @param cashPoint the cash point to retire
	 * @param retireReason the reason for retiring
	 * @return the retired cash point
	 * @throws IllegalArgumentException if retireReason is empty or null
	 */
	@Transactional
	@Authorized({ PrivilegeConstants.MANAGE_METADATA })
	CashPoint retireCashPoint(CashPoint cashPoint, String retireReason);
	
	/**
	 * Unretires the specified cash point.
	 *
	 * @param cashPoint the cash point to unretire
	 * @return the unretired cash point
	 */
	@Transactional
	@Authorized({ PrivilegeConstants.MANAGE_METADATA })
	CashPoint unretireCashPoint(CashPoint cashPoint);
}
