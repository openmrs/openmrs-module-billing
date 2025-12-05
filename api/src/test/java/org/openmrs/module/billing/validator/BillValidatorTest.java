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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.IBillService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

public class BillValidatorTest extends BaseModuleContextSensitiveTest {
	
	private BillValidator billValidator;
	
	private IBillService billService;
	
	private IBillService mockBillService;
	
	@BeforeEach
	public void setup() throws Exception {
		billValidator = new BillValidator();
		billService = Context.getService(IBillService.class);
		mockBillService = mock(IBillService.class);
		
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "StockOperationType.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "PaymentModeTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillTest.xml");
	}
	
	@Test
	public void validate_shouldNotRejectPendingBill() {
		Bill pendingBill = billService.getById(2);
		assertNotNull(pendingBill);
		assertEquals(BillStatus.PENDING, pendingBill.getStatus());
		
		try (MockedStatic<Context> contextMock = mockStatic(Context.class, CALLS_REAL_METHODS)) {
			contextMock.when(() -> Context.getService(IBillService.class)).thenReturn(mockBillService);
			when(mockBillService.getBillStatus(pendingBill.getUuid())).thenReturn(BillStatus.PENDING);
			
			Errors errors = new BindException(pendingBill, "bill");
			billValidator.validate(pendingBill, errors);
			
			assertFalse(errors.hasErrors());
		}
	}
	
	@Test
	public void validate_shouldRejectPostedBill() {
		Bill postedBill = billService.getById(1);
		assertNotNull(postedBill);
		assertEquals(BillStatus.PAID, postedBill.getStatus());
		
		try (MockedStatic<Context> contextMock = mockStatic(Context.class, CALLS_REAL_METHODS)) {
			contextMock.when(() -> Context.getService(IBillService.class)).thenReturn(mockBillService);
			when(mockBillService.getBillStatus(postedBill.getUuid())).thenReturn(BillStatus.PAID);
			
			Errors errors = new BindException(postedBill, "bill");
			billValidator.validate(postedBill, errors);
			
			assertTrue(errors.hasErrors());
			assertNotNull(errors.getGlobalError());
			assertTrue(errors.getGlobalError().getDefaultMessage()
			        .contains("Bill can only be modified when the bill is in PENDING or POSTED states"));
		}
	}
}
