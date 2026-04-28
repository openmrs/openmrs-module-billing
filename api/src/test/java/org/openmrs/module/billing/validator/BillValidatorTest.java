/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
	
}
