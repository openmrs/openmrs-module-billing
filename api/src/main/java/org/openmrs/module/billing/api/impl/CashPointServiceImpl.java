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
package org.openmrs.module.billing.api.impl;

import java.util.Collections;
import java.util.List;

import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.billing.api.CashPointService;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.db.CashPointDAO;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.api.search.CashPointSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link CashPointService}.
 */
@Transactional
public class CashPointServiceImpl extends BaseOpenmrsService implements CashPointService {
	
	@Setter(onMethod_ = { @Autowired })
	private CashPointDAO cashPointDAO;
	
	private static final int MAX_CASHPOINT_NAME_CHARACTERS = 255;
	
	/**
	 * @inheritDoc
	 */
	@Override
	@Transactional(readOnly = true)
	public CashPoint getCashPoint(Integer id) {
		if (id == null) {
			return null;
		}
		return cashPointDAO.getCashPoint(id);
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	@Transactional(readOnly = true)
	public CashPoint getCashPointByUuid(String uuid) {
		if (StringUtils.isEmpty(uuid)) {
			return null;
		}
		return cashPointDAO.getCashPointByUuid(uuid);
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	@Transactional(readOnly = true)
	public List<CashPoint> getCashPoints(CashPointSearch cashPointSearch, PagingInfo pagingInfo) {
		if (cashPointSearch == null) {
			return Collections.emptyList();
		}
		return cashPointDAO.getCashPoints(cashPointSearch, pagingInfo);
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	@Transactional(readOnly = true)
	public List<CashPoint> getCashPointsByLocation(Location location, boolean includeRetired) {
		return getCashPointsByLocation(location, includeRetired, null);
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	@Transactional(readOnly = true)
	public List<CashPoint> getCashPointsByLocation(final Location location, final boolean includeRetired,
	        PagingInfo pagingInfo) {
		if (location == null) {
			throw new IllegalArgumentException("Location cannot be null");
		}
		CashPointSearch cashPointSearch = CashPointSearch.builder().locationUuid(location.getUuid())
		        .includeRetired(includeRetired).build();
		return cashPointDAO.getCashPoints(cashPointSearch, pagingInfo);
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	@Transactional(readOnly = true)
	public List<CashPoint> getCashPointsByLocationAndName(Location location, String name, boolean includeRetired) {
		return getCashPointsByLocationAndName(location, name, includeRetired, null);
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	@Transactional(readOnly = true)
	public List<CashPoint> getCashPointsByLocationAndName(final Location location, final String name,
	        final boolean includeRetired, PagingInfo pagingInfo) {
		if (location == null) {
			throw new IllegalArgumentException("Location cannot be null");
		}
		if (StringUtils.isEmpty(name)) {
			throw new IllegalArgumentException("Name cannot be null or empty");
		}
		if (name.length() > MAX_CASHPOINT_NAME_CHARACTERS) {
			throw new IllegalArgumentException(
			        "Name cannot be longer than " + MAX_CASHPOINT_NAME_CHARACTERS + " characters");
		}
		
		CashPointSearch cashPointSearch = CashPointSearch.builder().locationUuid(location.getUuid()).name(name)
		        .includeRetired(includeRetired).build();
		return cashPointDAO.getCashPoints(cashPointSearch, pagingInfo);
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	@Transactional(readOnly = true)
	public List<CashPoint> getAllCashPoints(boolean includeRetired) {
		CashPointSearch cashPointSearch = CashPointSearch.builder().includeRetired(includeRetired).build();
		return cashPointDAO.getCashPoints(cashPointSearch, null);
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	@Transactional
	public CashPoint saveCashPoint(CashPoint cashPoint) {
		if (cashPoint == null) {
			throw new IllegalArgumentException("Cash point cannot be null");
		}
		return cashPointDAO.saveCashPoint(cashPoint);
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	@Transactional
	public void purgeCashPoint(CashPoint cashPoint) {
		if (cashPoint == null) {
			throw new IllegalArgumentException("Cash point cannot be null");
		}
		cashPointDAO.purgeCashPoint(cashPoint);
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	@Transactional
	public CashPoint retireCashPoint(CashPoint cashPoint, String retireReason) {
		if (StringUtils.isEmpty(retireReason)) {
			throw new IllegalArgumentException("Retire reason cannot be null or empty");
		}
		return cashPointDAO.saveCashPoint(cashPoint);
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	@Transactional
	public CashPoint unretireCashPoint(CashPoint cashPoint) {
		return cashPointDAO.saveCashPoint(cashPoint);
	}
}
