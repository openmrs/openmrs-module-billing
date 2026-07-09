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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.PaymentModeAttributeTypeService;
import org.openmrs.module.billing.api.model.PaymentModeAttributeType;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;

/**
 * Unit tests for {@link PaymentModeAttributeTypeResource}
 */
public class PaymentModeAttributeTypeResourceTest {
	
	private PaymentModeAttributeTypeResource resource;
	
	private PaymentModeAttributeTypeService service;
	
	private MockedStatic<Context> contextMock;
	
	@BeforeEach
	public void setUp() {
		service = mock(PaymentModeAttributeTypeService.class);
		contextMock = mockStatic(Context.class);
		contextMock.when(() -> Context.getService(PaymentModeAttributeTypeService.class)).thenReturn(service);
		resource = new PaymentModeAttributeTypeResource();
	}
	
	@AfterEach
	public void tearDown() {
		if (contextMock != null) {
			contextMock.close();
		}
	}
	
	@Test
	public void getRepresentationDescription_shouldIncludeAllPropertiesForFullRepresentation() {
		DelegatingResourceDescription description = resource.getRepresentationDescription(new FullRepresentation());
		
		assertNotNull(description);
		assertNotNull(description.getProperties().get("uuid"));
		assertNotNull(description.getProperties().get("name"));
		assertNotNull(description.getProperties().get("description"));
		assertNotNull(description.getProperties().get("retired"));
		assertNotNull(description.getProperties().get("retireReason"));
		assertNotNull(description.getProperties().get("format"));
		assertNotNull(description.getProperties().get("regExp"));
		assertNotNull(description.getProperties().get("attributeOrder"));
		assertNotNull(description.getProperties().get("foreignKey"));
		assertNotNull(description.getProperties().get("required"));
	}
	
	@Test
	public void getRepresentationDescription_shouldIncludeAllPropertiesForDefaultRepresentation() {
		DelegatingResourceDescription description = resource.getRepresentationDescription(new DefaultRepresentation());
		
		assertNotNull(description);
		assertNotNull(description.getProperties().get("attributeOrder"));
		assertNotNull(description.getProperties().get("foreignKey"));
		assertNotNull(description.getProperties().get("required"));
	}
	
	@Test
	public void getByUniqueId_shouldDelegateToService() {
		PaymentModeAttributeType expected = new PaymentModeAttributeType();
		when(service.getPaymentModeAttributeTypeByUuid("test-uuid")).thenReturn(expected);
		
		PaymentModeAttributeType result = resource.getByUniqueId("test-uuid");
		
		assertSame(expected, result);
		verify(service).getPaymentModeAttributeTypeByUuid("test-uuid");
	}
	
	@Test
	public void getByUniqueId_shouldReturnNullIfNotFound() {
		when(service.getPaymentModeAttributeTypeByUuid("non-existent-uuid")).thenReturn(null);
		
		PaymentModeAttributeType result = resource.getByUniqueId("non-existent-uuid");
		
		assertNull(result);
		verify(service).getPaymentModeAttributeTypeByUuid("non-existent-uuid");
	}
	
	@Test
	public void save_shouldDelegateToService() {
		PaymentModeAttributeType attributeType = new PaymentModeAttributeType();
		when(service.savePaymentModeAttributeType(attributeType)).thenReturn(attributeType);
		
		PaymentModeAttributeType result = resource.save(attributeType);
		
		assertSame(attributeType, result);
		verify(service).savePaymentModeAttributeType(attributeType);
	}
	
	@Test
	public void purge_shouldDelegateToService() {
		PaymentModeAttributeType attributeType = new PaymentModeAttributeType();
		RequestContext context = mock(RequestContext.class);
		
		resource.purge(attributeType, context);
		
		verify(service).purgePaymentModeAttributeType(attributeType);
	}
	
	@Test
	public void doGetAll_shouldDelegateToService() throws Exception {
		RequestContext context = mock(RequestContext.class);
		when(context.getIncludeAll()).thenReturn(false);
		List<PaymentModeAttributeType> list = Arrays.asList(new PaymentModeAttributeType());
		when(service.getAllPaymentModeAttributeTypes(false)).thenReturn(list);
		
		NeedsPaging<?> result = (NeedsPaging<?>) resource.doGetAll(context);
		
		assertNotNull(result);
		verify(service).getAllPaymentModeAttributeTypes(false);
	}
	
	@Test
	public void newDelegate_shouldReturnNewInstance() {
		PaymentModeAttributeType result = resource.newDelegate();
		assertNotNull(result);
	}
}
