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
package org.openmrs.module.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.openmrs.module.billing.base.entity.IMetadataDataServiceTest.assertOpenmrsMetadata;

import java.util.List;

import com.google.common.collect.Iterators;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.CashPointService;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

/**
 * Integration tests for {@link CashPointService}.
 */
public class ICashPointServiceTest extends BaseModuleContextSensitiveTest {
	
	public static final String CASH_POINT_DATASET = TestConstants.BASE_DATASET_DIR + "CashPointTest.xml";
	
	private CashPointService service;
	
	@BeforeEach
	public void before() {
		service = Context.getService(CashPointService.class);
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(CASH_POINT_DATASET);
	}
	
	/**
	 * Asserts that two cash points are equal.
	 *
	 * @param expected the expected cash point
	 * @param actual the actual cash point
	 */
	public static void assertCashPoint(CashPoint expected, CashPoint actual) {
		assertOpenmrsMetadata(expected, actual);
		
		assertEquals(expected.getName(), actual.getName());
		assertEquals(expected.getDescription(), actual.getDescription());
		if (expected.getLocation() == null) {
			assertNull(actual.getLocation());
		} else {
			assertEquals(expected.getLocation().getId(), actual.getLocation().getId());
		}
	}
	
	/**
	 * Asserts that two cash point entities are equal.
	 *
	 * @param expected the expected cash point
	 * @param actual the actual cash point
	 */
	protected void assertEntity(CashPoint expected, CashPoint actual) {
		assertCashPoint(expected, actual);
	}
	
	/**
	 * @see CashPointService#getCashPointsByLocation(Location, boolean)
	 */
	@Test
	public void getCashPointsByLocation_shouldReturnCashPointsForLocationWithCashPoints() {
		Location location = Context.getLocationService().getLocation(1);
		
		List<CashPoint> results = service.getCashPointsByLocation(location, false);
		assertNotNull(results);
		assertEquals(1, results.size());
	}
	
	/**
	 * @see CashPointService#getCashPointsByLocation(Location, boolean)
	 */
	@Test
	public void getCashPointsByLocation_shouldNotReturnRetiredCashPointsUnlessSpecified() {
		CashPoint cashPoint = service.getCashPoint(0);
		cashPoint.setRetired(true);
		cashPoint.setRetireReason("reason");
		service.saveCashPoint(cashPoint);
		Location location = Context.getLocationService().getLocation(0);
		
		Context.flushSession();
		
		List<CashPoint> cashPoints = service.getCashPointsByLocation(location, false);
		assertNotNull(cashPoints);
		assertEquals(2, cashPoints.size());
		assertEquals(4, (int) Iterators.get(cashPoints.iterator(), 0).getId());
		assertEquals(5, (int) Iterators.get(cashPoints.iterator(), 1).getId());
		
		List<CashPoint> cashPoints1 = service.getCashPointsByLocation(location, true);
		assertNotNull(cashPoints1);
		assertEquals(3, cashPoints1.size());
		assertEquals(0, (int) Iterators.get(cashPoints1.iterator(), 0).getId());
	}
	
	/**
	 * @see CashPointService#getCashPointsByLocation(Location, boolean)
	 */
	@Test
	public void getCashPointsByLocation_shouldReturnAllCashPointsForTheSpecifiedLocation() {
		List<CashPoint> cashPoint = service.getCashPointsByLocation(Context.getLocationService().getLocation(0), false);
		assertNotNull(cashPoint);
		assertEquals(3, cashPoint.size());
		
		assertEquals(0, (int) Iterators.get(cashPoint.iterator(), 0).getId());
		assertEquals(4, (int) Iterators.get(cashPoint.iterator(), 1).getId());
		assertEquals(5, (int) Iterators.get(cashPoint.iterator(), 2).getId());
	}
	
	/**
	 * @see CashPointService#getCashPointsByLocationAndName(Location, String, boolean)
	 */
	@Test
	public void getCashPointsByLocationAndName_shouldThrowIllegalArgumentExceptionIfNameIsNull() {
		assertThrows(IllegalArgumentException.class,
		    () -> service.getCashPointsByLocationAndName(Context.getLocationService().getLocation(1), null, false));
	}
	
	/**
	 * @see CashPointService#getCashPointsByLocationAndName(Location, String, boolean)
	 */
	@Test
	public void getCashPointsByLocationAndName_shouldThrowIllegalArgumentExceptionIfNameIsEmpty() {
		assertThrows(IllegalArgumentException.class,
		    () -> service.getCashPointsByLocationAndName(Context.getLocationService().getLocation(1), "", false));
	}
	
