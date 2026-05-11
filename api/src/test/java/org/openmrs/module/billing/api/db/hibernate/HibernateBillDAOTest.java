/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.db.hibernate;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.db.BillDAO;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.DiscountStatus;
import org.openmrs.module.billing.api.search.BillSearch;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HibernateBillDAOTest extends BaseModuleContextSensitiveTest {
	
	private static final String UUID_BILL_1001 = "b1000000-0000-0000-0000-000000000001";
	
	private static final String UUID_BILL_1002 = "b2000000-0000-0000-0000-000000000002";
	
	private static final String UUID_BILL_1003 = "b3000000-0000-0000-0000-000000000003";
	
	private static final String UUID_BILL_1004 = "b4000000-0000-0000-0000-000000000004";
	
	private static final String UUID_BILL_1005 = "b5000000-0000-0000-0000-000000000005";
	
	private BillDAO dao;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@BeforeEach
	public void setup() throws Exception {
		dao = new HibernateBillDAO(sessionFactory);
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "StockOperationType.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "PaymentModeTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillDiscountStatusFilterTest.xml");
	}
	
	// -------------------------------------------------------------------------
	// Test 6: sanity — should PASS even before the filter is wired
	// -------------------------------------------------------------------------
	
	@Test
	public void getBills_shouldReturnAllBillsWhenDiscountStatusesIsNull() {
		BillSearch search = new BillSearch();
		List<Bill> results = dao.getBills(search, null);
		
		// At least 5 bills come from the filter dataset (1001–1005) plus any from BillTest.xml
		assertTrue(results.size() >= 5, "Expected at least 5 bills but got " + results.size());
	}
	
	// -------------------------------------------------------------------------
	// Tests 1–5: should FAIL until the discount-status filter is implemented
	// -------------------------------------------------------------------------
	
	@Test
	public void getBills_shouldMatchBillsWithPendingDiscount() {
		BillSearch search = BillSearch.builder().discountStatuses(Arrays.asList(DiscountStatus.PENDING)).build();
		List<Bill> results = dao.getBills(search, null);
		List<String> resultUuids = uuids(results);
		
		// bill 1001 has a non-voided PENDING discount
		assertTrue(resultUuids.contains(UUID_BILL_1001), "Expected bill 1001 (PENDING discount)");
		// bill 1004 has a non-voided PENDING discount (plus an APPROVED one)
		assertTrue(resultUuids.contains(UUID_BILL_1004), "Expected bill 1004 (PENDING + APPROVED discounts)");
		// bill 1005 has a PENDING discount but it is voided — must not appear
		assertFalse(resultUuids.contains(UUID_BILL_1005), "Bill 1005 has only a voided PENDING discount — must be excluded");
	}
	
	@Test
	public void getBills_shouldMatchBillsWithApprovedDiscount() {
		BillSearch search = BillSearch.builder().discountStatuses(Arrays.asList(DiscountStatus.APPROVED)).build();
		List<Bill> results = dao.getBills(search, null);
		List<String> resultUuids = uuids(results);
		
		assertTrue(resultUuids.contains(UUID_BILL_1002), "Expected bill 1002 (APPROVED discount)");
		assertTrue(resultUuids.contains(UUID_BILL_1004), "Expected bill 1004 (APPROVED discount)");
		// bill 1001 has only a PENDING discount — must not appear when filtering by APPROVED
		assertFalse(resultUuids.contains(UUID_BILL_1001),
		    "Bill 1001 has only a PENDING discount — must not appear for APPROVED filter");
	}
	
	@Test
	public void getBills_shouldMatchUnionOfMultipleStatuses() {
		BillSearch search = BillSearch.builder()
		        .discountStatuses(Arrays.asList(DiscountStatus.APPROVED, DiscountStatus.REJECTED)).build();
		List<Bill> results = dao.getBills(search, null);
		List<String> resultUuids = uuids(results);
		
		assertTrue(resultUuids.contains(UUID_BILL_1002), "Expected bill 1002 (APPROVED)");
		assertTrue(resultUuids.contains(UUID_BILL_1003), "Expected bill 1003 (REJECTED)");
		assertTrue(resultUuids.contains(UUID_BILL_1004), "Expected bill 1004 (APPROVED + PENDING)");
		// bill 1001 has only a PENDING discount — must not appear in APPROVED|REJECTED union
		assertFalse(resultUuids.contains(UUID_BILL_1001),
		    "Bill 1001 has only a PENDING discount — must not appear for APPROVED|REJECTED filter");
	}
	
	@Test
	public void getBills_shouldExcludeVoidedDiscounts() {
		BillSearch search = BillSearch.builder().discountStatuses(Arrays.asList(DiscountStatus.PENDING)).build();
		List<Bill> results = dao.getBills(search, null);
		List<String> resultUuids = uuids(results);
		
		assertFalse(resultUuids.contains(UUID_BILL_1005),
		    "Bill 1005 has only a voided PENDING discount and must not appear in results");
	}
	
	@Test
	public void getBills_shouldNotDuplicateBillsWithMultipleMatchingDiscounts() {
		BillSearch search = BillSearch.builder()
		        .discountStatuses(Arrays.asList(DiscountStatus.PENDING, DiscountStatus.APPROVED)).build();
		List<Bill> results = dao.getBills(search, null);
		List<String> resultUuids = uuids(results);
		
		long countOf1004 = results.stream().filter(b -> UUID_BILL_1004.equals(b.getUuid())).count();
		
		assertEquals(1, countOf1004, "Bill 1004 matches both PENDING and APPROVED discounts but must appear exactly once");
		// bill 1003 has only a REJECTED discount — must not appear in PENDING|APPROVED results
		assertFalse(resultUuids.contains(UUID_BILL_1003),
		    "Bill 1003 has only a REJECTED discount — must not appear for PENDING|APPROVED filter");
	}
	
	// -------------------------------------------------------------------------
	// Helper
	// -------------------------------------------------------------------------
	
	private List<String> uuids(List<Bill> bills) {
		return bills.stream().map(Bill::getUuid).sorted().collect(Collectors.toList());
	}
}
