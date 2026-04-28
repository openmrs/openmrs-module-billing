/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.db.CashPointDAO;
import org.openmrs.module.billing.api.db.hibernate.HibernateCashPointDAOImpl;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.api.search.CashPointSearch;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

/**
 * Tests for {@link HibernateCashPointDAOImpl}.
 */
public class HibernateCashPointDAOTest extends BaseModuleContextSensitiveTest {
	
	private CashPointDAO cashPointDAO;
	
	@BeforeEach
	public void setup() {
		cashPointDAO = Context.getRegisteredComponent("cashPointDAO", CashPointDAO.class);
		
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
	}
	
	/**
	 * @see HibernateCashPointDAOImpl#getCashPoint(Integer)
	 */
	@Test
	public void getCashPoint_shouldReturnCashPointById() {
		CashPoint cashPoint = cashPointDAO.getCashPoint(0);
		assertNotNull(cashPoint);
		assertEquals(0, cashPoint.getId());
		assertEquals("Test 1 Cash Point", cashPoint.getName());
	}
	
	/**
	 * @see HibernateCashPointDAOImpl#getCashPoint(Integer)
	 */
	@Test
	public void getCashPoint_shouldReturnNullIfCashPointNotFound() {
		CashPoint cashPoint = cashPointDAO.getCashPoint(999);
		assertNull(cashPoint);
	}
	
	/**
	 * @see HibernateCashPointDAOImpl#getCashPointByUuid(String)
	 */
	@Test
	public void getCashPointByUuid_shouldReturnCashPointByUuid() {
		CashPoint cashPoint = cashPointDAO.getCashPointByUuid("4028814B39BB04B90139BB04B98B0000");
		assertNotNull(cashPoint);
		assertEquals("4028814B39BB04B90139BB04B98B0000", cashPoint.getUuid());
		assertEquals(0, cashPoint.getId());
	}
	
	/**
	 * @see HibernateCashPointDAOImpl#getCashPointByUuid(String)
	 */
	@Test
	public void getCashPointByUuid_shouldReturnNullIfUuidNotFound() {
		CashPoint cashPoint = cashPointDAO.getCashPointByUuid("nonexistent-uuid");
		assertNull(cashPoint);
	}
	
	/**
	 * @see HibernateCashPointDAOImpl#saveCashPoint(CashPoint)
	 */
	@Test
	public void saveCashPoint_shouldCreateNewCashPoint() {
		Location location = Context.getLocationService().getLocation(0);
		
		CashPoint newCashPoint = new CashPoint();
		newCashPoint.setName("New Test Cash Point");
		newCashPoint.setDescription("New test description");
		newCashPoint.setLocation(location);
		newCashPoint.setUuid(UUID.randomUUID().toString());
		
		CashPoint savedCashPoint = cashPointDAO.saveCashPoint(newCashPoint);
		
		assertNotNull(savedCashPoint);
		assertNotNull(savedCashPoint.getId());
		assertEquals("New Test Cash Point", savedCashPoint.getName());
		assertEquals("New test description", savedCashPoint.getDescription());
		assertEquals(location.getId(), savedCashPoint.getLocation().getId());
	}
	
	/**
	 * @see HibernateCashPointDAOImpl#saveCashPoint(CashPoint)
	 */
	@Test
	public void saveCashPoint_shouldUpdateExistingCashPoint() {
		CashPoint existingCashPoint = cashPointDAO.getCashPoint(0);
		assertNotNull(existingCashPoint);
		
		String newName = "Updated Cash Point Name";
		existingCashPoint.setName(newName);
		
		CashPoint updatedCashPoint = cashPointDAO.saveCashPoint(existingCashPoint);
		assertEquals(newName, updatedCashPoint.getName());
	}
	
	/**
	 * @see HibernateCashPointDAOImpl#purgeCashPoint(CashPoint)
	 */
	@Test
	public void purgeCashPoint_shouldDeleteCashPoint() {
		Location location = Context.getLocationService().getLocation(0);
		
		CashPoint newCashPoint = new CashPoint();
		newCashPoint.setName("Cash Point To Delete");
		newCashPoint.setDescription("To be deleted");
		newCashPoint.setLocation(location);
		newCashPoint.setUuid(UUID.randomUUID().toString());
		
		CashPoint savedCashPoint = cashPointDAO.saveCashPoint(newCashPoint);
		
		Integer cashPointId = savedCashPoint.getId();
		assertNotNull(cashPointId);
		
		cashPointDAO.purgeCashPoint(savedCashPoint);
		
		CashPoint deletedCashPoint = cashPointDAO.getCashPoint(cashPointId);
		assertNull(deletedCashPoint);
	}
	
