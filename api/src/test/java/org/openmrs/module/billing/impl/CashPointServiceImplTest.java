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

package org.openmrs.module.billing.impl;

import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.CashPointService;
import org.openmrs.module.billing.api.impl.CashPointServiceImpl;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link CashPointServiceImpl}.
 */
public class CashPointServiceImplTest extends BaseModuleContextSensitiveTest {
	
	private CashPointService cashPointService;
	
	private LocationService locationService;
	
	@BeforeEach
	public void setup() {
		cashPointService = Context.getService(CashPointService.class);
		locationService = Context.getLocationService();
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
	}
	
	/**
	 * @see CashPointServiceImpl#getCashPointsByLocation(Location, boolean)
	 */
	@Test
	public void getCashPointsByLocation_shouldThrowIllegalArgumentExceptionIfLocationIsNull() {
		assertThrows(IllegalArgumentException.class, () -> cashPointService.getCashPointsByLocation(null, false));
	}
	
	/**
	 * @see CashPointServiceImpl#getCashPointsByLocation(Location, boolean)
	 */
	@Test
	public void getCashPointsByLocation_shouldReturnCashPointsForLocation() {
		Location location = locationService.getLocation(0);
		assertNotNull(location);
		List<CashPoint> cashPoints = cashPointService.getCashPointsByLocation(location, false);
		assertNotNull(cashPoints);
		assertFalse(cashPoints.isEmpty());
		for (CashPoint cashPoint : cashPoints) {
			assertEquals(location.getId(), cashPoint.getLocation().getId());
		}
	}
	
	/**
	 * @see CashPointServiceImpl#getCashPointsByLocation(Location, boolean)
	 */
	@Test
	public void getCashPointsByLocation_shouldReturnEmptyListWhenLocationHasNoCashPoints() {
		Location location = locationService.getLocation(999);
		assertNotNull(location);
		List<CashPoint> cashPoints = cashPointService.getCashPointsByLocation(location, false);
		assertNotNull(cashPoints);
		assertTrue(cashPoints.isEmpty());
	}
	
	/**
	 * @see CashPointServiceImpl#getCashPointsByLocationAndName(Location, String, boolean)
	 */
	@Test
	public void getCashPointsByLocationAndName_shouldThrowIllegalArgumentExceptionIfLocationIsNull() {
		assertThrows(IllegalArgumentException.class,
		    () -> cashPointService.getCashPointsByLocationAndName(null, "Test", false));
	}
	
	/**
	 * @see CashPointServiceImpl#getCashPointsByLocationAndName(Location, String, boolean)
	 */
	@Test
	public void getCashPointsByLocationAndName_shouldThrowIllegalArgumentExceptionIfNameIsNull() {
		Location location = locationService.getLocation(0);
		assertThrows(IllegalArgumentException.class,
		    () -> cashPointService.getCashPointsByLocationAndName(location, null, false));
	}
	
	/**
	 * @see CashPointServiceImpl#getCashPointsByLocationAndName(Location, String, boolean)
	 */
	@Test
	public void getCashPointsByLocationAndName_shouldThrowIllegalArgumentExceptionIfNameIsEmpty() {
		Location location = locationService.getLocation(0);
		assertThrows(IllegalArgumentException.class,
		    () -> cashPointService.getCashPointsByLocationAndName(location, "", false));
	}
	
	/**
	 * @see CashPointServiceImpl#getCashPointsByLocationAndName(Location, String, boolean)
	 */
	@Test
	public void getCashPointsByLocationAndName_shouldThrowIllegalArgumentExceptionIfNameIsTooLong() {
		Location location = locationService.getLocation(0);
		String longName = RandomStringUtils.randomAlphanumeric(256);
		assertThrows(IllegalArgumentException.class,
		    () -> cashPointService.getCashPointsByLocationAndName(location, longName, false));
	}
	
	/**
	 * @see CashPointServiceImpl#getCashPointsByLocationAndName(Location, String, boolean)
	 */
	@Test
	public void getCashPointsByLocationAndName_shouldReturnCashPointsMatchingLocationAndName() {
		Location location = locationService.getLocation(0);
		List<CashPoint> cashPoints = cashPointService.getCashPointsByLocationAndName(location, "Test", false);
		assertNotNull(cashPoints);
		assertFalse(cashPoints.isEmpty());
		for (CashPoint cashPoint : cashPoints) {
			assertEquals(location.getId(), cashPoint.getLocation().getId());
			assertTrue(cashPoint.getName().startsWith("Test"));
		}
	}
	
	/**
	 * @see CashPointServiceImpl#getCashPointsByLocationAndName(Location, String, boolean)
	 */
	@Test
	public void getCashPointsByLocationAndName_shouldReturnEmptyListWhenNoMatch() {
		Location location = locationService.getLocation(0);
		List<CashPoint> cashPoints = cashPointService.getCashPointsByLocationAndName(location, "Fake name", false);
		assertNotNull(cashPoints);
		assertTrue(cashPoints.isEmpty());
	}
	
	/**
	 * @see CashPointServiceImpl#getCashPoint(Integer)
	 */
	@Test
	public void getCashPoint_shouldReturnCashPointWithSpecifiedId() {
		CashPoint cashPoint = cashPointService.getCashPoint(0);
		assertNotNull(cashPoint);
		assertEquals(0, cashPoint.getId());
	}
	
	/**
	 * @see CashPointServiceImpl#getCashPointByUuid(String)
	 */
	@Test
	public void getCashPointByUuid_shouldReturnCashPointWithSpecifiedUuid() {
		CashPoint cashPoint = cashPointService.getCashPointByUuid("4028814B39BB04B90139BB04B98B0000");
		assertNotNull(cashPoint);
		assertEquals("4028814B39BB04B90139BB04B98B0000", cashPoint.getUuid());
	}
	
	/**
	 * @see CashPointServiceImpl#getAllCashPoints(boolean)
	 */
	@Test
	public void getAllCashPoints_shouldReturnAllCashPoints() {
		List<CashPoint> cashPoints = cashPointService.getAllCashPoints(false);
		assertNotNull(cashPoints);
		assertFalse(cashPoints.isEmpty());
		assertEquals(7, cashPoints.size());
	}
}
