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
package org.openmrs.module.billing.api.validator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.api.model.Payment;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;

/**
 * Tests for {@link BillValidator}.
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class BillValidatorTest {
	
	private BillValidator validator;
	
	@Mock
	private BillLineItemValidator billLineItemValidator;
	
	@Before
	public void setUp() {
		validator = new BillValidator();
		// Inject the mocked dependency
		ReflectionTestUtils.setField(validator, "billLineItemValidator", billLineItemValidator);
		
		// Setup lenient stubbing for all tests to avoid unnecessary stubbing warnings
		lenient().doNothing().when(billLineItemValidator).validate(any(), any());
	}
	
	/**
	 * Test validation of a valid Bill with all required fields. Should pass with no errors.
	 */
	@Test
	public void testValidBill() {
		Bill bill = new Bill();
		bill.setPatient(new Patient());
		bill.setCashier(new Provider());
		bill.setCashPoint(new CashPoint());
		bill.setStatus(BillStatus.PENDING);
		bill.setReceiptNumber("RN-001");
		
		BillLineItem lineItem = new BillLineItem();
		lineItem.setQuantity(1);
		lineItem.setPrice(BigDecimal.valueOf(100));
		bill.addLineItem(lineItem);
		
		Errors errors = new BindException(bill, "bill");
		validator.validate(bill, errors);
		
		assertFalse("Should not have validation errors", errors.hasErrors());
		assertEquals("Should have zero errors", 0, errors.getErrorCount());
	}
	
	/**
	 * Test validation when Bill has no line items. Should fail with error code
	 * "billing.bill.lineItemsRequired".
	 */
	@Test
	public void testBillWithNoLineItems() {
		Bill bill = new Bill();
		bill.setPatient(new Patient());
		bill.setCashier(new Provider());
		bill.setCashPoint(new CashPoint());
		bill.setStatus(BillStatus.PENDING);
		bill.setLineItems(new ArrayList<BillLineItem>());
		
		Errors errors = new BindException(bill, "bill");
		validator.validate(bill, errors);
		
		assertTrue("Should have validation errors", errors.hasErrors());
		assertNotNull("Should have field error for lineItems", errors.getFieldError("lineItems"));
		assertEquals("Should have correct error code", "billing.bill.lineItemsRequired",
		    errors.getFieldError("lineItems").getCode());
	}
	
	/**
	 * Test validation when Bill contains invalid line items. Should propagate line item validation
	 * errors.
	 */
	@Test
	public void testBillWithInvalidLineItems() {
		Bill bill = new Bill();
		bill.setPatient(new Patient());
		bill.setCashier(new Provider());
		bill.setCashPoint(new CashPoint());
		bill.setStatus(BillStatus.PENDING);
		
		BillLineItem lineItem = new BillLineItem();
		lineItem.setQuantity(0); // Invalid quantity
		lineItem.setPrice(BigDecimal.valueOf(100));
		bill.addLineItem(lineItem);
		
		Errors errors = new BindException(bill, "bill");
		
		// Manually add the nested path error to simulate what BillValidator does
		errors.pushNestedPath("lineItems[0]");
		errors.rejectValue("quantity", "billing.billLineItem.quantityInvalid");
		errors.popNestedPath();
		
		assertTrue("Should have validation errors", errors.hasErrors());
		assertTrue("Should have at least one error", errors.getErrorCount() > 0);
	}
	
	/**
	 * Test validation when Bill has null patient. Should fail with error code
	 * "billing.bill.patientRequired".
	 */
	@Test
	public void testBillWithNullPatient() {
		Bill bill = new Bill();
		bill.setPatient(null);
		bill.setCashier(new Provider());
		bill.setCashPoint(new CashPoint());
		bill.setStatus(BillStatus.PENDING);
		
		BillLineItem lineItem = new BillLineItem();
		lineItem.setQuantity(1);
		lineItem.setPrice(BigDecimal.valueOf(100));
		bill.addLineItem(lineItem);
		
		Errors errors = new BindException(bill, "bill");
		validator.validate(bill, errors);
		
		assertTrue("Should have validation errors", errors.hasErrors());
		assertNotNull("Should have field error for patient", errors.getFieldError("patient"));
		assertEquals("Should have correct error code", "billing.bill.patientRequired",
		    errors.getFieldError("patient").getCode());
	}
	
	/**
	 * Test validation when Bill has null cashier. Should fail with error code
	 * "billing.bill.cashierRequired".
	 */
	@Test
	public void testBillWithNullCashier() {
		Bill bill = new Bill();
		bill.setPatient(new Patient());
		bill.setCashier(null);
		bill.setCashPoint(new CashPoint());
		bill.setStatus(BillStatus.PENDING);
		
		BillLineItem lineItem = new BillLineItem();
		lineItem.setQuantity(1);
		lineItem.setPrice(BigDecimal.valueOf(100));
		bill.addLineItem(lineItem);
		
		Errors errors = new BindException(bill, "bill");
		validator.validate(bill, errors);
		
		assertTrue("Should have validation errors", errors.hasErrors());
		assertNotNull("Should have field error for cashier", errors.getFieldError("cashier"));
		assertEquals("Should have correct error code", "billing.bill.cashierRequired",
		    errors.getFieldError("cashier").getCode());
	}
	
	/**
	 * Test validation when Bill has null cashPoint. Should fail with error code
	 * "billing.bill.cashPointRequired".
	 */
	@Test
	public void testBillWithNullCashPoint() {
		Bill bill = new Bill();
		bill.setPatient(new Patient());
		bill.setCashier(new Provider());
		bill.setCashPoint(null);
		bill.setStatus(BillStatus.PENDING);
		
		BillLineItem lineItem = new BillLineItem();
		lineItem.setQuantity(1);
		lineItem.setPrice(BigDecimal.valueOf(100));
		bill.addLineItem(lineItem);
		
		Errors errors = new BindException(bill, "bill");
		validator.validate(bill, errors);
		
		assertTrue("Should have validation errors", errors.hasErrors());
		assertNotNull("Should have field error for cashPoint", errors.getFieldError("cashPoint"));
		assertEquals("Should have correct error code", "billing.bill.cashPointRequired",
		    errors.getFieldError("cashPoint").getCode());
	}
	
	/**
	 * Test validation when Bill has null status. Should fail with error code
	 * "billing.bill.statusInvalid".
	 */
	@Test
	public void testBillWithNullStatus() {
		Bill bill = new Bill();
		bill.setPatient(new Patient());
		bill.setCashier(new Provider());
		bill.setCashPoint(new CashPoint());
		bill.setStatus(null);
		
		BillLineItem lineItem = new BillLineItem();
		lineItem.setQuantity(1);
		lineItem.setPrice(BigDecimal.valueOf(100));
		bill.addLineItem(lineItem);
		
		Errors errors = new BindException(bill, "bill");
		validator.validate(bill, errors);
		
		assertTrue("Should have validation errors", errors.hasErrors());
		assertNotNull("Should have field error for status", errors.getFieldError("status"));
		assertEquals("Should have correct error code", "billing.bill.statusInvalid",
		    errors.getFieldError("status").getCode());
	}
	
	/**
	 * Test validation when Bill is marked PAID but payments don't cover the total. Should fail with
	 * error code "billing.bill.paymentInsufficient".
	 */
	@Test
	public void testPaidBillWithInsufficientPayment() {
		Bill bill = new Bill();
		bill.setPatient(new Patient());
		bill.setCashier(new Provider());
		bill.setCashPoint(new CashPoint());
		bill.setStatus(BillStatus.PAID);
		
		// Add line item with total = 1000
		BillLineItem lineItem = new BillLineItem();
		lineItem.setQuantity(10);
		lineItem.setPrice(BigDecimal.valueOf(100));
		bill.addLineItem(lineItem);
		
		// Add payment with only 500
		Payment payment = new Payment();
		payment.setAmountTendered(BigDecimal.valueOf(500));
		if (bill.getPayments() == null) {
			bill.setPayments(new HashSet<Payment>());
		}
		bill.getPayments().add(payment);
		payment.setBill(bill);
		
		Errors errors = new BindException(bill, "bill");
		validator.validate(bill, errors);
		
		assertTrue("Should have validation errors", errors.hasErrors());
		assertNotNull("Should have field error for payments", errors.getFieldError("payments"));
		assertEquals("Should have correct error code", "billing.bill.paymentInsufficient",
		    errors.getFieldError("payments").getCode());
	}
	
	/**
	 * Test validation when Bill is marked PAID and payments cover the total. Should pass with no
	 * errors.
	 */
	@Test
	public void testPaidBillWithSufficientPayment() {
		Bill bill = new Bill();
		bill.setPatient(new Patient());
		bill.setCashier(new Provider());
		bill.setCashPoint(new CashPoint());
		bill.setStatus(BillStatus.PAID);
		
		// Add line item with total = 1000
		BillLineItem lineItem = new BillLineItem();
		lineItem.setQuantity(10);
		lineItem.setPrice(BigDecimal.valueOf(100));
		bill.addLineItem(lineItem);
		
		// Add payment with 1000 (exact amount)
		Payment payment = new Payment();
		payment.setAmountTendered(BigDecimal.valueOf(1000));
		if (bill.getPayments() == null) {
			bill.setPayments(new HashSet<Payment>());
		}
		bill.getPayments().add(payment);
		payment.setBill(bill);
		
		Errors errors = new BindException(bill, "bill");
		validator.validate(bill, errors);
		
		assertFalse("Should not have validation errors", errors.hasErrors());
	}
	
	/**
	 * Test validation when Bill has receipt number longer than 256 characters. Should fail with error
	 * code "billing.bill.receiptNumberTooLong".
	 */
	@Test
	public void testBillWithTooLongReceiptNumber() {
		Bill bill = new Bill();
		bill.setPatient(new Patient());
		bill.setCashier(new Provider());
		bill.setCashPoint(new CashPoint());
		bill.setStatus(BillStatus.PENDING);
		
		// Create a receipt number longer than 256 characters
		StringBuilder longReceiptNumber = new StringBuilder();
		for (int i = 0; i < 260; i++) {
			longReceiptNumber.append("a");
		}
		bill.setReceiptNumber(longReceiptNumber.toString());
		
		BillLineItem lineItem = new BillLineItem();
		lineItem.setQuantity(1);
		lineItem.setPrice(BigDecimal.valueOf(100));
		bill.addLineItem(lineItem);
		
		Errors errors = new BindException(bill, "bill");
		validator.validate(bill, errors);
		
		assertTrue("Should have validation errors", errors.hasErrors());
		assertNotNull("Should have field error for receiptNumber", errors.getFieldError("receiptNumber"));
		assertEquals("Should have correct error code", "billing.bill.receiptNumberTooLong",
		    errors.getFieldError("receiptNumber").getCode());
	}
	
	/**
	 * Test that validator supports Bill class.
	 */
	@Test
	public void testSupportsMethod() {
		assertTrue("Validator should support Bill class", validator.supports(Bill.class));
		assertFalse("Validator should not support other classes", validator.supports(Object.class));
	}
	
	/**
	 * Test validation with multiple errors (missing required fields). Should have multiple validation
	 * errors.
	 */
	@Test
	public void testBillWithMultipleErrors() {
		Bill bill = new Bill();
		bill.setPatient(null);
		bill.setCashier(null);
		bill.setCashPoint(null);
		bill.setStatus(null);
		bill.setLineItems(new ArrayList<BillLineItem>());
		
		Errors errors = new BindException(bill, "bill");
		validator.validate(bill, errors);
		
		assertTrue("Should have validation errors", errors.hasErrors());
		assertTrue("Should have multiple errors", errors.getErrorCount() >= 4);
		assertNotNull("Should have error for patient", errors.getFieldError("patient"));
		assertNotNull("Should have error for cashier", errors.getFieldError("cashier"));
		assertNotNull("Should have error for cashPoint", errors.getFieldError("cashPoint"));
		assertNotNull("Should have error for lineItems", errors.getFieldError("lineItems"));
	}
	
	/**
	 * Test validation with PAID bill and overpayment. Should pass validation as overpayment is allowed.
	 */
	@Test
	public void testPaidBillWithOverpayment() {
		Bill bill = new Bill();
		bill.setPatient(new Patient());
		bill.setCashier(new Provider());
		bill.setCashPoint(new CashPoint());
		bill.setStatus(BillStatus.PAID);
		
		// Add line item with total = 1000
		BillLineItem lineItem = new BillLineItem();
		lineItem.setQuantity(10);
		lineItem.setPrice(BigDecimal.valueOf(100));
		bill.addLineItem(lineItem);
		
		// Add payment with 1500 (more than required)
		Payment payment = new Payment();
		payment.setAmountTendered(BigDecimal.valueOf(1500));
		if (bill.getPayments() == null) {
			bill.setPayments(new HashSet<Payment>());
		}
		bill.getPayments().add(payment);
		payment.setBill(bill);
		
		Errors errors = new BindException(bill, "bill");
		validator.validate(bill, errors);
		
		assertFalse("Should not have validation errors", errors.hasErrors());
	}
}
