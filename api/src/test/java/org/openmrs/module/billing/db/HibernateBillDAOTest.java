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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.CashPointService;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.db.BillDAO;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.DiscountStatus;
import org.openmrs.module.billing.api.search.BillSearch;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

public class HibernateBillDAOTest extends BaseModuleContextSensitiveTest {
	
	private BillDAO billDAO;
	
	private PatientService patientService;
	
	private ProviderService providerService;
	
	private CashPointService cashPointService;
	
	@BeforeEach
	public void setup() {
		billDAO = Context.getRegisteredComponent("billDAO", BillDAO.class);
		patientService = Context.getPatientService();
		providerService = Context.getProviderService();
		cashPointService = Context.getService(CashPointService.class);
		
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "StockOperationType.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "PaymentModeTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillTest.xml");
	}
	
	@Test
	public void getBill_shouldReturnBillById() {
		Bill bill = billDAO.getBill(0);
		assertNotNull(bill);
		assertEquals(0, bill.getId());
	}
	
	@Test
	public void getBill_shouldReturnNullIfBillNotFound() {
		Bill bill = billDAO.getBill(999);
		assertNull(bill);
	}
	
	@Test
	public void getBillByUuid_shouldReturnBillByUuid() {
		Bill bill = billDAO.getBill(0);
		assertNotNull(bill);
		String uuid = bill.getUuid();
		
		Bill foundBill = billDAO.getBillByUuid(uuid);
		assertNotNull(foundBill);
		assertEquals(uuid, foundBill.getUuid());
		assertEquals(0, foundBill.getId());
	}
	
	@Test
	public void getBillByUuid_shouldReturnNullIfUuidNotFound() {
		Bill bill = billDAO.getBillByUuid("nonexistent-uuid");
		assertNull(bill);
	}
	
	@Test
	public void saveBill_shouldCreateNewBill() {
		Patient patient = patientService.getPatient(1);
		assertNotNull(patient);
		
		Bill newBill = new Bill();
		newBill.setCashier(providerService.getProvider(0));
		newBill.setPatient(patient);
		newBill.setCashPoint(cashPointService.getCashPoint(0));
		newBill.setReceiptNumber("TEST-" + UUID.randomUUID());
		newBill.setStatus(BillStatus.PENDING);
		
		Bill savedBill = billDAO.saveBill(newBill);
		Context.flushSession();
		
		assertNotNull(savedBill);
		assertNotNull(savedBill.getId());
		assertEquals(BillStatus.PENDING, savedBill.getStatus());
	}
	
	@Test
	public void saveBill_shouldUpdateExistingBill() {
		Bill existingBill = billDAO.getBill(2);
		assertNotNull(existingBill);
		assertEquals(BillStatus.PENDING, existingBill.getStatus());
		
		String newReceiptNumber = "UPDATED-" + UUID.randomUUID();
		existingBill.setReceiptNumber(newReceiptNumber);
		
		billDAO.saveBill(existingBill);
		Context.flushSession();
		Context.clearSession();
		
		Bill updatedBill = billDAO.getBill(2);
		assertEquals(newReceiptNumber, updatedBill.getReceiptNumber());
	}
	
	@Test
	public void getBillByReceiptNumber_shouldReturnBillWithMatchingReceiptNumber() {
		Bill bill = billDAO.getBillByReceiptNumber("test 1 receipt number");
		assertNotNull(bill);
		assertEquals("test 1 receipt number", bill.getReceiptNumber());
	}
	
	@Test
	public void getBillByReceiptNumber_shouldReturnNullIfReceiptNumberNotFound() {
		Bill bill = billDAO.getBillByReceiptNumber("nonexistent receipt number");
		assertNull(bill);
	}
	
	@Test
	public void getBillsByPatientId_shouldReturnBillsForPatient() {
		List<Bill> bills = billDAO.getBillsByPatientUuid("5631b434-78aa-102b-91a0-001e378eb67e", null);
		assertNotNull(bills);
		assertFalse(bills.isEmpty());
		assertEquals(1, bills.size());
	}
	
	@Test
	public void getBillsByPatientId_shouldReturnEmptyListWhenPatientHasNoBills() {
		List<Bill> bills = billDAO.getBillsByPatientUuid("abc", null);
		assertNotNull(bills);
		assertTrue(bills.isEmpty());
	}
	
	@Test
	public void getBillsByPatientId_shouldApplyPagingCorrectly() {
		PagingInfo pagingInfo = new PagingInfo(1, 5);
		List<Bill> bills = billDAO.getBillsByPatientUuid("5631b434-78aa-102b-91a0-001e378eb67e", pagingInfo);
		
		assertNotNull(bills);
		assertTrue(bills.size() <= 5);
	}
	
	@Test
	public void getBills_shouldReturnAllBillsWhenSearchIsEmpty() {
		BillSearch billSearch = new BillSearch();
		List<Bill> bills = billDAO.getBills(billSearch, null);
		
		assertNotNull(bills);
		assertFalse(bills.isEmpty());
	}
	
