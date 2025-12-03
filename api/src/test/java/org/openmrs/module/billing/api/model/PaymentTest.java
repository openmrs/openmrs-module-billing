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

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Provider;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.math.BigDecimal;

/**
 * Tests for {@link Payment}.
 */
public class PaymentTest extends BaseModuleContextSensitiveTest {
	
	/**
	 * @verifies set and get cashier correctly
	 * @see Payment#setCashier(Provider)
	 * @see Payment#getCashier()
	 */
	@Test
	public void setCashier_shouldSetAndGetCashierCorrectly() throws Exception {
		Payment payment = new Payment();
		Provider cashier = new Provider();
		cashier.setProviderId(1);
		cashier.setName("admin");
		
		payment.setCashier(cashier);
		
		Assert.assertNotNull(payment.getCashier());
		Assert.assertEquals(cashier, payment.getCashier());
		Assert.assertEquals("admin", payment.getCashier().getName());
	}
	
	/**
	 * @verifies allow null cashier initially
	 * @see Payment#getCashier()
	 */
	@Test
	public void getCashier_shouldAllowNullCashierInitially() throws Exception {
		Payment payment = new Payment();
		
		Assert.assertNull(payment.getCashier());
	}
	
	/**
	 * @verifies maintain payment amount when cashier is set
	 * @see Payment#setCashier(Provider)
	 */
	@Test
	public void setCashier_shouldMaintainPaymentAmountWhenCashierIsSet() throws Exception {
		Payment payment = new Payment();
		BigDecimal amount = new BigDecimal("100.50");
		payment.setAmount(amount);
		
		Provider cashier = new Provider();
		cashier.setProviderId(2);
		payment.setCashier(cashier);
		
		Assert.assertEquals(amount, payment.getAmount());
		Assert.assertEquals(cashier, payment.getCashier());
	}
}
