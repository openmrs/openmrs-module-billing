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

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.BillLineItemService;
import org.openmrs.module.billing.api.IBillService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.stockmanagement.api.StockManagementService;
import org.openmrs.module.stockmanagement.api.model.StockItem;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

public class BillLineItemServiceImplTest extends BaseModuleContextSensitiveTest {
	
	private BillLineItemService billLineItemService;
	
	private StockManagementService stockService;
	
	private IBillService billService;
	
	@BeforeEach
	public void setup() {
		billLineItemService = Context.getService(BillLineItemService.class);
		stockService = Context.getService(StockManagementService.class);
		billService = Context.getService(IBillService.class);
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "StockOperationType.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "PaymentModeTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillTest.xml");
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#validate(BillLineItem)
	 */
	@Test
	public void validate_shouldThrowIllegalArgumentExceptionIfBillLineItemIsNull() {
		assertThrows(NullPointerException.class, () -> billLineItemService.save(null));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#validate(BillLineItem)
	 */
	@Test
	public void validate_shouldThrowIllegalArgumentExceptionIfStockItemUuidDoesNotExist() {
		// Get a valid bill from test data
		Bill validBill = billService.getByUuid("4028814B39B565A20139B95D74360004");
		assertNotNull(validBill, "Test data should include a valid bill");
		
		BillLineItem lineItem = new BillLineItem();
		lineItem.setBill(validBill);
		lineItem.setPrice(BigDecimal.valueOf(100.00));
		lineItem.setQuantity(1);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		
		// Create a StockItem with a non-existent UUID
		StockItem stockItem = new StockItem();
		stockItem.setUuid("non-existent-uuid-12345");
		lineItem.setItem(stockItem);
		
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
		    () -> billLineItemService.save(lineItem));
		assertTrue(exception.getMessage().contains("A stock item with the given uuid does not exist"));
		assertTrue(exception.getMessage().contains("non-existent-uuid-12345"));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#validate(BillLineItem)
	 */
	@Test
	public void validate_shouldThrowIllegalArgumentExceptionIfBillableServiceUuidDoesNotExist() {
		// Get a valid bill from test data
		Bill validBill = billService.getByUuid("4028814B39B565A20139B95D74360004");
		assertNotNull(validBill, "Test data should include a valid bill");
		
		BillLineItem lineItem = new BillLineItem();
		lineItem.setBill(validBill);
		lineItem.setPrice(BigDecimal.valueOf(100.00));
		lineItem.setQuantity(1);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		
		// Create a BillableService with a non-existent UUID
		BillableService billableService = new BillableService();
		billableService.setUuid("non-existent-service-uuid-67890");
		lineItem.setBillableService(billableService);
		
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
		    () -> billLineItemService.save(lineItem));
		assertTrue(exception.getMessage().contains("A billable service with the given uuid does not exist"));
		assertTrue(exception.getMessage().contains("non-existent-service-uuid-67890"));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#validate(BillLineItem)
	 */
	@Test
	public void validate_shouldNotThrowExceptionIfValidStockItemIsSet() {
		// Get a valid bill from test data
		Bill validBill = billService.getByUuid("4028814B39B565A20139B95D74360004");
		assertNotNull(validBill, "Test data should include a valid bill");
		
		// Get a valid stock item from test data
		StockItem validStockItem = stockService.getStockItemByUuid("5631b434-78aa-102b-91a0-001e378eb67f");
		assertNotNull(validStockItem, "Test data should include a valid stock item");
		
		BillLineItem lineItem = new BillLineItem();
		lineItem.setBill(validBill);
		lineItem.setPrice(BigDecimal.valueOf(100.00));
		lineItem.setQuantity(1);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		lineItem.setItem(validStockItem);
		
		// Should not throw an exception
		assertDoesNotThrow(() -> billLineItemService.save(lineItem));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#validate(BillLineItem)
	 */
	@Test
	public void validate_shouldNotThrowExceptionIfItemIsNull() {
		// Get a valid bill from test data
		Bill validBill = billService.getByUuid("4028814B39B565A20139B95D74360004");
		assertNotNull(validBill, "Test data should include a valid bill");
		
		BillLineItem lineItem = new BillLineItem();
		lineItem.setBill(validBill);
		lineItem.setPrice(BigDecimal.valueOf(100.00));
		lineItem.setQuantity(1);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		lineItem.setItem(null);
		
		// Should not throw an exception when item is null (it's nullable)
		assertDoesNotThrow(() -> billLineItemService.save(lineItem));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#validate(BillLineItem)
	 */
	@Test
	public void validate_shouldNotThrowExceptionIfBillableServiceIsNull() {
		// Get a valid bill from test data
		Bill validBill = billService.getByUuid("4028814B39B565A20139B95D74360004");
		assertNotNull(validBill, "Test data should include a valid bill");
		
		BillLineItem lineItem = new BillLineItem();
		lineItem.setBill(validBill);
		lineItem.setPrice(BigDecimal.valueOf(100.00));
		lineItem.setQuantity(1);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		lineItem.setBillableService(null);
		
		// Should not throw an exception when billableService is null (it's nullable)
		assertDoesNotThrow(() -> billLineItemService.save(lineItem));
	}
}