	@Test
	public void getBills_shouldFilterByPatientUuid() {
		Patient patient = patientService.getPatient(0);
		assertNotNull(patient);
		
		BillSearch billSearch = new BillSearch();
		billSearch.setPatientUuid(patient.getUuid());
		
		List<Bill> bills = billDAO.getBills(billSearch, null);
		assertNotNull(bills);
		assertFalse(bills.isEmpty());
		
		for (Bill bill : bills) {
			assertEquals(patient.getUuid(), bill.getPatient().getUuid());
		}
	}
	
	@Test
	public void getBills_shouldFilterByCashPointUuid() {
		Bill existingBill = billDAO.getBill(0);
		assertNotNull(existingBill);
		assertNotNull(existingBill.getCashPoint());
		
		BillSearch billSearch = new BillSearch();
		billSearch.setCashPointUuid(existingBill.getCashPoint().getUuid());
		
		List<Bill> bills = billDAO.getBills(billSearch, null);
		assertNotNull(bills);
		assertFalse(bills.isEmpty());
	}
	
	@Test
	public void getBills_shouldExcludeVoidedBillsByDefault() {
		BillSearch billSearch = new BillSearch();
		billSearch.setIncludeVoided(false);
		
		List<Bill> bills = billDAO.getBills(billSearch, null);
		assertNotNull(bills);
		
		for (Bill bill : bills) {
			assertFalse(bill.getVoided());
		}
	}
	
	@Test
	public void purgeBill_shouldDeleteBill() {
		Patient patient = patientService.getPatient(1);
		assertNotNull(patient);
		
		Bill newBill = new Bill();
		newBill.setCashier(providerService.getProvider(0));
		newBill.setPatient(patient);
		newBill.setCashPoint(cashPointService.getCashPoint(0));
		newBill.setReceiptNumber("TO-DELETE-" + UUID.randomUUID());
		newBill.setStatus(BillStatus.PENDING);
		
		Bill savedBill = billDAO.saveBill(newBill);
		Context.flushSession();
		
		Integer billId = savedBill.getId();
		assertNotNull(billId);
		
		billDAO.purgeBill(savedBill);
		Context.flushSession();
		Context.clearSession();
		
		Bill deletedBill = billDAO.getBill(billId);
		assertNull(deletedBill);
	}
	
	@Test
	public void getBills_shouldReturnBillsOrderedByDateCreatedDescending() {
		BillSearch billSearch = new BillSearch();
		List<Bill> bills = billDAO.getBills(billSearch, null);
		
		assertNotNull(bills);
		assertTrue(bills.size() >= 2, "Expected at least 2 bills in test dataset for ordering assertion");
		
		for (int i = 0; i < bills.size() - 1; i++) {
			Date current = bills.get(i).getDateCreated();
			Date next = bills.get(i + 1).getDateCreated();
			assertNotNull(current);
			assertNotNull(next);
			assertFalse(current.before(next), "Bill at index " + i + " (dateCreated=" + current
			        + ") should be >= bill at index " + (i + 1) + " (dateCreated=" + next + ")");
		}
	}
	
	@Test
	public void getBillsByPatientUuid_shouldReturnBillsOrderedByDateCreatedDescending() {
		// Save two bills for the same patient with explicit dateCreated values to assert ordering
		Patient patient = patientService.getPatient(0);
		assertNotNull(patient);
		
		Bill olderBill = new Bill();
		olderBill.setCashier(providerService.getProvider(0));
		olderBill.setPatient(patient);
		olderBill.setCashPoint(cashPointService.getCashPoint(0));
		olderBill.setReceiptNumber("OLDER-" + UUID.randomUUID());
		olderBill.setStatus(BillStatus.PENDING);
		olderBill.setDateCreated(new Date(System.currentTimeMillis() - 86400000));
		billDAO.saveBill(olderBill);
		
		Bill newerBill = new Bill();
		newerBill.setCashier(providerService.getProvider(0));
		newerBill.setPatient(patient);
		newerBill.setCashPoint(cashPointService.getCashPoint(0));
		newerBill.setReceiptNumber("NEWER-" + UUID.randomUUID());
		newerBill.setStatus(BillStatus.PENDING);
		newerBill.setDateCreated(new Date());
		billDAO.saveBill(newerBill);
		
		List<Bill> bills = billDAO.getBillsByPatientUuid(patient.getUuid(), null);
		
		assertNotNull(bills);
		assertTrue(bills.size() >= 2, "Expected at least 2 bills for ordering assertion");
		
		for (int i = 0; i < bills.size() - 1; i++) {
			Date current = bills.get(i).getDateCreated();
			Date next = bills.get(i + 1).getDateCreated();
			assertNotNull(current);
			assertNotNull(next);
			assertFalse(current.before(next), "Bill at index " + i + " (dateCreated=" + current
			        + ") should be >= bill at index " + (i + 1) + " (dateCreated=" + next + ")");
		}
	}
	
