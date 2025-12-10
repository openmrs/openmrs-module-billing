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

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.BillLineItemService;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.stockmanagement.api.StockManagementService;
import org.openmrs.module.stockmanagement.api.model.StockItem;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BillLineItemServiceImplTest extends BaseModuleContextSensitiveTest {
	
	private BillLineItemService billLineItemService;
	
	private BillService billService;
	
	private StockManagementService stockManagementService;
	
	@BeforeEach
	public void setup() {
		billLineItemService = Context.getService(BillLineItemService.class);
		billService = Context.getService(BillService.class);
		stockManagementService = Context.getService(StockManagementService.class);
		
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "StockOperationType.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "PaymentModeTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillTest.xml");
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#getBillLineItem(Integer)
	 */
	@Test
	public void getBillLineItem_shouldReturnBillLineItemById() {
		BillLineItem billLineItem = billLineItemService.getBillLineItem(0);
		assertNotNull(billLineItem);
		assertEquals(0, billLineItem.getId());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#getBillLineItem(Integer)
	 */
	@Test
	public void getBillLineItem_shouldReturnNullIfIdIsNull() {
		BillLineItem billLineItem = billLineItemService.getBillLineItem(null);
		assertNull(billLineItem);
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#getBillLineItem(Integer)
	 */
	@Test
	public void getBillLineItem_shouldReturnNullIfBillLineItemNotFound() {
		BillLineItem billLineItem = billLineItemService.getBillLineItem(999);
		assertNull(billLineItem);
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#getBillLineItemByUuid(String)
	 */
	@Test
	public void getBillLineItemByUuid_shouldReturnBillLineItemByUuid() {
		BillLineItem billLineItem = billLineItemService.getBillLineItem(0);
		assertNotNull(billLineItem);
		String uuid = billLineItem.getUuid();
		
		BillLineItem foundBillLineItem = billLineItemService.getBillLineItemByUuid(uuid);
		assertNotNull(foundBillLineItem);
		assertEquals(uuid, foundBillLineItem.getUuid());
		assertEquals(0, foundBillLineItem.getId());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#getBillLineItemByUuid(String)
	 */
	@Test
	public void getBillLineItemByUuid_shouldReturnNullIfUuidIsNull() {
		BillLineItem billLineItem = billLineItemService.getBillLineItemByUuid(null);
		assertNull(billLineItem);
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#getBillLineItemByUuid(String)
	 */
	@Test
	public void getBillLineItemByUuid_shouldReturnNullIfUuidNotFound() {
		BillLineItem billLineItem = billLineItemService.getBillLineItemByUuid("nonexistent-uuid");
		assertNull(billLineItem);
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#saveBillLineItem(BillLineItem)
	 */
	@Test
	public void saveBillLineItem_shouldThrowNullPointerExceptionIfBillLineItemIsNull() {
		assertThrows(NullPointerException.class, () -> billLineItemService.saveBillLineItem(null));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#saveBillLineItem(BillLineItem)
	 */
	@Test
	public void saveBillLineItem_shouldCreateNewBillLineItem() {
		Bill bill = billService.getBill(0);
		assertNotNull(bill);
		
		StockItem stockItem = stockManagementService.getStockItemByUuid("5631b434-78aa-102b-91a0-001e378eb67f");
		assertNotNull(stockItem);
		
		BillLineItem newBillLineItem = new BillLineItem();
		newBillLineItem.setBill(bill);
		newBillLineItem.setItem(stockItem);
		newBillLineItem.setPrice(new BigDecimal("99.99"));
		newBillLineItem.setPriceName("default");
		newBillLineItem.setQuantity(5);
		newBillLineItem.setLineItemOrder(10);
		newBillLineItem.setPaymentStatus(BillStatus.PENDING);
		
		BillLineItem savedBillLineItem = billLineItemService.saveBillLineItem(newBillLineItem);
		
		assertNotNull(savedBillLineItem);
		assertNotNull(savedBillLineItem.getId());
		assertEquals(new BigDecimal("99.99"), savedBillLineItem.getPrice());
		assertEquals(5, savedBillLineItem.getQuantity());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#saveBillLineItem(BillLineItem)
	 */
	@Test
	public void saveBillLineItem_shouldUpdateExistingBillLineItem() {
		BillLineItem existingBillLineItem = billLineItemService.getBillLineItem(0);
		assertNotNull(existingBillLineItem);
		assertEquals(new BigDecimal("101.01"), existingBillLineItem.getPrice());
		
		existingBillLineItem.setPrice(new BigDecimal("150.00"));
		existingBillLineItem.setQuantity(10);
		
		billLineItemService.saveBillLineItem(existingBillLineItem);
		
		BillLineItem updatedBillLineItem = billLineItemService.getBillLineItem(0);
		assertEquals(new BigDecimal("150.00"), updatedBillLineItem.getPrice());
		assertEquals(10, updatedBillLineItem.getQuantity());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#voidBillLineItem(BillLineItem,
	 *      String)
	 */
	@Test
	public void voidBillLineItem_shouldThrowNullPointerExceptionIfVoidReasonIsNull() {
		BillLineItem billLineItem = billLineItemService.getBillLineItem(0);
		assertNotNull(billLineItem);
		
		assertThrows(NullPointerException.class, () -> billLineItemService.voidBillLineItem(billLineItem, null));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#voidBillLineItem(BillLineItem,
	 *      String)
	 */
	@Test
	public void voidBillLineItem_shouldVoidBillLineItem() {
		BillLineItem billLineItem = billLineItemService.getBillLineItem(0);
		assertNotNull(billLineItem);
		assertFalse(billLineItem.getVoided());
		
		BillLineItem voidedBillLineItem = billLineItemService.voidBillLineItem(billLineItem, "Test void reason");
		
		assertNotNull(voidedBillLineItem);
		assertEquals(billLineItem.getId(), voidedBillLineItem.getId());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#unvoidBillLineItem(BillLineItem)
	 */
	@Test
	public void unvoidBillLineItem_shouldUnvoidBillLineItem() {
		BillLineItem billLineItem = billLineItemService.getBillLineItem(0);
		assertNotNull(billLineItem);
		
		BillLineItem voidedBillLineItem = billLineItemService.voidBillLineItem(billLineItem, "Test void reason");
		
		BillLineItem unvoidedBillLineItem = billLineItemService.unvoidBillLineItem(voidedBillLineItem);
		
		assertNotNull(unvoidedBillLineItem);
		assertEquals(billLineItem.getId(), unvoidedBillLineItem.getId());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#purgeBillLineItem(BillLineItem)
	 */
	@Test
	public void purgeBillLineItem_shouldDeleteBillLineItem() {
		Bill bill = billService.getBill(0);
		assertNotNull(bill);
		
		StockItem stockItem = stockManagementService.getStockItemByUuid("5631b434-78aa-102b-91a0-001e378eb67f");
		assertNotNull(stockItem);
		
		BillLineItem newBillLineItem = new BillLineItem();
		newBillLineItem.setBill(bill);
		newBillLineItem.setItem(stockItem);
		newBillLineItem.setPrice(new BigDecimal("25.00"));
		newBillLineItem.setPriceName("default");
		newBillLineItem.setQuantity(1);
		newBillLineItem.setLineItemOrder(99);
		newBillLineItem.setPaymentStatus(BillStatus.PENDING);
		
		BillLineItem savedBillLineItem = billLineItemService.saveBillLineItem(newBillLineItem);
		
		Integer billLineItemId = savedBillLineItem.getId();
		assertNotNull(billLineItemId);
		
		billLineItemService.purgeBillLineItem(savedBillLineItem);
		
		BillLineItem deletedBillLineItem = billLineItemService.getBillLineItem(billLineItemId);
		assertNull(deletedBillLineItem);
	}
}
