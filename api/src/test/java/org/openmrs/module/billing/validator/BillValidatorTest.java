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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Integration tests for {@link BillValidator}
 */
public class BillValidatorTest extends BaseModuleContextSensitiveTest {
	
	private BillValidator billValidator;
	
	private BillService billService;
	
	@BeforeEach
	public void setup() throws Exception {
		billValidator = new BillValidator();
		billService = Context.getService(BillService.class);
		
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "StockOperationType.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "PaymentModeTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillTest.xml");
	}
	
	@Test
	public void validate_shouldNotRejectPendingBill() {
		Bill pendingBill = billService.getBill(2);
		assertNotNull(pendingBill);
		assertEquals(BillStatus.PENDING, pendingBill.getStatus());
		
		Errors errors = new BindException(pendingBill, "bill");
		billValidator.validate(pendingBill, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldRejectPaidBill() {
		Bill paidBill = billService.getBill(1);
		assertNotNull(paidBill);
		assertEquals(BillStatus.PAID, paidBill.getStatus());
		
		Errors errors = new BindException(paidBill, "bill");
		billValidator.validate(paidBill, errors);
		
		assertTrue(errors.hasErrors());
		assertTrue(errors.getGlobalError().getDefaultMessage()
		        .contains("Bill can only be modified when the bill is in PENDING state"));
		assertTrue(errors.getGlobalError().getDefaultMessage().contains("PAID"));
	}
}