	@Test
	public void getBills_shouldReturnAllBillsWhenDiscountStatusesIsNull() {
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillDiscountStatusFilterTest.xml");
		BillSearch search = new BillSearch();
		List<Bill> results = billDAO.getBills(search, null);
		
		assertTrue(results.size() >= 5, "Expected at least 5 bills but got " + results.size());
	}
	
	@Test
	public void getBills_shouldMatchBillsWithPendingDiscount() {
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillDiscountStatusFilterTest.xml");
		BillSearch search = BillSearch.builder().discountStatuses(Arrays.asList(DiscountStatus.PENDING)).build();
		List<Bill> results = billDAO.getBills(search, null);
		List<String> resultUuids = uuids(results);
		
		assertTrue(resultUuids.contains("b1000000-0000-0000-0000-000000000001"), "Expected bill 1001 (PENDING discount)");
		assertTrue(resultUuids.contains("b4000000-0000-0000-0000-000000000004"),
		    "Expected bill 1004 (PENDING + APPROVED discounts)");
		assertFalse(resultUuids.contains("b5000000-0000-0000-0000-000000000005"),
		    "Bill 1005 has only a voided PENDING discount — must be excluded");
	}
	
	@Test
	public void getBills_shouldMatchBillsWithApprovedDiscount() {
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillDiscountStatusFilterTest.xml");
		BillSearch search = BillSearch.builder().discountStatuses(Arrays.asList(DiscountStatus.APPROVED)).build();
		List<Bill> results = billDAO.getBills(search, null);
		List<String> resultUuids = uuids(results);
		
		assertTrue(resultUuids.contains("b2000000-0000-0000-0000-000000000002"), "Expected bill 1002 (APPROVED discount)");
		assertTrue(resultUuids.contains("b4000000-0000-0000-0000-000000000004"), "Expected bill 1004 (APPROVED discount)");
		assertFalse(resultUuids.contains("b1000000-0000-0000-0000-000000000001"),
		    "Bill 1001 has only a PENDING discount — must not appear for APPROVED filter");
	}
	
	@Test
	public void getBills_shouldMatchUnionOfMultipleDiscountStatuses() {
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillDiscountStatusFilterTest.xml");
		BillSearch search = BillSearch.builder()
		        .discountStatuses(Arrays.asList(DiscountStatus.APPROVED, DiscountStatus.REJECTED)).build();
		List<Bill> results = billDAO.getBills(search, null);
		List<String> resultUuids = uuids(results);
		
		assertTrue(resultUuids.contains("b2000000-0000-0000-0000-000000000002"), "Expected bill 1002 (APPROVED)");
		assertTrue(resultUuids.contains("b3000000-0000-0000-0000-000000000003"), "Expected bill 1003 (REJECTED)");
		assertTrue(resultUuids.contains("b4000000-0000-0000-0000-000000000004"), "Expected bill 1004 (APPROVED + PENDING)");
		assertFalse(resultUuids.contains("b1000000-0000-0000-0000-000000000001"),
		    "Bill 1001 has only a PENDING discount — must not appear for APPROVED|REJECTED filter");
	}
	
	@Test
	public void getBills_shouldExcludeVoidedDiscounts() {
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillDiscountStatusFilterTest.xml");
		BillSearch search = BillSearch.builder().discountStatuses(Arrays.asList(DiscountStatus.PENDING)).build();
		List<Bill> results = billDAO.getBills(search, null);
		List<String> resultUuids = uuids(results);
		
		assertFalse(resultUuids.contains("b5000000-0000-0000-0000-000000000005"),
		    "Bill 1005 has only a voided PENDING discount and must not appear in results");
	}
	
	@Test
	public void getBills_shouldNotDuplicateBillsWithMultipleMatchingDiscounts() {
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillDiscountStatusFilterTest.xml");
		BillSearch search = BillSearch.builder()
		        .discountStatuses(Arrays.asList(DiscountStatus.PENDING, DiscountStatus.APPROVED)).build();
		List<Bill> results = billDAO.getBills(search, null);
		List<String> resultUuids = uuids(results);
		
		long countOf1004 = results.stream().filter(b -> "b4000000-0000-0000-0000-000000000004".equals(b.getUuid())).count();
		
		assertEquals(1, countOf1004, "Bill 1004 matches both PENDING and APPROVED discounts but must appear exactly once");
		assertFalse(resultUuids.contains("b3000000-0000-0000-0000-000000000003"),
		    "Bill 1003 has only a REJECTED discount — must not appear for PENDING|APPROVED filter");
	}
	
	private List<String> uuids(List<Bill> bills) {
		return bills.stream().map(Bill::getUuid).sorted().collect(Collectors.toList());
	}
}
