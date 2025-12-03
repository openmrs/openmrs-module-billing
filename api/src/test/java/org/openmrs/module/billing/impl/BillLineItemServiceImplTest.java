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
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

public class BillLineItemServiceImplTest extends BaseModuleContextSensitiveTest {
	
	private BillLineItemService billLineItemService;
	
	private IBillService billService;
	
	@BeforeEach
	public void setup() {
		billLineItemService = Context.getService(BillLineItemService.class);
		billService = Context.getService(IBillService.class);
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "StockOperationType.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "PaymentModeTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillTest.xml");
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#save(BillLineItem)
	 */
	@Test
	public void save_shouldAllowSavingLineItemForPendingBill() {
		// Get the PENDING bill from test data (bill_id=2)
		Bill pendingBill = billService.getById(2);
		assertNotNull(pendingBill);
		assertEquals(BillStatus.PENDING, pendingBill.getStatus());
		
		// Get a line item from the pending bill
		BillLineItem lineItem = pendingBill.getLineItems().get(0);
		assertNotNull(lineItem);
		
		// Update the line item
		lineItem.setPrice(BigDecimal.valueOf(99.99));
		
		// Should not throw exception
		BillLineItem savedItem = billLineItemService.saveBill(lineItem);
		assertNotNull(savedItem);
		assertEquals(BigDecimal.valueOf(99.99), savedItem.getPrice());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#save(BillLineItem)
	 */
	@Test
	public void save_shouldThrowExceptionWhenSavingLineItemForPostedBill() {
		// Get the POSTED bill from test data (bill_id=0)
		Bill postedBill = billService.getById(0);
		assertNotNull(postedBill);
		assertEquals(BillStatus.POSTED, postedBill.getStatus());
		
		// Get a line item from the posted bill
		BillLineItem lineItem = postedBill.getLineItems().get(0);
		assertNotNull(lineItem);
		
		// Try to update the line item
		lineItem.setPrice(BigDecimal.valueOf(99.99));
		
		// Should throw exception
		assertThrows(IllegalStateException.class, () -> billLineItemService.saveBill(lineItem));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#save(BillLineItem)
	 */
	@Test
	public void save_shouldThrowExceptionWhenSavingLineItemForPaidBill() {
		// Get the PAID bill from test data (bill_id=1)
		Bill paidBill = billService.getById(1);
		assertNotNull(paidBill);
		assertEquals(BillStatus.PAID, paidBill.getStatus());
		
		// Get a line item from the paid bill
		BillLineItem lineItem = paidBill.getLineItems().get(0);
		assertNotNull(lineItem);
		
		// Try to update the line item
		lineItem.setPrice(BigDecimal.valueOf(99.99));
		
		// Should throw exception
		assertThrows(IllegalStateException.class, () -> billLineItemService.saveBill(lineItem));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#voidEntity(BillLineItem, String)
	 */
	@Test
	public void voidEntity_shouldAllowVoidingLineItemForPendingBill() {
		// Get the PENDING bill from test data (bill_id=2)
		Bill pendingBill = billService.getById(2);
		assertNotNull(pendingBill);
		assertEquals(BillStatus.PENDING, pendingBill.getStatus());
		
		// Get a line item from the pending bill
		BillLineItem lineItem = pendingBill.getLineItems().get(0);
		assertNotNull(lineItem);
		assertFalse(lineItem.getVoided());
		
		// Should not throw exception
		BillLineItem voidedItem = billLineItemService.voidEntity(lineItem, "Test void reason");
		assertNotNull(voidedItem);
		assertTrue(voidedItem.getVoided());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#voidEntity(BillLineItem, String)
	 */
	@Test
	public void voidEntity_shouldThrowExceptionWhenVoidingLineItemForPostedBill() {
		// Get the POSTED bill from test data (bill_id=0)
		Bill postedBill = billService.getById(0);
		assertNotNull(postedBill);
		assertEquals(BillStatus.POSTED, postedBill.getStatus());
		
		// Get a line item from the posted bill
		BillLineItem lineItem = postedBill.getLineItems().get(0);
		assertNotNull(lineItem);
		
		// Should throw exception
		assertThrows(IllegalStateException.class, () -> billLineItemService.voidEntity(lineItem, "Test void reason"));
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#purge(BillLineItem)
	 */
	@Test
	public void purge_shouldAllowPurgingLineItemForPendingBill() {
		// Get the PENDING bill from test data (bill_id=2)
		Bill pendingBill = billService.getById(2);
		assertNotNull(pendingBill);
		assertEquals(BillStatus.PENDING, pendingBill.getStatus());
		
		int originalSize = pendingBill.getLineItems().size();
		assertTrue(originalSize > 0);
		
		// Get a line item from the pending bill
		BillLineItem lineItem = pendingBill.getLineItems().get(0);
		assertNotNull(lineItem);
		
		// Should not throw exception
		billLineItemService.purge(lineItem);
		
		// Verify the line item was purged
		Bill updatedBill = billService.getById(2);
		assertTrue(updatedBill.getLineItems().size() < originalSize);
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.BillLineItemServiceImpl#purge(BillLineItem)
	 */
	@Test
	public void purge_shouldThrowExceptionWhenPurgingLineItemForPostedBill() {
		// Get the POSTED bill from test data (bill_id=0)
		Bill postedBill = billService.getById(0);
		assertNotNull(postedBill);
		assertEquals(BillStatus.POSTED, postedBill.getStatus());
		
		// Get a line item from the posted bill
		BillLineItem lineItem = postedBill.getLineItems().get(0);
		assertNotNull(lineItem);
		
		// Should throw exception
		assertThrows(IllegalStateException.class, () -> billLineItemService.purge(lineItem));
	}
}
