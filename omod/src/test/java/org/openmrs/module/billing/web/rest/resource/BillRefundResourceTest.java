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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillLineItemService;
import org.openmrs.module.billing.api.BillRefundService;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillRefund;
import org.openmrs.module.billing.api.model.RefundStatus;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;

public class BillRefundResourceTest {
	
	private BillRefundResource resource;
	
	private BillRefundService refundService;
	
	private BillService billService;
	
	private BillLineItemService lineItemService;
	
	private UserService userService;
	
	private MockedStatic<Context> contextMock;
	
	@BeforeEach
	public void setUp() {
		resource = new BillRefundResource();
		refundService = mock(BillRefundService.class);
		billService = mock(BillService.class);
		lineItemService = mock(BillLineItemService.class);
		userService = mock(UserService.class);
		
		contextMock = mockStatic(Context.class);
		contextMock.when(() -> Context.getService(BillRefundService.class)).thenReturn(refundService);
		contextMock.when(() -> Context.getService(BillService.class)).thenReturn(billService);
		contextMock.when(() -> Context.getService(BillLineItemService.class)).thenReturn(lineItemService);
		contextMock.when(Context::getUserService).thenReturn(userService);
	}
	
	@AfterEach
	public void tearDown() {
		if (contextMock != null) {
			contextMock.close();
		}
	}
	
	@Test
	public void save_shouldSetInitiatorFromAuthenticatedUserOnCreate() {
		User authUser = new User();
		authUser.setId(7);
		contextMock.when(Context::getAuthenticatedUser).thenReturn(authUser);
		
		BillRefund refund = new BillRefund();
		refund.setRefundAmount(BigDecimal.TEN);
		
		when(refundService.saveBillRefund(refund)).thenReturn(refund);
		
		resource.save(refund);
		
		assertSame(authUser, refund.getInitiator(), "Initiator should default to the authenticated user");
		verify(refundService).saveBillRefund(refund);
	}
	
	@Test
	public void save_shouldNotOverrideInitiatorOnUpdate() {
		User existingInitiator = new User();
		existingInitiator.setId(3);
		User authUser = new User();
		authUser.setId(7);
		contextMock.when(Context::getAuthenticatedUser).thenReturn(authUser);
		
		BillRefund refund = new BillRefund();
		refund.setId(42);
		refund.setInitiator(existingInitiator);
		
		when(refundService.saveBillRefund(refund)).thenReturn(refund);
		
		resource.save(refund);
		
		assertSame(existingInitiator, refund.getInitiator(), "Existing initiator must not be overwritten on update");
	}
	
	@Test
	public void save_shouldNotOverrideClientProvidedInitiatorOnCreate() {
		User clientInitiator = new User();
		clientInitiator.setId(11);
		User authUser = new User();
		authUser.setId(7);
		contextMock.when(Context::getAuthenticatedUser).thenReturn(authUser);
		
		BillRefund refund = new BillRefund();
		refund.setInitiator(clientInitiator);
		
		when(refundService.saveBillRefund(refund)).thenReturn(refund);
		
		resource.save(refund);
		
		assertSame(clientInitiator, refund.getInitiator(),
		    "Client-provided initiator should not be replaced by the authenticated user");
	}
	
	@Test
	public void delete_shouldVoidUnvoidedRefund() {
		BillRefund refund = new BillRefund();
		refund.setVoided(false);
		
		resource.delete(refund, "Bad request", null);
		
		assertTrue(refund.getVoided());
		assertEquals("Bad request", refund.getVoidReason());
		verify(refundService).saveBillRefund(refund);
	}
	
	@Test
	public void delete_shouldNotResaveAlreadyVoidedRefund() {
		BillRefund refund = new BillRefund();
		refund.setVoided(true);
		
		resource.delete(refund, "Already voided", null);
		
		verify(refundService, never()).saveBillRefund(refund);
	}
	
	@Test
	public void purge_shouldThrowUnsupported() {
		assertThrows(UnsupportedOperationException.class, () -> resource.purge(new BillRefund(), null));
	}
	
