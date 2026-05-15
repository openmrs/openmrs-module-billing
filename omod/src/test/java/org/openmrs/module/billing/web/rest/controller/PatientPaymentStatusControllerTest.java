/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.web.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.PatientPaymentStatusResolver;
import org.openmrs.module.billing.api.PatientPaymentStatusResolverFactory;
import org.openmrs.module.billing.api.model.PatientPaymentStatus;
import org.openmrs.module.billing.api.model.PatientPaymentStatusResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class PatientPaymentStatusControllerTest {
	
	private PatientPaymentStatusController controller;
	
	private PatientPaymentStatusResolverFactory factory;
	
	private PatientPaymentStatusResolver resolver;
	
	private PatientService patientService;
	
	private MockedStatic<Context> contextMock;
	
	@BeforeEach
	public void setUp() {
		factory = mock(PatientPaymentStatusResolverFactory.class);
		resolver = mock(PatientPaymentStatusResolver.class);
		patientService = mock(PatientService.class);
		
		contextMock = mockStatic(Context.class);
		contextMock.when(Context::getPatientService).thenReturn(patientService);
		
		controller = new PatientPaymentStatusController(factory);
	}
	
	@AfterEach
	public void tearDown() {
		if (contextMock != null) {
			contextMock.close();
		}
	}
	
	@Test
	public void getPatientPaymentStatus_shouldReturn404WhenPatientNotFound() {
		when(patientService.getPatientByUuid("missing")).thenReturn(null);
		
		ResponseEntity<PatientPaymentStatusResult> response = controller.getPatientPaymentStatus("missing");
		
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		verifyNoInteractions(factory);
	}
	
	@Test
	public void getPatientPaymentStatus_shouldReturn200WithResolverResult() {
		Patient patient = patientWithUuid("abc");
		PatientPaymentStatusResult expected = PatientPaymentStatusResult.builder().status(PatientPaymentStatus.PAID)
		        .reason("No outstanding bills").build();
		
		when(patientService.getPatientByUuid("abc")).thenReturn(patient);
		when(factory.getResolver()).thenReturn(resolver);
		when(resolver.resolve(patient)).thenReturn(expected);
		
		ResponseEntity<PatientPaymentStatusResult> response = controller.getPatientPaymentStatus("abc");
		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertSame(expected, response.getBody());
	}
	
	@Test
	public void getPatientPaymentStatus_shouldPassLookedUpPatientToResolver() {
		Patient patient = patientWithUuid("abc");
		
		when(patientService.getPatientByUuid("abc")).thenReturn(patient);
		when(factory.getResolver()).thenReturn(resolver);
		when(resolver.resolve(patient)).thenReturn(PatientPaymentStatusResult.builder().build());
		
		controller.getPatientPaymentStatus("abc");
		
		verify(resolver).resolve(patient);
	}
	
	@Test
	public void getPatientPaymentStatus_shouldDelegateToFactoryEachCall() {
		Patient patient = patientWithUuid("abc");
		
		when(patientService.getPatientByUuid("abc")).thenReturn(patient);
		when(factory.getResolver()).thenReturn(resolver);
		when(resolver.resolve(patient)).thenReturn(PatientPaymentStatusResult.builder().build());
		
		controller.getPatientPaymentStatus("abc");
		controller.getPatientPaymentStatus("abc");
		
		verify(factory, times(2)).getResolver();
	}
	
	private Patient patientWithUuid(String uuid) {
		Patient patient = new Patient();
		patient.setUuid(uuid);
		return patient;
	}
}
