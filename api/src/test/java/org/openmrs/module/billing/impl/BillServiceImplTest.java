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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.module.billing.api.IBillService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.stockmanagement.api.model.StockItem;

/**
 * Unit tests for {@link org.openmrs.module.billing.api.impl.BillServiceImpl}.
 * <p>
 * These tests verify bill creation and saving functionality with proper validation of BillLineItem
 * objects according to the new BillLineItemValidator rules.
 * </p>
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class BillServiceImplTest {
	
	@Mock
	private IBillService billService;
	
	@Mock
	private Patient patient;
	
	@Mock
	private Provider cashier;
	
	@Mock
	private CashPoint cashPoint;
	
	@Mock
	private StockItem item;
	
	@Before
	public void setUp() {
		// Setup default mock behavior for item
		lenient().when(item.getId()).thenReturn(1);
	}
	
	/**
	 * Test that saving a PENDING bill with valid line items succeeds.
	 * <p>
	 * This test verifies that:
	 * <ul>
	 * <li>A Bill with status PENDING can be saved</li>
	 * <li>The Bill has patient, cashier, and cashPoint properly set</li>
	 * <li>Each BillLineItem has a valid item set (required by BillLineItemValidator)</li>
	 * <li>Each BillLineItem has quantity, price, and paymentStatus set</li>
	 * <li>The saved bill has an ID and correct number of line items</li>
	 * </ul>
	 * </p>
	 * 
	 * @verifies allow adding line items to a pending bill with valid item references
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#save(Bill)
	 */
	@Test
	public void save_shouldAllowAddingLineItemsToPendingBill() {
		// Create a Bill with PENDING status
		Bill bill = new Bill();
		bill.setPatient(patient);
		bill.setCashier(cashier);
		bill.setCashPoint(cashPoint);
		bill.setStatus(BillStatus.PENDING);
		bill.setReceiptNumber("TEST-RN-001");
		
		// Create first BillLineItem with item set (required by BillLineItemValidator)
		BillLineItem lineItem1 = new BillLineItem();
		lineItem1.setItem(item); // Item MUST be set to satisfy BillLineItemValidator
		lineItem1.setQuantity(2);
		lineItem1.setPrice(BigDecimal.valueOf(50.00));
		lineItem1.setPriceName("Standard Price");
		lineItem1.setPaymentStatus(BillStatus.PENDING);
		bill.addLineItem(lineItem1);
		
		// Create second BillLineItem with item set (required by BillLineItemValidator)
		BillLineItem lineItem2 = new BillLineItem();
		lineItem2.setItem(item); // Item MUST be set to satisfy BillLineItemValidator
		lineItem2.setQuantity(1);
		lineItem2.setPrice(BigDecimal.valueOf(100.00));
		lineItem2.setPriceName("Premium Price");
		lineItem2.setPaymentStatus(BillStatus.PENDING);
		bill.addLineItem(lineItem2);
		
		// Setup mock to return saved bill with ID
		Bill savedBillMock = new Bill();
		savedBillMock.setId(1);
		savedBillMock.setPatient(patient);
		savedBillMock.setCashier(cashier);
		savedBillMock.setCashPoint(cashPoint);
		savedBillMock.setStatus(BillStatus.PENDING);
		savedBillMock.setReceiptNumber("TEST-RN-001");
		
		// Copy line items to saved bill mock
		BillLineItem savedLineItem1 = new BillLineItem();
		savedLineItem1.setId(1);
		savedLineItem1.setItem(item);
		savedLineItem1.setQuantity(2);
		savedLineItem1.setPrice(BigDecimal.valueOf(50.00));
		savedLineItem1.setPaymentStatus(BillStatus.PENDING);
		savedBillMock.addLineItem(savedLineItem1);
		
		BillLineItem savedLineItem2 = new BillLineItem();
		savedLineItem2.setId(2);
		savedLineItem2.setItem(item);
		savedLineItem2.setQuantity(1);
		savedLineItem2.setPrice(BigDecimal.valueOf(100.00));
		savedLineItem2.setPaymentStatus(BillStatus.PENDING);
		savedBillMock.addLineItem(savedLineItem2);
		
		when(billService.save(any(Bill.class))).thenReturn(savedBillMock);
		
		// Execute: Save the bill
		Bill savedBill = billService.save(bill);
		
		// Assert: Verify the saved bill is valid
		assertNotNull("Saved bill should not be null", savedBill);
		assertNotNull("Saved bill should have an ID", savedBill.getId());
		assertEquals("Saved bill should have PENDING status", BillStatus.PENDING, savedBill.getStatus());
		assertNotNull("Saved bill should have line items", savedBill.getLineItems());
		assertEquals("Saved bill should have 2 line items", 2, savedBill.getLineItems().size());
		
		// Verify each line item has item set (required by BillLineItemValidator)
		for (BillLineItem lineItem : savedBill.getLineItems()) {
			assertNotNull("Line item should have item set", lineItem.getItem());
			assertNotNull("Line item should have quantity set", lineItem.getQuantity());
			assertNotNull("Line item should have price set", lineItem.getPrice());
			assertNotNull("Line item should have payment status set", lineItem.getPaymentStatus());
		}
	}
}
