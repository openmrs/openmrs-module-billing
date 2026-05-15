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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.api.model.DiscountStatus;
import org.openmrs.module.billing.api.search.BillSearch;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.response.InvalidSearchException;

/**
 * Tests for {@link BillResource}
 */
public class BillResourceTest {
	
	private BillResource resource;
	
	private BillService billService;
	
	private MockedStatic<Context> contextMock;
	
	/** Captures the BillSearch passed to billService.getBills() in the current test. */
	private final List<BillSearch> capturedSearches = new ArrayList<>();
	
	@BeforeEach
	public void setUp() {
		resource = new BillResource();
		billService = mock(BillService.class);
		capturedSearches.clear();
		
		doAnswer(invocation -> {
			capturedSearches.add(invocation.getArgument(0));
			PagingInfo pagingInfo = invocation.getArgument(1);
			pagingInfo.setTotalRecordCount(0L);
			return Collections.emptyList();
		}).when(billService).getBills(any(), any());
		
		contextMock = mockStatic(Context.class);
		contextMock.when(() -> Context.getService(BillService.class)).thenReturn(billService);
	}
	
	@AfterEach
	public void tearDown() {
		if (contextMock != null) {
			contextMock.close();
		}
	}
	
	/**
	 * Builds a mocked {@link RequestContext} with the given discountStatus request parameter value
	 * (null means the parameter is absent).
	 */
	private RequestContext buildContext(String discountStatusParam) {
		HttpServletRequest req = mock(HttpServletRequest.class);
		org.mockito.Mockito.when(req.getParameter("discountStatus")).thenReturn(discountStatusParam);
		
		RequestContext context = mock(RequestContext.class);
		org.mockito.Mockito.when(context.getRequest()).thenReturn(req);
		org.mockito.Mockito.when(context.getStartIndex()).thenReturn(0);
		org.mockito.Mockito.when(context.getLimit()).thenReturn(10);
		
		return context;
	}
	
	@Test
	public void doSearch_shouldParseSingleDiscountStatusParam() {
		RequestContext context = buildContext("PENDING");
		
		resource.doSearch(context);
		
		assertEquals(Collections.singletonList(DiscountStatus.PENDING), capturedSearches.get(0).getDiscountStatuses());
	}
	
	@Test
	public void doSearch_shouldParseCommaSeparatedDiscountStatuses() {
		RequestContext context = buildContext("approved, rejected");
		
		resource.doSearch(context);
		
		assertEquals(Arrays.asList(DiscountStatus.APPROVED, DiscountStatus.REJECTED),
		    capturedSearches.get(0).getDiscountStatuses());
	}
	
	@Test
	public void doSearch_shouldRejectInvalidDiscountStatus() {
		RequestContext context = buildContext("MAYBE");
		
		InvalidSearchException ex = assertThrows(InvalidSearchException.class, () -> resource.doSearch(context));
		assertTrue(ex.getMessage().contains("MAYBE"));
		assertTrue(ex.getMessage().contains("PENDING"));
	}
	
	@Test
	public void doSearch_shouldNotSetDiscountStatusesWhenParamMissing() {
		RequestContext context = buildContext(null);
		
		resource.doSearch(context);
		
		assertNull(capturedSearches.get(0).getDiscountStatuses());
	}
	
	@Test
	public void doSearch_shouldPassVisitUuidIntoBillSearch() {
		RequestContext context = mock(RequestContext.class);
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(context.getRequest()).thenReturn(request);
		when(request.getParameter("visitUuid")).thenReturn("11111111-1111-1111-1111-111111111111");
		when(context.getLimit()).thenReturn(10);
		when(context.getStartIndex()).thenReturn(0);
		
		resource.doSearch(context);
		
		assertEquals(1, capturedSearches.size());
		assertEquals("11111111-1111-1111-1111-111111111111", capturedSearches.get(0).getVisitUuid());
	}
	
	@Test
	public void doSearch_shouldLeaveVisitUuidNullWhenAbsent() {
		RequestContext context = mock(RequestContext.class);
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(context.getRequest()).thenReturn(request);
		when(request.getParameter("visitUuid")).thenReturn(null);
		when(context.getLimit()).thenReturn(10);
		when(context.getStartIndex()).thenReturn(0);
		
		resource.doSearch(context);
		
		assertNull(capturedSearches.get(0).getVisitUuid());
	}
	
	@Test
	public void save_shouldAutoPopulateVisitWhenPatientHasSingleActiveVisit() {
		VisitService visitService = mock(VisitService.class);
		contextMock.when(() -> Context.getVisitService()).thenReturn(visitService);
		
		Patient patient = new Patient();
		Visit visit = new Visit();
		when(visitService.getActiveVisitsByPatient(patient)).thenReturn(Collections.singletonList(visit));
		when(billService.saveBill(any())).thenAnswer(inv -> inv.getArgument(0));
		
		Bill bill = new Bill();
		bill.setPatient(patient);
		bill.setCashier(new Provider());
		bill.setCashPoint(new CashPoint());
		bill.setStatus(BillStatus.PENDING);
		bill.setPayments(new HashSet<>());
		
		resource.save(bill);
		
		assertEquals(visit, bill.getVisit());
	}
	
	@Test
	public void save_shouldLeaveVisitNullWhenZeroActiveVisits() {
		VisitService visitService = mock(VisitService.class);
		contextMock.when(() -> Context.getVisitService()).thenReturn(visitService);
		
		Patient patient = new Patient();
		when(visitService.getActiveVisitsByPatient(patient)).thenReturn(Collections.emptyList());
		when(billService.saveBill(any())).thenAnswer(inv -> inv.getArgument(0));
		
		Bill bill = new Bill();
		bill.setPatient(patient);
		bill.setCashier(new Provider());
		bill.setCashPoint(new CashPoint());
		bill.setStatus(BillStatus.PENDING);
		bill.setPayments(new HashSet<>());
		
		resource.save(bill);
		
		assertNull(bill.getVisit());
	}
	
	@Test
	public void save_shouldLeaveVisitNullWhenMultipleActiveVisits() {
		VisitService visitService = mock(VisitService.class);
		contextMock.when(() -> Context.getVisitService()).thenReturn(visitService);
		
		Patient patient = new Patient();
		when(visitService.getActiveVisitsByPatient(patient)).thenReturn(Arrays.asList(new Visit(), new Visit()));
		when(billService.saveBill(any())).thenAnswer(inv -> inv.getArgument(0));
		
		Bill bill = new Bill();
		bill.setPatient(patient);
		bill.setCashier(new Provider());
		bill.setCashPoint(new CashPoint());
		bill.setStatus(BillStatus.PENDING);
		bill.setPayments(new HashSet<>());
		
		resource.save(bill);
		
		assertNull(bill.getVisit());
	}
	
	@Test
	public void save_shouldKeepCallerSuppliedVisit() {
		VisitService visitService = mock(VisitService.class);
		contextMock.when(() -> Context.getVisitService()).thenReturn(visitService);
		
		Patient patient = new Patient();
		Visit caller = new Visit();
		when(billService.saveBill(any())).thenAnswer(inv -> inv.getArgument(0));
		
		Bill bill = new Bill();
		bill.setPatient(patient);
		bill.setVisit(caller);
		bill.setCashier(new Provider());
		bill.setCashPoint(new CashPoint());
		bill.setStatus(BillStatus.PENDING);
		bill.setPayments(new HashSet<>());
		
		resource.save(bill);
		
		assertEquals(caller, bill.getVisit());
		verify(visitService, never()).getActiveVisitsByPatient(any());
	}
}
