/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.web.rest.resource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
	
	@BeforeEach
	public void setUp() {
		resource = new PaymentResource();
		billService = mock(BillService.class);
		
		contextMock = mockStatic(Context.class);
		contextMock.when(() -> Context.getService(BillService.class)).thenReturn(billService);
		
		providerUtilMock = mockStatic(ProviderUtil.class);
	}
	
	@AfterEach
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
		
		assertNotNull(payment.getCashier(), "Cashier should be set on the payment");
		assertSame(cashier, payment.getCashier(), "Cashier should be the current provider");
		verify(billService).saveBill(bill);
	}
	
	@Test
	public void save_shouldThrowAPIExceptionWhenNoProviderLinkedToUser() {
		providerUtilMock.when(ProviderUtil::getCurrentProvider).thenReturn(null);
		
		Bill bill = new Bill();
		bill.setId(1);
		Payment payment = new Payment();
		payment.setBill(bill);
		
		assertThrows(APIException.class, () -> resource.save(payment));
	}
	
	@Test
	public void save_shouldUseClientProvidedCashierWhenSet() {
		Provider clientCashier = new Provider();
		clientCashier.setId(2);
		
		Provider authenticatedCashier = new Provider();
		authenticatedCashier.setId(99);
		providerUtilMock.when(ProviderUtil::getCurrentProvider).thenReturn(authenticatedCashier);
		
		Bill bill = new Bill();
		bill.setId(1);
		Payment payment = new Payment();
		payment.setBill(bill);
		payment.setAmount(BigDecimal.TEN);
		payment.setAmountTendered(BigDecimal.TEN);
		payment.setCashier(clientCashier);
		
		when(billService.saveBill(bill)).thenReturn(bill);
		
		resource.save(payment);
		
		assertSame(clientCashier, payment.getCashier(), "Client-provided cashier should not be overwritten");
	}
	
	@Test
	public void save_shouldFallbackToAuthenticatedUserWhenNoCashierProvided() {
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
		
		assertSame(cashier, payment.getCashier(), "Authenticated user's provider should be used as fallback");
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
