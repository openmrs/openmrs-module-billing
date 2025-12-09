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
package org.openmrs.module.billing.db;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.ICashPointService;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.db.BillDAO;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.search.BillSearch;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

public class HibernateBillDAOImplTest extends BaseModuleContextSensitiveTest {
	
	private BillDAO billDAO;
	
	private PatientService patientService;
	
	private ProviderService providerService;
	
	private ICashPointService cashPointService;
	
	@BeforeEach
	public void setup() {
		billDAO = Context.getRegisteredComponent("billDAO", BillDAO.class);
		patientService = Context.getPatientService();
		providerService = Context.getProviderService();
		cashPointService = Context.getService(ICashPointService.class);
		
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
		newBill.setCashPoint(cashPointService.getById(0));
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
		newBill.setCashPoint(cashPointService.getById(0));
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
}
