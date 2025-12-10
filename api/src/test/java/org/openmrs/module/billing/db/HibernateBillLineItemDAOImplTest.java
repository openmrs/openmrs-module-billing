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

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.db.BillDAO;
import org.openmrs.module.billing.api.db.BillLineItemDAO;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.stockmanagement.api.StockManagementService;
import org.openmrs.module.stockmanagement.api.model.StockItem;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class HibernateBillLineItemDAOImplTest extends BaseModuleContextSensitiveTest {
	
	private BillLineItemDAO billLineItemDAO;
	
	private BillDAO billDAO;
	
	private StockManagementService stockManagementService;
	
	@BeforeEach
	public void setup() {
		billLineItemDAO = Context.getRegisteredComponent("billLineItemDAO", BillLineItemDAO.class);
		billDAO = Context.getRegisteredComponent("billDAO", BillDAO.class);
		stockManagementService = Context.getService(StockManagementService.class);
		
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "StockOperationType.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "PaymentModeTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillTest.xml");
	}
	
	@Test
	public void getBillLineItem_shouldReturnBillLineItemById() {
		BillLineItem billLineItem = billLineItemDAO.getBillLineItem(0);
		assertNotNull(billLineItem);
		assertEquals(0, billLineItem.getId());
	}
	
	@Test
	public void getBillLineItem_shouldReturnNullIfBillLineItemNotFound() {
		BillLineItem billLineItem = billLineItemDAO.getBillLineItem(999);
		assertNull(billLineItem);
	}
	
	@Test
	public void getBillLineItemByUuid_shouldReturnBillLineItemByUuid() {
		BillLineItem billLineItem = billLineItemDAO.getBillLineItem(0);
		assertNotNull(billLineItem);
		String uuid = billLineItem.getUuid();
		
		BillLineItem foundBillLineItem = billLineItemDAO.getBillLineItemByUuid(uuid);
		assertNotNull(foundBillLineItem);
		assertEquals(uuid, foundBillLineItem.getUuid());
		assertEquals(0, foundBillLineItem.getId());
	}
	
	@Test
	public void saveBillLineItem_shouldCreateNewBillLineItem() {
		Bill bill = billDAO.getBill(0);
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
		
		BillLineItem savedBillLineItem = billLineItemDAO.saveBillLineItem(newBillLineItem);
		
		assertNotNull(savedBillLineItem);
		assertNotNull(savedBillLineItem.getId());
		assertEquals(new BigDecimal("99.99"), savedBillLineItem.getPrice());
		assertEquals(5, savedBillLineItem.getQuantity());
	}
	
	@Test
	public void saveBillLineItem_shouldUpdateExistingBillLineItem() {
		BillLineItem existingBillLineItem = billLineItemDAO.getBillLineItem(0);
		assertNotNull(existingBillLineItem);
		assertEquals(new BigDecimal("101.01"), existingBillLineItem.getPrice());
		
		existingBillLineItem.setPrice(new BigDecimal("150.00"));
		existingBillLineItem.setQuantity(10);
		
		billLineItemDAO.saveBillLineItem(existingBillLineItem);
		
		BillLineItem updatedBillLineItem = billLineItemDAO.getBillLineItem(0);
		assertEquals(new BigDecimal("150.00"), updatedBillLineItem.getPrice());
		assertEquals(10, updatedBillLineItem.getQuantity());
	}
	
	@Test
	public void purgeBillLineItem_shouldDeleteBillLineItem() {
		Bill bill = billDAO.getBill(0);
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
		
		BillLineItem savedBillLineItem = billLineItemDAO.saveBillLineItem(newBillLineItem);
		
		Integer billLineItemId = savedBillLineItem.getId();
		assertNotNull(billLineItemId);
		
		billLineItemDAO.purgeBillLineItem(savedBillLineItem);
		
		BillLineItem deletedBillLineItem = billLineItemDAO.getBillLineItem(billLineItemId);
		assertNull(deletedBillLineItem);
	}
}
