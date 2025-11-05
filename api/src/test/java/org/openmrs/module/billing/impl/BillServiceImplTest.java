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

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.IBillService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

public class BillServiceImplTest extends BaseModuleContextSensitiveTest {
	
	private IBillService billService;
	
	@BeforeEach
	public void setup() {
		billService = Context.getService(IBillService.class);
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "StockOperationType.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "PaymentModeTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillTest.xml");
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#save(Bill)
	 */
	@Test
	public void save_shouldThrowNullPointerExceptionIfBillIsNull() {
		assertThrows(NullPointerException.class, () -> billService.save(null));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillByReceiptNumber(String)
	 */
	@Test
	public void getBillByReceiptNumber_shouldThrowIllegalArgumentExceptionIfReceiptNumberIsNull() {
		assertThrows(IllegalArgumentException.class, () -> billService.getBillByReceiptNumber(null));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillByReceiptNumber(String)
	 */
	@Test
	public void getBillByReceiptNumber_shouldThrowIllegalArgumentExceptionIfReceiptNumberIsEmpty() {
		assertThrows(IllegalArgumentException.class, () -> billService.getBillByReceiptNumber(""));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillByReceiptNumber(String)
	 */
	@Test
	public void getBillByReceiptNumber_shouldThrowIllegalArgumentExceptionIfReceiptNumberIsTooLong() {
		String longReceiptNumber = RandomStringUtils.randomAlphanumeric(1999);
		assertThrows(IllegalArgumentException.class, () -> billService.getBillByReceiptNumber(longReceiptNumber));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillsByPatient(Patient,
	 *      org.openmrs.module.billing.api.base.PagingInfo)
	 */
	@Test
	public void getBillsByPatient_shouldThrowNullPointerExceptionIfPatientIsNull() {
		assertThrows(NullPointerException.class, () -> billService.getBillsByPatient(null, null));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillsByPatientId(int,
	 *      org.openmrs.module.billing.api.base.PagingInfo)
	 */
	@Test
	public void getBillsByPatientId_shouldThrowIllegalArgumentExceptionIfPatientIdIsNegative() {
		assertThrows(IllegalArgumentException.class, () -> billService.getBillsByPatientId(-1, null));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getAll()
	 */
	@Test
	public void getAll_shouldReturnAllBills() {
		List<Bill> bills = billService.getAll();
		assertNotNull(bills);
		for (Bill bill : bills) {
			if (bill.getLineItems() != null) {
				for (Object item : bill.getLineItems()) {
					assertNotNull(item, "Line items should not contain null values");
				}
			}
		}
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillByReceiptNumber(String)
	 */
	@Test
	public void getBillByReceiptNumber_shouldReturnBillWithSpecifiedReceiptNumber() {
		Bill bill = billService.getBillByReceiptNumber("test 1 receipt number");
		assertNotNull(bill);
		assertEquals("test 1 receipt number", bill.getReceiptNumber());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillByReceiptNumber(String)
	 */
	@Test
	public void getBillByReceiptNumber_shouldReturnNullIfReceiptNumberNotFound() {
		Bill bill = billService.getBillByReceiptNumber("nonexistent receipt number");
		assertNull(bill);
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillsByPatientId(int,
	 *      org.openmrs.module.billing.api.base.PagingInfo)
	 */
	@Test
	public void getBillsByPatientId_shouldReturnBillsForPatient() {
		List<Bill> bills = billService.getBillsByPatientId(0, null);
		assertNotNull(bills);
		assertFalse(bills.isEmpty());
		assertEquals(1, bills.size());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getBillsByPatientId(int,
	 *      org.openmrs.module.billing.api.base.PagingInfo)
	 */
	@Test
	public void getBillsByPatientId_shouldReturnEmptyListWhenPatientHasNoBills() {
		List<Bill> bills = billService.getBillsByPatientId(999, null);
		assertNotNull(bills);
		assertEquals(0, bills.size());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getById(int)
	 */
	@Test
	public void getById_shouldReturnBillWithSpecifiedId() {
		Bill bill = billService.getById(1);
		assertNotNull(bill);
		assertEquals(1, bill.getId());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#getById(int)
	 */
	@Test
	public void getById_shouldRemoveNullLineItems() {
		Bill bill = billService.getById(1);
		assertNotNull(bill);
		if (bill.getLineItems() != null) {
			for (Object item : bill.getLineItems()) {
				assertNotNull(item, "Line items should not contain null values");
			}
		}
	}
}
