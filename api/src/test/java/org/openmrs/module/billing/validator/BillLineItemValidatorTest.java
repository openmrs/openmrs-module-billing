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

package org.openmrs.module.billing.validator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.IBillService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Integration tests for {@link BillLineItemValidator}
 */
public class BillLineItemValidatorTest extends BaseModuleContextSensitiveTest {
	
	private BillLineItemValidator billLineItemValidator;
	
	private IBillService billService;
	
	private IBillService mockBillService;
	
	@BeforeEach
	public void setup() throws Exception {
		billLineItemValidator = new BillLineItemValidator();
		billService = Context.getService(IBillService.class);
		mockBillService = mock(IBillService.class);
		
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "StockOperationType.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "PaymentModeTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillTest.xml");
	}
	
	@Test
	public void validate_shouldNotRejectLineItemFromPendingBill() {
		Bill pendingBill = billService.getById(2);
		assertNotNull(pendingBill);
		assertEquals(BillStatus.PENDING, pendingBill.getStatus());
		assertFalse(pendingBill.getLineItems().isEmpty());
		
		BillLineItem lineItem = pendingBill.getLineItems().get(0);
		
		// Mock the getBillStatusFromDb to return PENDING
		try (MockedStatic<Context> contextMock = mockStatic(Context.class, CALLS_REAL_METHODS)) {
			contextMock.when(() -> Context.getService(IBillService.class)).thenReturn(mockBillService);
			when(mockBillService.getBillStatus(pendingBill.getUuid())).thenReturn(BillStatus.PENDING);
			
			Errors errors = new BindException(lineItem, "billLineItem");
			billLineItemValidator.validate(lineItem, errors);
			
			assertFalse(errors.hasErrors());
		}
	}
	
	@Test
	public void validate_shouldNotRejectLineItemFromPostedBill() {
		Bill postedBill = billService.getById(0);
		assertNotNull(postedBill);
		assertEquals(BillStatus.POSTED, postedBill.getStatus());
		assertFalse(postedBill.getLineItems().isEmpty());
		
		BillLineItem lineItem = postedBill.getLineItems().get(0);
		
		// Mock the getBillStatusFromDb to return POSTED
		try (MockedStatic<Context> contextMock = mockStatic(Context.class, CALLS_REAL_METHODS)) {
			contextMock.when(() -> Context.getService(IBillService.class)).thenReturn(mockBillService);
			when(mockBillService.getBillStatus(postedBill.getUuid())).thenReturn(BillStatus.POSTED);
			
			Errors errors = new BindException(lineItem, "billLineItem");
			billLineItemValidator.validate(lineItem, errors);
			
			assertFalse(errors.hasErrors());
		}
	}
	
	@Test
	public void validate_shouldRejectLineItemFromPaidBill() {
		Bill paidBill = billService.getById(1);
		assertNotNull(paidBill);
		assertEquals(BillStatus.PAID, paidBill.getStatus());
		assertFalse(paidBill.getLineItems().isEmpty());
		
		BillLineItem lineItem = paidBill.getLineItems().get(0);
		
		// Mock the getBillStatusFromDb to return PAID
		try (MockedStatic<Context> contextMock = mockStatic(Context.class, CALLS_REAL_METHODS)) {
			contextMock.when(() -> Context.getService(IBillService.class)).thenReturn(mockBillService);
			when(mockBillService.getBillStatus(paidBill.getUuid())).thenReturn(BillStatus.PAID);
			
			Errors errors = new BindException(lineItem, "billLineItem");
			billLineItemValidator.validate(lineItem, errors);
			
			assertTrue(errors.hasErrors());
			assertNotNull(errors.getGlobalError());
			assertTrue(errors.getGlobalError().getDefaultMessage()
			        .contains("Bill can only be modified when the bill is in PENDING or POSTED states"));
		}
	}
	
	@Test
	public void validate_shouldRejectLineItemFromAdjustedBill() {
		Bill paidBill = billService.getById(1);
		assertNotNull(paidBill);
		assertFalse(paidBill.getLineItems().isEmpty());
		
		BillLineItem lineItem = paidBill.getLineItems().get(0);
		
		// Mock the getBillStatusFromDb to return ADJUSTED
		try (MockedStatic<Context> contextMock = mockStatic(Context.class, CALLS_REAL_METHODS)) {
			contextMock.when(() -> Context.getService(IBillService.class)).thenReturn(mockBillService);
			when(mockBillService.getBillStatus(paidBill.getUuid())).thenReturn(BillStatus.ADJUSTED);
			
			Errors errors = new BindException(lineItem, "billLineItem");
			billLineItemValidator.validate(lineItem, errors);
			
			assertTrue(errors.hasErrors());
			assertNotNull(errors.getGlobalError());
			assertTrue(errors.getGlobalError().getDefaultMessage()
			        .contains("Bill can only be modified when the bill is in PENDING or POSTED states"));
		}
	}
	
	@Test
	public void validate_shouldHandleNewLineItemWithoutBill() {
		BillLineItem newLineItem = new BillLineItem();
		newLineItem.setPrice(BigDecimal.valueOf(100.00));
		newLineItem.setQuantity(1);
		newLineItem.setPaymentStatus(BillStatus.PENDING);
		
		// Line item without a bill should not cause errors
		Errors errors = new BindException(newLineItem, "billLineItem");
		// This should not throw NPE
		assertDoesNotThrow(() -> billLineItemValidator.validate(newLineItem, errors));
	}
}
