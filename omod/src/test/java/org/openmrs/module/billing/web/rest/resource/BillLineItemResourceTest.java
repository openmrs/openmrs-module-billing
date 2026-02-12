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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.webservices.rest.web.RequestContext;

/**
 * Tests for {@link BillLineItemResource}
 */
public class BillLineItemResourceTest {
	
	private BillLineItemResource resource;
	
	private BillService billService;
	
	private User authenticatedUser;
	
	private MockedStatic<Context> contextMock;
	
	@Before
	public void setUp() {
		resource = new BillLineItemResource();
		billService = mock(BillService.class);
		authenticatedUser = mock(User.class);
		
		contextMock = mockStatic(Context.class);
		contextMock.when(() -> Context.getService(BillService.class)).thenReturn(billService);
		contextMock.when(() -> Context.getAuthenticatedUser()).thenReturn(authenticatedUser);
	}
	
	@After
	public void tearDown() {
		if (contextMock != null) {
			contextMock.close();
		}
	}
	
	/**
	 * @verifies void the line item and save the bill
	 * @see BillLineItemResource#delete(BillLineItem, String, RequestContext)
	 */
	@Test
	public void delete_shouldVoidTheLineItemAndSaveTheBill() {
		Bill bill = new Bill();
		bill.setId(1);
		
		BillLineItem lineItem = new BillLineItem();
		lineItem.setId(1);
		lineItem.setBill(bill);
		assertFalse("Line item should not be voided initially", lineItem.getVoided());
		assertNull("Void reason should be null initially", lineItem.getVoidReason());
		assertNull("Voided by should be null initially", lineItem.getVoidedBy());
		
		String reason = "Test deletion reason";
		RequestContext context = mock(RequestContext.class);
		
		Bill savedBill = new Bill();
		savedBill.setId(1);
		when(billService.saveBill(bill)).thenReturn(savedBill);
		
		resource.delete(lineItem, reason, context);
		
		assertTrue("Line item should be voided", lineItem.getVoided());
		assertNotNull("Void reason should be set", lineItem.getVoidReason());
		assertTrue("Void reason should match", reason.equals(lineItem.getVoidReason()));
		assertNotNull("Voided by should be set", lineItem.getVoidedBy());
		assertTrue("Voided by should be the authenticated user", authenticatedUser.equals(lineItem.getVoidedBy()));
		
		verify(billService).saveBill(bill);
	}
	
	/**
	 * @verifies handle null reason gracefully
	 * @see BillLineItemResource#delete(BillLineItem, String, RequestContext)
	 */
	@Test
	public void delete_shouldHandleNullReasonGracefully() {
		Bill bill = new Bill();
		bill.setId(1);
		
		BillLineItem lineItem = new BillLineItem();
		lineItem.setId(1);
		lineItem.setBill(bill);
		
		String reason = null;
		RequestContext context = mock(RequestContext.class);
		
		Bill savedBill = new Bill();
		savedBill.setId(1);
		when(billService.saveBill(bill)).thenReturn(savedBill);
		
		resource.delete(lineItem, reason, context);
		
		assertTrue("Line item should be voided", lineItem.getVoided());
		assertNull("Void reason should be null when reason is null", lineItem.getVoidReason());
		assertNotNull("Voided by should be set", lineItem.getVoidedBy());
		
		verify(billService).saveBill(bill);
	}
}
