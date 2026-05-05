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
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.db.BillDiscountDAO;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillDiscount;
import org.openmrs.module.billing.api.model.DiscountType;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BillDiscountDAOImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String DISCOUNT_UUID_1 = "d1000000-0000-0000-0000-000000000001";
	
	private BillDiscountDAO dao;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@BeforeEach
	public void setup() throws Exception {
		dao = new HibernateBillDiscountDAO(sessionFactory);
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "StockOperationType.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "PaymentModeTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillDiscountTest.xml");
	}
	
	@Test
	public void getBillDiscountById_shouldReturnDiscountWithSpecifiedId() {
		BillDiscount discount = dao.getBillDiscountById(1);
		
		assertNotNull(discount);
		assertEquals(1, discount.getBillDiscountId());
		assertEquals(DiscountType.PERCENTAGE, discount.getDiscountType());
		assertEquals(0, new BigDecimal("10.00").compareTo(discount.getDiscountValue()));
		assertEquals("Loyal patient", discount.getJustification());
		assertFalse(discount.getVoided());
	}
	
	@Test
	public void getBillDiscountById_shouldReturnNullForInvalidId() {
		assertNull(dao.getBillDiscountById(999));
	}
	
	@Test
	public void getBillDiscountByUuid_shouldReturnMatchingDiscount() {
		BillDiscount discount = dao.getBillDiscountByUuid(DISCOUNT_UUID_1);
		
		assertNotNull(discount);
		assertEquals(DISCOUNT_UUID_1, discount.getUuid());
		assertEquals(1, discount.getBillDiscountId());
	}
	
	@Test
	public void getBillDiscountByBillId_shouldReturnActiveDiscountForBill() {
		BillDiscount discount = dao.getBillDiscountByBillId(0);
		
		assertNotNull(discount);
		assertEquals(1, discount.getBillDiscountId());
		assertFalse(discount.getVoided());
	}
	
	@Test
	public void getBillDiscountByBillId_shouldIgnoreVoidedDiscounts() {
		// Bill 2 has only a voided discount in the dataset; lookup must not return it.
		assertNull(dao.getBillDiscountByBillId(2));
	}
	
	@Test
	public void getBillDiscountByBillId_shouldIgnoreLineScopedDiscount() {
		// Bill 100 only has a line-item-scoped discount; the bill-level lookup must skip it.
		assertNull(dao.getBillDiscountByBillId(100));
	}
	
	@Test
	public void getActiveLineItemDiscount_shouldReturnDiscountForLineItem() {
		BillDiscount discount = dao.getActiveLineItemDiscount(100);
		
		assertNotNull(discount);
		assertEquals(3, discount.getBillDiscountId());
		assertNotNull(discount.getLineItem());
		assertEquals(100, discount.getLineItem().getId());
		assertFalse(discount.getVoided());
	}
	
	@Test
	public void getActiveLineItemDiscount_shouldReturnNullWhenNone() {
		// Line item 102 exists but has no discount.
		assertNull(dao.getActiveLineItemDiscount(102));
	}

	@Test
	public void getDiscountsByBillId_shouldReturnVoidedHistory() {
		// Bill 2 holds only a voided discount in the dataset; the audit query must surface it.
		List<BillDiscount> history = dao.getDiscountsByBillId(2);

		assertNotNull(history);
		assertEquals(1, history.size());
		assertTrue(history.get(0).getVoided());
	}
	
	@Test
	public void saveBillDiscount_shouldPersistNewDiscount() {
		Bill bill = Context.getService(BillService.class).getBillByUuid("6028814B39B565A20139B95D74360004");
		assertNotNull(bill);
		
		BillDiscount discount = new BillDiscount();
		discount.setBill(bill);
		discount.setDiscountType(DiscountType.FIXED_AMOUNT);
		discount.setDiscountValue(new BigDecimal("25.00"));
		discount.setJustification("Financial hardship");
		discount.setInitiator(Context.getAuthenticatedUser());
		discount.setCreator(Context.getAuthenticatedUser());
		discount.setDateCreated(new Date());
		
		BillDiscount saved = dao.saveBillDiscount(discount);
		
		assertNotNull(saved);
		assertNotNull(saved.getBillDiscountId());
		assertEquals(bill.getId(), saved.getBill().getId());
		assertEquals(DiscountType.FIXED_AMOUNT, saved.getDiscountType());
	}
	
	@Test
	public void saveBillDiscount_shouldUpdateExistingDiscount() {
		BillDiscount discount = dao.getBillDiscountById(1);
		assertNotNull(discount);
		
		discount.setJustification("Updated justification");
		BillDiscount updated = dao.saveBillDiscount(discount);
		
		assertNotNull(updated);
		assertEquals("Updated justification", updated.getJustification());
	}
}