	/**
	 * @see CashPointService#getCashPointsByLocationAndName(Location, String, boolean)
	 */
	public void getCashPointsByLocationAndName_shouldThrowIllegalArgumentExceptionIfNameExceeds255Characters() {
		assertThrows(IllegalArgumentException.class,
		    () -> service.getCashPointsByLocationAndName(Context.getLocationService().getLocation(1),
		        StringUtils.repeat("A", 256), false));
	}
	
	/**
	 * @see CashPointService#getCashPointsByLocationAndName(Location, String, boolean)
	 */
	@Test
	public void getCashPointsByLocationAndName_shouldReturnCashPointsMatchingLocationAndName() {
		Location location = Context.getLocationService().getLocation(1);
		
		List<CashPoint> results = service.getCashPointsByLocationAndName(location, "Test", false);
		assertNotNull(results);
		assertEquals(1, results.size());
	}
	
	/**
	 * @see CashPointService#getCashPointsByLocationAndName(Location, String, boolean)
	 */
	@Test
	public void getCashPointsByLocationAndName_shouldNotReturnRetiredCashPointsUnlessSpecified() {
		CashPoint cashPoint = service.getCashPoint(0);
		cashPoint.setRetired(true);
		cashPoint.setRetireReason("reason");
		service.saveCashPoint(cashPoint);
		Location location = Context.getLocationService().getLocation(0);
		
		Context.flushSession();
		
		List<CashPoint> results = service.getCashPointsByLocationAndName(location, "Test", false);
		assertNotNull(results);
		assertEquals(2, results.size());
		assertEquals(4, (int) Iterators.get(results.iterator(), 0).getId());
		assertEquals(5, (int) Iterators.get(results.iterator(), 1).getId());
		
		List<CashPoint> results1 = service.getCashPointsByLocationAndName(location, "Test", true);
		assertNotNull(results1);
		assertEquals(3, results1.size());
		assertEquals(0, (int) Iterators.get(results1.iterator(), 0).getId());
	}
	
	/**
	 * @see CashPointService#getCashPointsByLocationAndName(Location, String, boolean)
	 */
	@Test
	public void getCashPointsByLocationAndName_shouldReturnCashPointsContainingTheSpecifiedName() {
		List<CashPoint> results = service.getCashPointsByLocationAndName(Context.getLocationService().getLocation(0),
		    "Test 1", false);
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(0, (int) Iterators.get(results.iterator(), 0).getId());
		
		CashPoint cashPoint = service.getCashPoint(0);
		assertEntity(cashPoint, results.get(0));
		
		List<CashPoint> results1 = service.getCashPointsByLocationAndName(Context.getLocationService().getLocation(2),
		    "Test", false);
		assertNotNull(results1);
		assertEquals(2, results1.size());
		assertEquals(2, (int) Iterators.get(results1.iterator(), 0).getId());
		assertEquals(6, (int) Iterators.get(results1.iterator(), 1).getId());
	}
	
	/**
	 * @see CashPointService#getCashPointsByLocationAndName(Location, String, boolean)
	 */
	@Test
	public void getCashPointsByLocationAndName_shouldReturnCashPointsForOnlyTheSpecifiedLocation() {
		List<CashPoint> cashPoint = service.getCashPointsByLocationAndName(Context.getLocationService().getLocation(0),
		    "Test", false);
		assertNotNull(cashPoint);
		assertEquals(3, cashPoint.size());
		
		assertEquals(0, (int) Iterators.get(cashPoint.iterator(), 0).getId());
		assertEquals(4, (int) Iterators.get(cashPoint.iterator(), 1).getId());
		assertEquals(5, (int) Iterators.get(cashPoint.iterator(), 2).getId());
	}
	
	/**
	 * @see CashPointService#getCashPointsByLocation(Location, boolean)
	 */
	@Test
	public void getCashPointsByLocation_shouldThrowIllegalArgumentExceptionIfLocationIsNull() {
		assertThrows(IllegalArgumentException.class, () -> service.getCashPointsByLocation(null, false));
	}
	
	/**
	 * @see CashPointService#getCashPointsByLocationAndName(Location, String, boolean)
	 */
	@Test
	public void getCashPointsByLocationAndName_shouldThrowIllegalArgumentExceptionIfLocationIsNull() {
		assertThrows(IllegalArgumentException.class, () -> service.getCashPointsByLocationAndName(null, "Test", false));
	}
}
