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
	
	private PaymentModeService paymentModeService;
	
	@BeforeEach
	public void setup() {
		billValidator = new BillValidator();
		billService = Context.getService(BillService.class);
		paymentModeService = Context.getService(PaymentModeService.class);
		
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
		Bill postedBill = billService.getBill(0);
		assertNotNull(postedBill);
		assertEquals(BillStatus.POSTED, postedBill.getStatus());
		
		PaymentMode paymentMode = paymentModeService.getPaymentMode(0);
		
		Payment payment = Payment.builder().amount(BigDecimal.valueOf(10.0)).amountTendered(BigDecimal.valueOf(10.0))
		        .build();
		payment.setInstanceType(paymentMode);
		// cashier intentionally NOT set
		
		postedBill.addPayment(payment);
		
		Errors errors = new BindException(postedBill, "bill");
		billValidator.validate(postedBill, errors);
		
		assertTrue(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldTolerateExistingPaymentsWithNoCashier() {
		// Legacy payments (with ID) that have no cashier must be tolerated
		Bill paidBill = billService.getBill(1);
		assertNotNull(paidBill);
		assertFalse(paidBill.getPayments().isEmpty());
		
		Payment existingPayment = paidBill.getPayments().iterator().next();
		assertNotNull(existingPayment.getId(), "Existing payment should have an ID");
		
		Errors errors = new BindException(paidBill, "bill");
		billValidator.validate(paidBill, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldTolerateVoidedNewPaymentWithNoCashier() {
		Bill postedBill = billService.getBill(0);
		assertNotNull(postedBill);
		assertEquals(BillStatus.POSTED, postedBill.getStatus());
		
		PaymentMode paymentMode = paymentModeService.getPaymentMode(0);
		
		Payment payment = Payment.builder().amount(BigDecimal.valueOf(10.0)).amountTendered(BigDecimal.valueOf(10.0))
		        .build();
		payment.setInstanceType(paymentMode);
		payment.setVoided(true);
		// cashier intentionally NOT set
		
		postedBill.addPayment(payment);
		
		Errors errors = new BindException(postedBill, "bill");
		billValidator.validate(postedBill, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldRejectRefundRequestedBillWithNoRefundReason() {
		Bill paidBill = billService.getBill(1);
		assertNotNull(paidBill);
		paidBill.setStatus(BillStatus.REFUND_REQUESTED);
		// refundReason intentionally NOT set
		
		Errors errors = new BindException(paidBill, "bill");
		billValidator.validate(paidBill, errors);
		
		assertTrue(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldNotRejectRefundRequestedBillWithRefundReason() {
		Bill paidBill = billService.getBill(1);
		assertNotNull(paidBill);
		paidBill.setStatus(BillStatus.REFUND_REQUESTED);
		paidBill.setRefundReason("Equipment failure");
		
		Errors errors = new BindException(paidBill, "bill");
		billValidator.validate(paidBill, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldRejectRefundDeniedBillWithNoDenialReason() {
		Bill paidBill = billService.getBill(1);
		assertNotNull(paidBill);
		paidBill.setStatus(BillStatus.REFUND_DENIED);
		// denialReason intentionally NOT set
		
		Errors errors = new BindException(paidBill, "bill");
		billValidator.validate(paidBill, errors);
		
		assertTrue(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldNotRejectRefundDeniedBillWithDenialReason() {
		Bill paidBill = billService.getBill(1);
		assertNotNull(paidBill);
		paidBill.setStatus(BillStatus.REFUND_DENIED);
		paidBill.setRefundDenialReason("Service was already provided");
		
		Errors errors = new BindException(paidBill, "bill");
		billValidator.validate(paidBill, errors);
		
		assertFalse(errors.hasErrors());
	}
	
}
