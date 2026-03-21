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

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Provider;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.PaymentModeService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.Payment;
import org.openmrs.module.billing.api.model.PaymentMode;
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
	public void setup() {
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
	public void validate_shouldNotRejectUnmodifiedPaidBill() {
		Bill paidBill = billService.getBill(1);
		assertNotNull(paidBill);
		assertEquals(BillStatus.PAID, paidBill.getStatus());
		
		Errors errors = new BindException(paidBill, "bill");
		billValidator.validate(paidBill, errors);
		
		// Unmodified PAID bills should pass validation - rejection happens only when
		// attempting to modify line items
		assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldRejectNewPaymentWithNoCashier() {
		Bill bill = billService.getBill(0);
		assertNotNull(bill);
		
		PaymentModeService paymentModeService = Context.getService(PaymentModeService.class);
		PaymentMode paymentMode = paymentModeService.getPaymentMode(0);
		
		Payment newPayment = new Payment();
		newPayment.setAmount(BigDecimal.valueOf(100.0));
		newPayment.setAmountTendered(BigDecimal.valueOf(100.0));
		newPayment.setInstanceType(paymentMode);
		// cashier intentionally NOT set — id is null (new payment)
		
		bill.addPayment(newPayment);
		
		Errors errors = new BindException(bill, "bill");
		billValidator.validate(bill, errors);
		
		assertTrue(errors.hasErrors(), "Should reject bill with uncashiered new payment");
		assertTrue(errors.getAllErrors().stream().anyMatch(e -> "billing.error.paymentCashierRequired".equals(e.getCode())),
		    "Should use correct error code");
	}
	
	@Test
	public void validate_shouldNotRejectNewPaymentWithCashier() {
		Bill bill = billService.getBill(0);
		assertNotNull(bill);
		
		PaymentModeService paymentModeService = Context.getService(PaymentModeService.class);
		PaymentMode paymentMode = paymentModeService.getPaymentMode(0);
		
		ProviderService providerService = Context.getProviderService();
		Provider cashier = providerService.getProvider(1);
		assertNotNull(cashier);
		
		Payment newPayment = new Payment();
		newPayment.setAmount(BigDecimal.valueOf(100.0));
		newPayment.setAmountTendered(BigDecimal.valueOf(100.0));
		newPayment.setInstanceType(paymentMode);
		newPayment.setCashier(cashier);
		
		bill.addPayment(newPayment);
		
		Errors errors = new BindException(bill, "bill");
		billValidator.validate(bill, errors);
		
		assertFalse(errors.hasErrors(), "Should accept bill with cashiered new payment");
	}
	
	@Test
	public void validate_shouldNotRejectExistingPaymentWithNoCashier() {
		// Legacy payments (with ID) that have no cashier must be tolerated
		// BillTest.xml has bill_id=1 with bill_payment_id=1 and no provider_id
		Bill bill = billService.getBill(1);
		assertNotNull(bill);
		assertFalse(bill.getPayments().isEmpty(), "Bill should have existing payments");
		
		Payment existingPayment = bill.getPayments().iterator().next();
		assertNotNull(existingPayment.getId(), "Existing payment should have an ID");
		
		Errors errors = new BindException(bill, "bill");
		billValidator.validate(bill, errors);
		
		assertFalse(errors.hasErrors(), "Should not reject bill with legacy payments missing cashier");
	}
}
