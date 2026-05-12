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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
	
	@BeforeEach
	public void setUp() {
		resource = new BillLineItemResource();
		billService = mock(BillService.class);
		authenticatedUser = mock(User.class);
		
		contextMock = mockStatic(Context.class);
		contextMock.when(() -> Context.getService(BillService.class)).thenReturn(billService);
		contextMock.when(() -> Context.getAuthenticatedUser()).thenReturn(authenticatedUser);
	}
	
	@AfterEach
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
		assertFalse(lineItem.getVoided(), "Line item should not be voided initially");
		assertNull(lineItem.getVoidReason(), "Void reason should be null initially");
		assertNull(lineItem.getVoidedBy(), "Voided by should be null initially");
		
		String reason = "Test deletion reason";
		RequestContext context = mock(RequestContext.class);
		
		Bill savedBill = new Bill();
		savedBill.setId(1);
		when(billService.saveBill(bill)).thenReturn(savedBill);
		
		resource.delete(lineItem, reason, context);
		
		assertTrue(lineItem.getVoided(), "Line item should be voided");
		assertNotNull(lineItem.getVoidReason(), "Void reason should be set");
		assertTrue(reason.equals(lineItem.getVoidReason()), "Void reason should match");
		assertNotNull(lineItem.getVoidedBy(), "Voided by should be set");
		assertTrue(authenticatedUser.equals(lineItem.getVoidedBy()), "Voided by should be the authenticated user");
		
		verify(billService).saveBill(bill);
	}
	
	/**
	 * @verifies throw IllegalArgumentException when reason is null or blank
	 * @see BillLineItemResource#delete(BillLineItem, String, RequestContext)
	 */
	@Test
	public void delete_shouldThrowExceptionWhenReasonIsNull() {
		Bill bill = new Bill();
		bill.setId(1);
		
		BillLineItem lineItem = new BillLineItem();
		lineItem.setId(1);
		lineItem.setBill(bill);
		
		String reason = null;
		RequestContext context = mock(RequestContext.class);
		
		assertThrows(IllegalArgumentException.class, () -> resource.delete(lineItem, reason, context));
	}
}
