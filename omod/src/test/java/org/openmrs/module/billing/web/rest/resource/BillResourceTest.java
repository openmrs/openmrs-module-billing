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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.base.PagingInfo;
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
	
	@Before
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
	
	@After
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
}