	/**
	 * @see HibernateCashPointDAOImpl#getCashPoints(CashPointSearch,
	 *      org.openmrs.module.billing.api.base.PagingInfo)
	 */
	@Test
	public void getCashPoints_shouldReturnAllNonRetiredCashPoints() {
		CashPointSearch search = CashPointSearch.builder().includeRetired(false).build();
		
		List<CashPoint> cashPoints = cashPointDAO.getCashPoints(search, null);
		
		assertNotNull(cashPoints);
		assertEquals(7, cashPoints.size());
		for (CashPoint cashPoint : cashPoints) {
			assertFalse(cashPoint.getRetired());
		}
	}
	
	/**
	 * @see HibernateCashPointDAOImpl#getCashPoints(CashPointSearch,
	 *      org.openmrs.module.billing.api.base.PagingInfo)
	 */
	@Test
	public void getCashPoints_shouldReturnCashPointsByLocationUuid() {
		Location location = Context.getLocationService().getLocation(0);
		CashPointSearch search = CashPointSearch.builder().locationUuid(location.getUuid()).includeRetired(false).build();
		
		List<CashPoint> cashPoints = cashPointDAO.getCashPoints(search, null);
		
		assertNotNull(cashPoints);
		assertEquals(3, cashPoints.size());
		for (CashPoint cashPoint : cashPoints) {
			assertEquals(location.getId(), cashPoint.getLocation().getId());
		}
	}
	
	/**
	 * @see HibernateCashPointDAOImpl#getCashPoints(CashPointSearch,
	 *      org.openmrs.module.billing.api.base.PagingInfo)
	 */
	@Test
	public void getCashPoints_shouldReturnCashPointsByName() {
		CashPointSearch search = CashPointSearch.builder().name("Test 1").includeRetired(false).build();
		
		List<CashPoint> cashPoints = cashPointDAO.getCashPoints(search, null);
		
		assertNotNull(cashPoints);
		assertEquals(1, cashPoints.size());
		assertTrue(cashPoints.get(0).getName().contains("Test 1"));
	}
	
	/**
	 * @see HibernateCashPointDAOImpl#getCashPoints(CashPointSearch,
	 *      org.openmrs.module.billing.api.base.PagingInfo)
	 */
	@Test
	public void getCashPoints_shouldReturnCashPointsByNameCaseInsensitive() {
		CashPointSearch search = CashPointSearch.builder().name("test").includeRetired(false).build();
		
		List<CashPoint> cashPoints = cashPointDAO.getCashPoints(search, null);
		
		assertNotNull(cashPoints);
		assertEquals(7, cashPoints.size());
	}
	
	/**
	 * @see HibernateCashPointDAOImpl#getCashPoints(CashPointSearch,
	 *      org.openmrs.module.billing.api.base.PagingInfo)
	 */
	@Test
	public void getCashPoints_shouldReturnCashPointsByLocationAndName() {
		Location location = Context.getLocationService().getLocation(0);
		CashPointSearch search = CashPointSearch.builder().locationUuid(location.getUuid()).name("Test 1")
		        .includeRetired(false).build();
		
		List<CashPoint> cashPoints = cashPointDAO.getCashPoints(search, null);
		
		assertNotNull(cashPoints);
		assertEquals(1, cashPoints.size());
		assertEquals("Test 1 Cash Point", cashPoints.get(0).getName());
		assertEquals(location.getId(), cashPoints.get(0).getLocation().getId());
	}
	
	/**
	 * @see HibernateCashPointDAOImpl#getCashPoints(CashPointSearch,
	 *      org.openmrs.module.billing.api.base.PagingInfo)
	 */
	@Test
	public void getCashPoints_shouldReturnEmptyListWhenNoMatch() {
		CashPointSearch search = CashPointSearch.builder().name("Nonexistent").includeRetired(false).build();
		
		List<CashPoint> cashPoints = cashPointDAO.getCashPoints(search, null);
		
		assertNotNull(cashPoints);
		assertTrue(cashPoints.isEmpty());
	}
	
	/**
	 * @see HibernateCashPointDAOImpl#getCashPoints(CashPointSearch,
	 *      org.openmrs.module.billing.api.base.PagingInfo)
	 */
	@Test
	public void getCashPoints_shouldIncludeRetiredCashPointsWhenSpecified() {
		CashPoint cashPoint = cashPointDAO.getCashPoint(0);
		cashPoint.setRetired(true);
		cashPoint.setRetireReason("Test reason");
		cashPointDAO.saveCashPoint(cashPoint);
		
		CashPointSearch searchWithoutRetired = CashPointSearch.builder().includeRetired(false).build();
		List<CashPoint> cashPointsWithoutRetired = cashPointDAO.getCashPoints(searchWithoutRetired, null);
		
		CashPointSearch searchWithRetired = CashPointSearch.builder().includeRetired(true).build();
		List<CashPoint> cashPointsWithRetired = cashPointDAO.getCashPoints(searchWithRetired, null);
		
		assertEquals(6, cashPointsWithoutRetired.size());
		assertEquals(7, cashPointsWithRetired.size());
	}
}
