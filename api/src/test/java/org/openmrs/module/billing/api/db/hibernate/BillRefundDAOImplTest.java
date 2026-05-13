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
import org.openmrs.module.billing.api.BillLineItemService;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.db.BillRefundDAO;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillRefund;
import org.openmrs.module.billing.api.model.RefundStatus;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BillRefundDAOImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String REFUND_UUID_1 = "r1000000-0000-0000-0000-000000000001";
	
	private static final String LINE_REFUND_UUID = "r1000000-0000-0000-0000-000000000003";
	
	private static final String LINE_ITEM_WITH_REFUND_UUID = "r0000000-0000-0000-0000-000000000330";
	
	private static final String BILL_WITH_BILL_REFUND_UUID = "r0000000-0000-0000-0000-000000000302";
	
	private static final String BILL_WITH_LINE_REFUND_UUID = "r0000000-0000-0000-0000-000000000303";
	
	private static final String VOIDED_HISTORY_BILL_UUID = "r0000000-0000-0000-0000-000000000304";
	
	private BillRefundDAO dao;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@BeforeEach
	public void setup() throws Exception {
		dao = new HibernateBillRefundDAO(sessionFactory);
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "StockOperationType.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "PaymentModeTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillRefundTest.xml");
	}
	
	@Test
	public void getBillRefundById_shouldReturnRefundWithSpecifiedId() {
		BillRefund refund = dao.getBillRefundById(1);
		
		assertNotNull(refund);
		assertEquals(1, refund.getBillRefundId());
		assertEquals(0, new BigDecimal("50.00").compareTo(refund.getRefundAmount()));
		assertEquals("Service not delivered", refund.getReason());
		assertFalse(refund.getVoided());
	}
	
	@Test
	public void getBillRefundById_shouldReturnNullForInvalidId() {
		assertNull(dao.getBillRefundById(999));
	}
	
	@Test
	public void getBillRefundByUuid_shouldReturnMatchingRefund() {
		BillRefund refund = dao.getBillRefundByUuid(REFUND_UUID_1);
		
		assertNotNull(refund);
		assertEquals(REFUND_UUID_1, refund.getUuid());
	}
	
	@Test
	public void getActiveBillRefund_shouldReturnActiveBillLevelRefund() {
		Bill bill = Context.getService(BillService.class).getBillByUuid(BILL_WITH_BILL_REFUND_UUID);
		assertNotNull(bill);
		
		BillRefund refund = dao.getActiveBillRefund(bill.getId());
		
		assertNotNull(refund);
		assertEquals(REFUND_UUID_1, refund.getUuid());
	}
	
	@Test
	public void getActiveBillRefund_shouldIgnoreVoided() {
		Bill bill = Context.getService(BillService.class).getBillByUuid(VOIDED_HISTORY_BILL_UUID);
		assertNull(dao.getActiveBillRefund(bill.getId()));
	}
	
	@Test
	public void getActiveBillRefund_shouldTreatApprovedAsActiveAndCompletedRejectedAsInactive() {
		BillRefund refund = dao.getBillRefundByUuid(REFUND_UUID_1);
		Bill bill = refund.getBill();
		
		refund.setStatus(RefundStatus.APPROVED);
		dao.saveBillRefund(refund);
		assertNotNull(dao.getActiveBillRefund(bill.getId()), "APPROVED bill-level refund should remain active");
		
		refund.setStatus(RefundStatus.COMPLETED);
		dao.saveBillRefund(refund);
		assertNull(dao.getActiveBillRefund(bill.getId()), "COMPLETED bill-level refund frees the scope");
		
		refund.setStatus(RefundStatus.REJECTED);
		dao.saveBillRefund(refund);
		assertNull(dao.getActiveBillRefund(bill.getId()), "REJECTED bill-level refund frees the scope");
	}
	
	@Test
	public void getActiveLineScopedRefunds_shouldReturnOnlyActiveLineScoped() {
		Bill bill = Context.getService(BillService.class).getBillByUuid(BILL_WITH_LINE_REFUND_UUID);
		List<BillRefund> active = dao.getActiveLineScopedRefunds(bill.getId());
		assertEquals(1, active.size());
		assertEquals(LINE_REFUND_UUID, active.get(0).getUuid());
	}
	
	@Test
	public void getActiveLineItemRefund_shouldReturnActiveLineScopedRefund() {
		BillLineItem lineItem = Context.getService(BillLineItemService.class)
		        .getBillLineItemByUuid(LINE_ITEM_WITH_REFUND_UUID);
		assertNotNull(lineItem);
		
		BillRefund refund = dao.getActiveLineItemRefund(lineItem.getId());
		
		assertNotNull(refund);
		assertEquals(LINE_REFUND_UUID, refund.getUuid());
	}
	
	@Test
	public void getRefundsByBillId_shouldReturnAllInclVoided() {
		Bill bill = Context.getService(BillService.class).getBillByUuid(VOIDED_HISTORY_BILL_UUID);
		assertNotNull(bill);
		
		List<BillRefund> all = dao.getRefundsByBillId(bill.getId());
		
		assertNotNull(all);
		assertEquals(1, all.size());
	}
	
	@Test
	public void getStatusById_shouldReturnPersistedStatus() {
		assertEquals(RefundStatus.REQUESTED, dao.getStatusById(1));
	}
}
