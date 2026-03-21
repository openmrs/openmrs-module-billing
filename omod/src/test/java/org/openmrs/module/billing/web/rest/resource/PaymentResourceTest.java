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
package org.openmrs.module.billing.web.rest.resource;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.openmrs.Provider;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.base.ProviderUtil;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.Payment;

/**
 * Unit tests for {@link PaymentResource}
 */
public class PaymentResourceTest {
	
	private PaymentResource resource;
	
	private BillService billService;
	
	private MockedStatic<Context> contextMock;
	
	private MockedStatic<ProviderUtil> providerUtilMock;
	
	@Before
	public void setUp() {
		resource = new PaymentResource();
		billService = mock(BillService.class);
		
		contextMock = mockStatic(Context.class);
		contextMock.when(() -> Context.getService(BillService.class)).thenReturn(billService);
		
		providerUtilMock = mockStatic(ProviderUtil.class);
	}
	
	@After
	public void tearDown() {
		if (contextMock != null) {
			contextMock.close();
		}
		if (providerUtilMock != null) {
			providerUtilMock.close();
		}
	}
	
	@Test
	public void save_shouldSetCashierFromAuthenticatedProvider() {
		Provider cashier = new Provider();
		cashier.setId(1);
		providerUtilMock.when(ProviderUtil::getCurrentProvider).thenReturn(cashier);
		
		Bill bill = new Bill();
		bill.setId(1);
		Payment payment = new Payment();
		payment.setBill(bill);
		payment.setAmount(BigDecimal.TEN);
		payment.setAmountTendered(BigDecimal.TEN);
		
		when(billService.saveBill(bill)).thenReturn(bill);
		
		resource.save(payment);
		
		assertNotNull("Cashier should be set on the payment", payment.getCashier());
		assertSame("Cashier should be the current provider", cashier, payment.getCashier());
		verify(billService).saveBill(bill);
	}
	
	@Test(expected = APIException.class)
	public void save_shouldThrowAPIExceptionWhenNoProviderLinkedToUser() {
		providerUtilMock.when(ProviderUtil::getCurrentProvider).thenReturn(null);
		
		Bill bill = new Bill();
		bill.setId(1);
		Payment payment = new Payment();
		payment.setBill(bill);
		
		resource.save(payment);
	}
	
	@Test
	public void save_shouldSetCashierBeforeAddingPaymentToBill() {
		Provider cashier = new Provider();
		cashier.setId(1);
		providerUtilMock.when(ProviderUtil::getCurrentProvider).thenReturn(cashier);
		
		Bill bill = new Bill();
		bill.setId(1);
		Payment payment = new Payment();
		payment.setBill(bill);
		payment.setAmount(BigDecimal.TEN);
		payment.setAmountTendered(BigDecimal.TEN);
		
		when(billService.saveBill(bill)).thenReturn(bill);
		
		resource.save(payment);
		
		assertNotNull(payment.getCashier());
		assertTrue(bill.getPayments().contains(payment));
	}
}
