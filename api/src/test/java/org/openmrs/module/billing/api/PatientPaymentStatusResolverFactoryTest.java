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

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.ModuleSettings;
import org.openmrs.module.billing.api.model.PatientPaymentStatusResult;

public class PatientPaymentStatusResolverFactoryTest {
	
	private AdministrationService administrationService;
	
	private PatientPaymentStatusResolver defaultResolver;
	
	private PatientPaymentStatusResolverFactory factory;
	
	private MockedStatic<Context> contextMock;
	
	@BeforeEach
	public void setUp() {
		administrationService = mock(AdministrationService.class);
		defaultResolver = mock(PatientPaymentStatusResolver.class);
		contextMock = mockStatic(Context.class);
		contextMock.when(Context::getAdministrationService).thenReturn(administrationService);
		factory = new PatientPaymentStatusResolverFactory(defaultResolver);
	}
	
	@AfterEach
	public void tearDown() {
		if (contextMock != null) {
			contextMock.close();
		}
	}
	
	@Test
	public void getResolver_shouldReturnDefaultWhenGpIsBlank() {
		when(administrationService.getGlobalProperty(ModuleSettings.PATIENT_PAYMENT_STATUS_RESOLVER)).thenReturn("");
		assertSame(defaultResolver, factory.getResolver());
	}
	
	@Test
	public void getResolver_shouldReturnDefaultWhenGpIsNull() {
		when(administrationService.getGlobalProperty(ModuleSettings.PATIENT_PAYMENT_STATUS_RESOLVER)).thenReturn(null);
		assertSame(defaultResolver, factory.getResolver());
	}
	
	@Test
	public void getResolver_shouldInstantiateAndReturnConfiguredResolver() {
		when(administrationService.getGlobalProperty(ModuleSettings.PATIENT_PAYMENT_STATUS_RESOLVER))
		        .thenReturn(StubResolverA.class.getName());
		
		PatientPaymentStatusResolver result = factory.getResolver();
		
		assertNotSame(defaultResolver, result);
		assertInstanceOf(StubResolverA.class, result);
	}
	
	@Test
	public void getResolver_shouldReturnSameInstanceOnSubsequentCalls() {
		when(administrationService.getGlobalProperty(ModuleSettings.PATIENT_PAYMENT_STATUS_RESOLVER))
		        .thenReturn(StubResolverA.class.getName());
		
		assertSame(factory.getResolver(), factory.getResolver());
	}
	
	@Test
	public void getResolver_shouldReinstantiateWhenGpValueChanges() {
		when(administrationService.getGlobalProperty(ModuleSettings.PATIENT_PAYMENT_STATUS_RESOLVER))
		        .thenReturn(StubResolverA.class.getName(), StubResolverB.class.getName());
		
		PatientPaymentStatusResolver first = factory.getResolver();
		PatientPaymentStatusResolver second = factory.getResolver();
		
		assertInstanceOf(StubResolverA.class, first);
		assertInstanceOf(StubResolverB.class, second);
		assertNotSame(first, second);
	}
	
	@Test
	public void getResolver_shouldRevertToDefaultWhenGpIsClearedAfterBeingSet() {
		when(administrationService.getGlobalProperty(ModuleSettings.PATIENT_PAYMENT_STATUS_RESOLVER))
		        .thenReturn(StubResolverA.class.getName(), "");
		
		assertInstanceOf(StubResolverA.class, factory.getResolver());
		assertSame(defaultResolver, factory.getResolver());
	}
	
	@Test
	public void getResolver_shouldThrowApiExceptionWhenClassNotFound() {
		when(administrationService.getGlobalProperty(ModuleSettings.PATIENT_PAYMENT_STATUS_RESOLVER))
		        .thenReturn("does.not.exist.Resolver");
		
		APIException ex = assertThrows(APIException.class, () -> factory.getResolver());
		assertTrue(ex.getMessage().contains("does.not.exist.Resolver"));
	}
	
	@Test
	public void getResolver_shouldThrowApiExceptionWhenClassDoesNotImplementInterface() {
		when(administrationService.getGlobalProperty(ModuleSettings.PATIENT_PAYMENT_STATUS_RESOLVER))
		        .thenReturn(String.class.getName());
		
		APIException ex = assertThrows(APIException.class, () -> factory.getResolver());
		assertTrue(ex.getMessage().contains("does not implement"));
	}
	
	@Test
	public void getResolver_shouldThrowApiExceptionWhenClassHasNoNoArgConstructor() {
		when(administrationService.getGlobalProperty(ModuleSettings.PATIENT_PAYMENT_STATUS_RESOLVER))
		        .thenReturn(NoNoArgCtorResolver.class.getName());
		
		APIException ex = assertThrows(APIException.class, () -> factory.getResolver());
		assertTrue(ex.getMessage().contains(NoNoArgCtorResolver.class.getName()));
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
	
	public static class NoNoArgCtorResolver implements PatientPaymentStatusResolver {
		
		public NoNoArgCtorResolver(String requiresArg) {
		}
		
		@Override
		public PatientPaymentStatusResult resolve(Patient patient) {
			return null;
		}
	}
}
