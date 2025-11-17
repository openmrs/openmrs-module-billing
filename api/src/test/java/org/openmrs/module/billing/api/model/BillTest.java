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
package org.openmrs.module.billing.api.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BillTest {
	
	private Bill bill;
	
	@BeforeEach
	void setUp() {
		bill = createBillWithTotal(new BigDecimal("100"));
	}
	
	@Test
	void addPayment_shouldThrowWhenAmountIsNull() {
		Payment payment = buildPayment(null);
		
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> bill.addPayment(payment));
		assertEquals("Payment amount cannot be null", exception.getMessage());
	}
	
	@Test
	void addPayment_shouldThrowWhenAmountIsZero() {
		Payment payment = buildPayment(BigDecimal.ZERO);
		
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> bill.addPayment(payment));
		assertEquals("Payment amount must be greater than 0", exception.getMessage());
	}
	
	@Test
	void addPayment_shouldThrowWhenAmountIsNegative() {
		Payment payment = buildPayment(new BigDecimal("-1"));
		
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> bill.addPayment(payment));
		assertEquals("Payment amount must be greater than 0", exception.getMessage());
	}
	
	@Test
	void addPayment_shouldThrowWhenAmountExceedsBillTotal() {
		Bill smallerBill = createBillWithTotal(new BigDecimal("50"));
		Payment payment = buildPayment(new BigDecimal("60"));
		
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
		    () -> smallerBill.addPayment(payment));
		assertEquals("Payment amount (60) cannot exceed remaining balance (50)", exception.getMessage());
	}
	
	@Test
	void addPayment_shouldThrowWhenAmountExceedsRemainingBalance() {
		Payment initialPayment = buildPayment(new BigDecimal("80"));
		assertDoesNotThrow(() -> bill.addPayment(initialPayment));
		
		Payment payment = buildPayment(new BigDecimal("30"));
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> bill.addPayment(payment));
		assertEquals("Payment amount (30) cannot exceed remaining balance (20)", exception.getMessage());
	}
	
	@Test
	void addPayment_shouldAllowValidAmounts() {
		Payment payment = buildPayment(new BigDecimal("50"));
		
		assertDoesNotThrow(() -> bill.addPayment(payment));
		assertEquals(1, bill.getPayments().size());
		assertSame(bill, payment.getBill());
	}
	
	private Bill createBillWithTotal(BigDecimal total) {
		Bill bill = new Bill();
		BillLineItem lineItem = new BillLineItem();
		lineItem.setBill(bill);
		lineItem.setPrice(total);
		lineItem.setQuantity(1);
		lineItem.setVoided(false);
		bill.setLineItems(Collections.singletonList(lineItem));
		return bill;
	}
	
	private Payment buildPayment(BigDecimal amount) {
		Payment payment = new Payment();
		payment.setAmount(amount);
		payment.setAmountTendered(amount);
		payment.setVoided(false);
		return payment;
	}
}