	@Test
	public void setBill_shouldThrowWhenBillNotFound() {
		BillRefund refund = new BillRefund();
		when(billService.getBillByUuid("missing")).thenReturn(null);
		
		assertThrows(ObjectNotFoundException.class, () -> resource.setBill(refund, "missing"));
	}
	
	@Test
	public void setBill_shouldAttachExistingBill() {
		Bill bill = new Bill();
		bill.setId(5);
		when(billService.getBillByUuid("good")).thenReturn(bill);
		
		BillRefund refund = new BillRefund();
		resource.setBill(refund, "good");
		
		assertSame(bill, refund.getBill());
	}
	
	@Test
	public void setLineItem_shouldThrowWhenLineItemNotFound() {
		BillRefund refund = new BillRefund();
		when(lineItemService.getBillLineItemByUuid("missing")).thenReturn(null);
		
		assertThrows(ObjectNotFoundException.class, () -> resource.setLineItem(refund, "missing"));
	}
	
	@Test
	public void setApprover_shouldThrowWhenUserNotFound() {
		BillRefund refund = new BillRefund();
		when(userService.getUserByUuid("missing")).thenReturn(null);
		
		assertThrows(ObjectNotFoundException.class, () -> resource.setApprover(refund, "missing"));
	}
	
	@Test
	public void setCompleter_shouldThrowWhenUserNotFound() {
		BillRefund refund = new BillRefund();
		when(userService.getUserByUuid("missing")).thenReturn(null);
		
		assertThrows(ObjectNotFoundException.class, () -> resource.setCompleter(refund, "missing"));
	}
	
	@Test
	public void setRefundAmount_shouldAcceptIntegerLiteral() {
		BillRefund refund = new BillRefund();
		resource.setRefundAmount(refund, 25);
		assertEquals(0, new BigDecimal("25").compareTo(refund.getRefundAmount()));
	}
	
	@Test
	public void setRefundAmount_shouldAcceptDoubleLiteral() {
		BillRefund refund = new BillRefund();
		resource.setRefundAmount(refund, 12.5);
		assertEquals(0, new BigDecimal("12.5").compareTo(refund.getRefundAmount()));
	}
	
	@Test
	public void setRefundAmount_shouldAcceptNumericString() {
		BillRefund refund = new BillRefund();
		resource.setRefundAmount(refund, "33.33");
		assertEquals(0, new BigDecimal("33.33").compareTo(refund.getRefundAmount()));
	}
	
	@Test
	public void setRefundAmount_shouldThrowConversionExceptionForNonNumeric() {
		BillRefund refund = new BillRefund();
		assertThrows(ConversionException.class, () -> resource.setRefundAmount(refund, "not-a-number"));
	}
	
	@Test
	public void setRefundAmount_shouldAcceptNull() {
		BillRefund refund = new BillRefund();
		refund.setRefundAmount(BigDecimal.TEN);
		resource.setRefundAmount(refund, null);
		assertNull(refund.getRefundAmount());
	}
	
	@Test
	public void setStatus_shouldParseEnumValue() {
		BillRefund refund = new BillRefund();
		resource.setStatus(refund, "APPROVED");
		assertEquals(RefundStatus.APPROVED, refund.getStatus());
	}
	
	@Test
	public void getBillUuid_shouldReturnNullWhenNoBill() {
		assertNull(resource.getBillUuid(new BillRefund()));
	}
	
	@Test
	public void getBillUuid_shouldReturnBillUuid() {
		Bill bill = new Bill();
		bill.setUuid("bill-uuid-123");
		BillRefund refund = new BillRefund();
		refund.setBill(bill);
		
		assertEquals("bill-uuid-123", resource.getBillUuid(refund));
	}
	
	@Test
	public void getLineItemUuid_shouldReturnLineItemUuid() {
		BillLineItem lineItem = new BillLineItem();
		lineItem.setUuid("line-uuid-456");
		BillRefund refund = new BillRefund();
		refund.setLineItem(lineItem);
		
		assertEquals("line-uuid-456", resource.getLineItemUuid(refund));
	}
	
	@Test
	public void newDelegate_shouldReturnFreshInstance() {
		BillRefund delegate = resource.newDelegate();
		assertNotNull(delegate);
		assertNull(delegate.getId());
	}
}
