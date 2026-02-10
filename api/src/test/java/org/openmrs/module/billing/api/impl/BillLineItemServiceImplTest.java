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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.BillLineItemService;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BillLineItemServiceImplTest extends BaseModuleContextSensitiveTest {
	
	private BillLineItemService billLineItemService;
	
	private BillService billService;
	
	@BeforeEach
	public void setup() {
		billLineItemService = Context.getService(BillLineItemService.class);
		billService = Context.getService(BillService.class);
		
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "StockOperationType.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "PaymentModeTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillTest.xml");
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#getPersistedLineItemIds(Integer)
	 */
	@Test
	public void getPersistedLineItemIds_shouldReturnListOfIdsForValidBillId() {
		// Bill 0 has 3 line items (IDs: 0, 1, 2)
		List<Integer> lineItemIds = billLineItemService.getPersistedLineItemIds(0);
		
		assertNotNull(lineItemIds);
		assertEquals(3, lineItemIds.size());
		assertTrue(lineItemIds.contains(0));
		assertTrue(lineItemIds.contains(1));
		assertTrue(lineItemIds.contains(2));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#getPersistedLineItemIds(Integer)
	 */
	@Test
	public void getPersistedLineItemIds_shouldReturnEmptyListForNullBillId() {
		List<Integer> lineItemIds = billLineItemService.getPersistedLineItemIds(null);
		
		assertNotNull(lineItemIds);
		assertTrue(lineItemIds.isEmpty());
		assertEquals(Collections.emptyList(), lineItemIds);
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#getPersistedLineItemIds(Integer)
	 */
	@Test
	public void getPersistedLineItemIds_shouldReturnEmptyListForNonExistentBillId() {
		List<Integer> lineItemIds = billLineItemService.getPersistedLineItemIds(999);
		
		assertNotNull(lineItemIds);
		assertTrue(lineItemIds.isEmpty());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#getPersistedLineItemIds(Integer)
	 */
	@Test
	public void getPersistedLineItemIds_shouldReturnCorrectIdsForBillWithSingleLineItem() {
		// Bill 1 has 1 line item (ID: 3)
		List<Integer> lineItemIds = billLineItemService.getPersistedLineItemIds(1);
		
		assertNotNull(lineItemIds);
		assertEquals(1, lineItemIds.size());
		assertTrue(lineItemIds.contains(3));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#getBillLineItemByUuid(String)
	 */
	@Test
	public void getBillLineItemByUuid_shouldReturnBillLineItemWithSpecifiedUuid() {
		Bill bill = billService.getBill(0);
		assertNotNull(bill);
		assertNotNull(bill.getLineItems());
		assertTrue(bill.getLineItems().size() > 0);
		
		BillLineItem expectedLineItem = bill.getLineItems().get(0);
		String uuid = expectedLineItem.getUuid();
		
		BillLineItem foundLineItem = billLineItemService.getBillLineItemByUuid(uuid);
		
		assertNotNull(foundLineItem);
		assertEquals(uuid, foundLineItem.getUuid());
		assertEquals(expectedLineItem.getId(), foundLineItem.getId());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#getBillLineItemByUuid(String)
	 */
	@Test
	public void getBillLineItemByUuid_shouldReturnNullIfUuidNotFound() {
		BillLineItem lineItem = billLineItemService.getBillLineItemByUuid("nonexistent-uuid");
		
		assertNull(lineItem);
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#getBillLineItemByUuid(String)
	 */
	@Test
	public void getBillLineItemByUuid_shouldReturnNullIfUuidIsNull() {
		BillLineItem lineItem = billLineItemService.getBillLineItemByUuid(null);
		
		assertNull(lineItem);
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#getBillLineItemByUuid(String)
	 */
	@Test
	public void getBillLineItemByUuid_shouldReturnNullIfUuidIsEmpty() {
		BillLineItem lineItem = billLineItemService.getBillLineItemByUuid("");
		
		assertNull(lineItem);
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#getBillLineItemByUuid(String)
	 */
	@Test
	public void getBillLineItemByUuid_shouldReturnNullIfUuidIsBlank() {
		BillLineItem lineItem = billLineItemService.getBillLineItemByUuid("   ");
		
		assertNull(lineItem);
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#voidBillLineItem(BillLineItem,
	 *      String)
	 */
	@Test
	public void voidBillLineItem_shouldVoidLineItemWithValidVoidReason() {
		Bill bill = billService.getBill(2);
		assertNotNull(bill);
		assertNotNull(bill.getLineItems());
		assertTrue(bill.getLineItems().size() > 0);
		
		BillLineItem lineItem = bill.getLineItems().get(0);
		String voidReason = "Test void reason";
		
		BillLineItem voidedLineItem = billLineItemService.voidBillLineItem(lineItem, voidReason);
		
		assertNotNull(voidedLineItem);
		assertEquals(lineItem.getId(), voidedLineItem.getId());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#voidBillLineItem(BillLineItem,
	 *      String)
	 */
	@Test
	public void voidBillLineItem_shouldThrowIllegalArgumentExceptionIfVoidReasonIsNull() {
		Bill bill = billService.getBill(2);
		assertNotNull(bill);
		assertNotNull(bill.getLineItems());
		assertTrue(bill.getLineItems().size() > 0);
		
		BillLineItem lineItem = bill.getLineItems().get(0);
		
		assertThrows(IllegalArgumentException.class, () -> billLineItemService.voidBillLineItem(lineItem, null));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#voidBillLineItem(BillLineItem,
	 *      String)
	 */
	@Test
	public void voidBillLineItem_shouldThrowIllegalArgumentExceptionIfVoidReasonIsEmpty() {
		Bill bill = billService.getBill(2);
		assertNotNull(bill);
		assertNotNull(bill.getLineItems());
		assertTrue(bill.getLineItems().size() > 0);
		
		BillLineItem lineItem = bill.getLineItems().get(0);
		
		assertThrows(IllegalArgumentException.class, () -> billLineItemService.voidBillLineItem(lineItem, ""));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#voidBillLineItem(BillLineItem,
	 *      String)
	 */
	@Test
	public void voidBillLineItem_shouldThrowIllegalArgumentExceptionIfVoidReasonIsBlank() {
		Bill bill = billService.getBill(2);
		assertNotNull(bill);
		assertNotNull(bill.getLineItems());
		assertTrue(bill.getLineItems().size() > 0);
		
		BillLineItem lineItem = bill.getLineItems().get(0);
		
		assertThrows(IllegalArgumentException.class, () -> billLineItemService.voidBillLineItem(lineItem, "   "));
	}
}
