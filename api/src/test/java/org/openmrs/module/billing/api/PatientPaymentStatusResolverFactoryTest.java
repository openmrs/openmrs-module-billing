/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.ModuleSettings;
import org.openmrs.module.billing.api.impl.DefaultPatientPaymentStatusResolver;
import org.openmrs.module.billing.api.model.PatientPaymentStatusResult;

public class PatientPaymentStatusResolverFactoryTest {
	
	private AdministrationService administrationService;
	
	private PatientPaymentStatusResolverFactory factory;
	
	private MockedStatic<Context> contextMock;
	
	@BeforeEach
	public void setUp() {
		administrationService = mock(AdministrationService.class);
		contextMock = mockStatic(Context.class);
		contextMock.when(Context::getAdministrationService).thenReturn(administrationService);
		factory = new PatientPaymentStatusResolverFactory();
	}
	
	@AfterEach
	public void tearDown() {
		if (contextMock != null) {
			contextMock.close();
		}
	}
	
	@Test
	public void getResolver_shouldReturnRegisteredDefaultWhenGpIsBlank() {
		DefaultPatientPaymentStatusResolver registeredDefault = new DefaultPatientPaymentStatusResolver(null);
		when(administrationService.getGlobalProperty(ModuleSettings.PATIENT_PAYMENT_STATUS_RESOLVER)).thenReturn("");
		contextMock.when(() -> Context.getRegisteredComponents(PatientPaymentStatusResolver.class))
		        .thenReturn(Collections.singletonList(registeredDefault));
		
		assertSame(registeredDefault, factory.getResolver());
	}
	
	@Test
	public void getResolver_shouldReturnRegisteredDefaultWhenGpIsNull() {
		DefaultPatientPaymentStatusResolver registeredDefault = new DefaultPatientPaymentStatusResolver(null);
		when(administrationService.getGlobalProperty(ModuleSettings.PATIENT_PAYMENT_STATUS_RESOLVER)).thenReturn(null);
		contextMock.when(() -> Context.getRegisteredComponents(PatientPaymentStatusResolver.class))
		        .thenReturn(Collections.singletonList(registeredDefault));
		
		assertSame(registeredDefault, factory.getResolver());
	}
	
	@Test
	public void getResolver_shouldReturnRegisteredResolverWhenGpMatchesClassName() {
		StubResolverA registered = new StubResolverA();
		when(administrationService.getGlobalProperty(ModuleSettings.PATIENT_PAYMENT_STATUS_RESOLVER))
		        .thenReturn(StubResolverA.class.getName());
		contextMock.when(() -> Context.getRegisteredComponents(PatientPaymentStatusResolver.class))
		        .thenReturn(Collections.singletonList(registered));
		
		assertSame(registered, factory.getResolver());
	}
	
	@Test
	public void getResolver_shouldPickMatchingResolverFromMultipleRegistered() {
		StubResolverA a = new StubResolverA();
		StubResolverB b = new StubResolverB();
		when(administrationService.getGlobalProperty(ModuleSettings.PATIENT_PAYMENT_STATUS_RESOLVER))
		        .thenReturn(StubResolverB.class.getName());
		contextMock.when(() -> Context.getRegisteredComponents(PatientPaymentStatusResolver.class))
		        .thenReturn(Arrays.asList(a, b));
		
		assertSame(b, factory.getResolver());
	}
	
	@Test
	public void getResolver_shouldThrowApiExceptionWhenNoRegisteredResolverMatches() {
		when(administrationService.getGlobalProperty(ModuleSettings.PATIENT_PAYMENT_STATUS_RESOLVER))
		        .thenReturn("does.not.exist.Resolver");
		contextMock.when(() -> Context.getRegisteredComponents(PatientPaymentStatusResolver.class))
		        .thenReturn(Collections.singletonList(new StubResolverA()));
		
		APIException ex = assertThrows(APIException.class, () -> factory.getResolver());
		assertTrue(ex.getMessage().contains("does.not.exist.Resolver"));
	}
	
	@Test
	public void getResolver_shouldThrowApiExceptionWhenDefaultNotRegistered() {
		when(administrationService.getGlobalProperty(ModuleSettings.PATIENT_PAYMENT_STATUS_RESOLVER)).thenReturn("");
		contextMock.when(() -> Context.getRegisteredComponents(PatientPaymentStatusResolver.class))
		        .thenReturn(Collections.emptyList());
		
		APIException ex = assertThrows(APIException.class, () -> factory.getResolver());
		assertTrue(ex.getMessage().contains(DefaultPatientPaymentStatusResolver.class.getName()));
	}
	
	public static class StubResolverA implements PatientPaymentStatusResolver {
		
		@Override
		public PatientPaymentStatusResult resolve(Patient patient) {
			return null;
		}
	}
	
	public static class StubResolverB implements PatientPaymentStatusResolver {
		
		@Override
		public PatientPaymentStatusResult resolve(Patient patient) {
			return null;
		}
	}
}
