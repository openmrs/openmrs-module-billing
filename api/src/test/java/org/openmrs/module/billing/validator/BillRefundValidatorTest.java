/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.ModuleSettings;
import org.openmrs.module.billing.api.BillRefundService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillRefund;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.RefundStatus;
import org.openmrs.module.billing.api.util.PrivilegeConstants;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

public class BillRefundValidatorTest {
	
	private BillRefundValidator validator;
	
	private MockedStatic<Context> contextMock;
	
	private AdministrationService adminService;
	
	private BillRefundService refundService;
	
	@BeforeEach
	public void setUp() {
		validator = new BillRefundValidator();
		adminService = mock(AdministrationService.class);
		refundService = mock(BillRefundService.class);
		
		contextMock = mockStatic(Context.class);
		contextMock.when(Context::getAdministrationService).thenReturn(adminService);
		contextMock.when(() -> Context.getService(BillRefundService.class)).thenReturn(refundService);
		when(adminService.getGlobalProperty(ModuleSettings.REFUND_ENABLED)).thenReturn("true");
		contextMock.when(() -> Context.hasPrivilege(any(String.class))).thenReturn(true);
	}
	
	@AfterEach
	public void tearDown() {
		if (contextMock != null) {
			contextMock.close();
		}
	}
	
	@Test
	public void shouldRejectVoidingFinalizedRefundWithoutApprovePrivilege() {
		contextMock.when(() -> Context.hasPrivilege(PrivilegeConstants.APPROVE_REFUNDS)).thenReturn(false);
		
		BillRefund refund = baseRefund();
		refund.setId(99);
		refund.setStatus(RefundStatus.APPROVED);
		refund.setVoided(true);
		
		Errors errors = new BeanPropertyBindingResult(refund, "billRefund");
		validator.validate(refund, errors);
		
		assertTrue(errors.hasErrors());
	}
	
	@Test
	public void shouldAllowVoidingFinalizedRefundWithApprovePrivilege() {
		contextMock.when(() -> Context.hasPrivilege(PrivilegeConstants.APPROVE_REFUNDS)).thenReturn(true);
		
		BillRefund refund = baseRefund();
		refund.setId(99);
		refund.setStatus(RefundStatus.APPROVED);
		refund.setVoided(true);
		refund.setVoidReason("Audit correction");
		
		Errors errors = new BeanPropertyBindingResult(refund, "billRefund");
		validator.validate(refund, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	@Test
	public void shouldAllowVoidingPendingRefundWithoutApprovePrivilege() {
		contextMock.when(() -> Context.hasPrivilege(PrivilegeConstants.APPROVE_REFUNDS)).thenReturn(false);
		
		BillRefund refund = baseRefund();
		refund.setId(99);
		refund.setStatus(RefundStatus.REQUESTED);
		refund.setVoided(true);
		refund.setVoidReason("Cancelling pending request");
		
		Errors errors = new BeanPropertyBindingResult(refund, "billRefund");
		validator.validate(refund, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	@Test
	public void shouldRejectStatusTransitionWithoutRequiredPrivilege() {
		when(refundService.getStatusById(99)).thenReturn(RefundStatus.REQUESTED);
		contextMock.when(() -> Context.hasPrivilege(PrivilegeConstants.APPROVE_REFUNDS)).thenReturn(false);
		contextMock.when(() -> Context.hasPrivilege(eq(PrivilegeConstants.COMPLETE_REFUNDS))).thenReturn(true);
		
		BillRefund refund = baseRefund();
		refund.setId(99);
		refund.setStatus(RefundStatus.APPROVED);
		refund.setApprover(new User());
		
		Errors errors = new BeanPropertyBindingResult(refund, "billRefund");
		validator.validate(refund, errors);
		
		assertTrue(errors.hasErrors());
	}
	
	@Test
	public void shouldRejectCompleteTransitionWithoutCompletePrivilege() {
		when(refundService.getStatusById(99)).thenReturn(RefundStatus.APPROVED);
		contextMock.when(() -> Context.hasPrivilege(PrivilegeConstants.APPROVE_REFUNDS)).thenReturn(true);
		contextMock.when(() -> Context.hasPrivilege(PrivilegeConstants.COMPLETE_REFUNDS)).thenReturn(false);
		
		BillRefund refund = baseRefund();
		refund.setId(99);
		refund.setStatus(RefundStatus.COMPLETED);
		refund.setCompleter(new User());
		
		Errors errors = new BeanPropertyBindingResult(refund, "billRefund");
		validator.validate(refund, errors);
		
		assertTrue(errors.hasErrors());
	}
	
	@Test
	public void shouldRejectLineItemFromDifferentBill() {
		Bill billA = bill(1, BillStatus.PAID, new BigDecimal("100.00"));
		Bill billB = bill(2, BillStatus.PAID, new BigDecimal("100.00"));
		
		BillLineItem lineOnB = lineItem(20, billB, new BigDecimal("50.00"));
		
		BillRefund refund = new BillRefund();
		refund.setBill(billA);
		refund.setLineItem(lineOnB);
		refund.setStatus(RefundStatus.REQUESTED);
		refund.setRefundAmount(new BigDecimal("10.00"));
		refund.setReason("Cross-bill spoofing");
		refund.setInitiator(new User());
		
		Errors errors = new BeanPropertyBindingResult(refund, "billRefund");
		validator.validate(refund, errors);
		
		assertTrue(errors.hasFieldErrors("lineItem"));
	}
	
	@Test
	public void shouldRejectVoidedLineItem() {
		Bill bill = bill(1, BillStatus.PAID, new BigDecimal("100.00"));
		BillLineItem voidedLine = lineItem(20, bill, new BigDecimal("50.00"));
		voidedLine.setVoided(true);
		
		BillRefund refund = new BillRefund();
		refund.setBill(bill);
		refund.setLineItem(voidedLine);
		refund.setStatus(RefundStatus.REQUESTED);
		refund.setRefundAmount(new BigDecimal("10.00"));
		refund.setReason("Refund a voided line");
		refund.setInitiator(new User());
		
		Errors errors = new BeanPropertyBindingResult(refund, "billRefund");
		validator.validate(refund, errors);
		
		assertTrue(errors.hasFieldErrors("lineItem"));
	}
	
	private BillRefund baseRefund() {
		BillRefund refund = new BillRefund();
		Bill bill = bill(1, BillStatus.REFUND_REQUESTED, new BigDecimal("100.00"));
		refund.setBill(bill);
		refund.setRefundAmount(new BigDecimal("10.00"));
		refund.setReason("Test");
		refund.setInitiator(new User());
		lenient().when(refundService.getStatusById(any(Integer.class))).thenReturn(RefundStatus.REQUESTED);
		return refund;
	}
	
	private Bill bill(int id, BillStatus status, BigDecimal lineTotal) {
		Bill b = new Bill();
		b.setId(id);
		b.setStatus(status);
		BillLineItem line = lineItem(100 + id, b, lineTotal);
		b.addLineItem(line);
		return b;
	}
	
	private BillLineItem lineItem(int id, Bill owner, BigDecimal price) {
		BillLineItem line = new BillLineItem();
		line.setId(id);
		line.setBill(owner);
		line.setPrice(price);
		line.setQuantity(1);
		line.setVoided(false);
		return line;
	}
}
