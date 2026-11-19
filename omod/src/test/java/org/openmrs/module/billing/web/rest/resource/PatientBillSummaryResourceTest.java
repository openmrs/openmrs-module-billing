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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.model.PatientBillSummary;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.NamedRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;

/**
 * Tests for {@link PatientBillSummaryResource}
 */
public class PatientBillSummaryResourceTest {
	
	private PatientBillSummaryResource resource;
	
	private BillService billService;
	
	private MockedStatic<Context> contextMock;
	
	@BeforeEach
	public void setUp() {
		resource = new PatientBillSummaryResource();
		billService = mock(BillService.class);
		
		// The resource reads the total from the paging info, so the mock must set it.
		doAnswer(invocation -> {
			PagingInfo pagingInfo = invocation.getArgument(1);
			pagingInfo.setTotalRecordCount(0L);
			return Collections.emptyList();
		}).when(billService).getPatientBillSummaries(any(), any());
		
		contextMock = mockStatic(Context.class);
		contextMock.when(() -> Context.getService(BillService.class)).thenReturn(billService);
	}
	
	@AfterEach
	public void tearDown() {
		if (contextMock != null) {
			contextMock.close();
		}
	}
	
	private RequestContext buildContext(String patientName, int startIndex, int limit) {
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getParameter("patientName")).thenReturn(patientName);
		
		RequestContext context = mock(RequestContext.class);
		when(context.getRequest()).thenReturn(req);
		when(context.getStartIndex()).thenReturn(startIndex);
		when(context.getLimit()).thenReturn(limit);
		return context;
	}
	
	private PatientBillSummary summaryFor(String patientUuid) {
		Patient patient = new Patient();
		patient.setUuid(patientUuid);
		return new PatientBillSummary(patient, 1L, BigDecimal.ZERO, new Date());
	}
	
	private PagingInfo capturePaging(String expectedPatientName) {
		ArgumentCaptor<PagingInfo> paging = ArgumentCaptor.forClass(PagingInfo.class);
		verify(billService).getPatientBillSummaries(eq(expectedPatientName), paging.capture());
		return paging.getValue();
	}
	
	@Test
	public void doSearch_shouldPassPatientNameAndPaging() {
		resource.doSearch(buildContext("John", 0, 10));
		
		PagingInfo paging = capturePaging("John");
		assertEquals(Integer.valueOf(1), paging.getPage());
		assertEquals(Integer.valueOf(10), paging.getPageSize());
	}
	
	@Test
	public void doGetAll_shouldPassNullPatientName() {
		resource.doGetAll(buildContext(null, 0, 10));
		
		verify(billService).getPatientBillSummaries(isNull(), any());
	}
	
	@Test
	public void doSearch_shouldTreatBlankPatientNameAsAbsent() {
		resource.doSearch(buildContext("  ", 0, 10));
		
		verify(billService).getPatientBillSummaries(isNull(), any());
	}
	
	@Test
	public void doSearch_shouldRoundStartIndexDownToPage() {
		resource.doSearch(buildContext("John", 15, 10));
		
		assertEquals(Integer.valueOf(2), capturePaging("John").getPage());
	}
	
	@Test
	public void getRepresentationDescription_shouldExposeSummaryProperties() {
		// REF is what list requests without a v= parameter use. Any other named representation gets the
		// same properties, so no named representation fails with a ConversionException.
		for (Representation rep : Arrays.asList(Representation.REF, Representation.DEFAULT, Representation.FULL,
		    new NamedRepresentation("other"))) {
			DelegatingResourceDescription description = resource.getRepresentationDescription(rep);
			
			assertNotNull(description, "No description for " + rep);
			assertEquals(new HashSet<>(Arrays.asList("patient", "billCount", "totalUnpaid", "latestBillDate")),
			    description.getProperties().keySet());
			assertEquals(Representation.REF, description.getProperties().get("patient").getRep());
			assertTrue(description.getLinks().isEmpty());
		}
	}
	
	@Test
	public void getRepresentationDescription_shouldLeaveCustomRepresentationToTheFramework() {
		assertNull(resource.getRepresentationDescription(new CustomRepresentation("(billCount)")));
	}
	
	@Test
	public void getUniqueId_shouldReturnPatientUuid() {
		assertEquals("patient-uuid", resource.getUniqueId(summaryFor("patient-uuid")));
	}
	
	@Test
	public void shouldRejectUnsupportedOperations() {
		PatientBillSummary summary = summaryFor("patient-uuid");
		RequestContext context = buildContext(null, 0, 10);
		
		assertThrows(ResourceDoesNotSupportOperationException.class, () -> resource.getByUniqueId("x"));
		assertThrows(ResourceDoesNotSupportOperationException.class, () -> resource.save(summary));
		assertThrows(ResourceDoesNotSupportOperationException.class, () -> resource.newDelegate());
		assertThrows(ResourceDoesNotSupportOperationException.class, () -> resource.delete(summary, "r", context));
		assertThrows(ResourceDoesNotSupportOperationException.class, () -> resource.purge(summary, context));
	}
}
