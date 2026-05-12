/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.ModuleSettings;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.BillLineItemService;
import org.openmrs.module.billing.api.BillRefundService;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillLineItemStatus;
import org.openmrs.module.billing.api.model.BillRefund;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.RefundStatus;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BillRefundServiceImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String CLEAN_PAID_BILL_UUID = "r0000000-0000-0000-0000-000000000300";
	
	private static final String CLEAN_PAID_LINE_ITEM_UUID = "r0000000-0000-0000-0000-000000000310";
	
	private static final String MULTILINE_PAID_BILL_UUID = "r0000000-0000-0000-0000-000000000301";
	
	private static final String MULTILINE_PAID_LINE_ITEM_UUID = "r0000000-0000-0000-0000-000000000311";
	
	private static final String BILL_WITH_ACTIVE_BILL_REFUND_UUID = "r0000000-0000-0000-0000-000000000302";
	
	private static final String ACTIVE_BILL_REFUND_UUID = "r1000000-0000-0000-0000-000000000001";
	
	private static final String BILL_WITH_ACTIVE_LINE_REFUND_UUID = "r0000000-0000-0000-0000-000000000303";
	
	private static final String LINE_ITEM_WITH_REFUND_UUID = "r0000000-0000-0000-0000-000000000330";
	
	private static final String FREE_LINE_ITEM_UUID = "r0000000-0000-0000-0000-000000000331";
	
	private static final String VOIDED_HISTORY_BILL_UUID = "r0000000-0000-0000-0000-000000000304";
	
	private static final String PENDING_BILL_UUID = "6028814B39B565A20139B95D74360004";
	
	private static final String OTHER_PAID_BILL_UUID = "5028814B39B565A20139B95D74360004";
	
	private BillRefundService service;
	
	private BillService billService;
	
	private BillLineItemService lineItemService;
	
	@BeforeEach
	public void setup() throws Exception {
		service = Context.getService(BillRefundService.class);
		billService = Context.getService(BillService.class);
		lineItemService = Context.getService(BillLineItemService.class);
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "StockOperationType.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "PaymentModeTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillRefundTest.xml");
	}
	
	@Test
	public void saveBillRefund_shouldPersistValidRefundOnPaidBill() {
		BillRefund refund = buildRefund(CLEAN_PAID_BILL_UUID, new BigDecimal("100.00"), "Equipment failure");
		
		BillRefund saved = service.saveBillRefund(refund);
		
		assertNotNull(saved);
		assertNotNull(saved.getBillRefundId());
		assertEquals(RefundStatus.REQUESTED, saved.getStatus());
	}
	
	@Test
	public void saveBillRefund_shouldFlipBillToRefundRequestedOnCreate() {
		BillRefund refund = buildRefund(CLEAN_PAID_BILL_UUID, new BigDecimal("100.00"), "Equipment failure");
		
		service.saveBillRefund(refund);
		
		Bill bill = billService.getBillByUuid(CLEAN_PAID_BILL_UUID);
		assertEquals(BillStatus.REFUND_REQUESTED, bill.getStatus());
	}
	
	@Test
	public void saveBillRefund_shouldRejectWhenFeatureDisabled() {
		Context.getAdministrationService().setGlobalProperty(ModuleSettings.REFUND_ENABLED, "false");
		BillRefund refund = buildRefund(CLEAN_PAID_BILL_UUID, new BigDecimal("10.00"), "any");
		
		assertThrows(Exception.class, () -> service.saveBillRefund(refund));
	}
	
	@Test
	public void saveBillRefund_shouldRejectRefundOnPendingBill() {
		BillRefund refund = buildRefund(PENDING_BILL_UUID, new BigDecimal("10.00"), "Too early");
		
		assertThrows(Exception.class, () -> service.saveBillRefund(refund));
	}
	
	@Test
	public void saveBillRefund_shouldRejectWithoutReason() {
		BillRefund refund = buildRefund(CLEAN_PAID_BILL_UUID, new BigDecimal("10.00"), "");
		
		assertThrows(Exception.class, () -> service.saveBillRefund(refund));
	}
	
	@Test
	public void saveBillRefund_shouldRejectNonPositiveAmount() {
		BillRefund refund = buildRefund(CLEAN_PAID_BILL_UUID, BigDecimal.ZERO, "Zero amount");
		
		assertThrows(Exception.class, () -> service.saveBillRefund(refund));
	}
	
	@Test
	public void saveBillRefund_shouldRejectAmountExceedingBillTotal() {
		BillRefund refund = buildRefund(CLEAN_PAID_BILL_UUID, new BigDecimal("200.00"), "Too much");
		
		assertThrows(Exception.class, () -> service.saveBillRefund(refund));
	}
	
	@Test
	public void saveBillRefund_shouldAllowAmountEqualToBillTotal() {
		BillRefund refund = buildRefund(CLEAN_PAID_BILL_UUID, new BigDecimal("100.00"), "Full refund");
		
		BillRefund saved = service.saveBillRefund(refund);
		
		assertNotNull(saved.getBillRefundId());
	}
	
	@Test
	public void saveBillRefund_shouldRejectSecondActiveBillLevelRefund() {
		BillRefund refund = buildRefund(BILL_WITH_ACTIVE_BILL_REFUND_UUID, new BigDecimal("10.00"), "Duplicate");
		
		assertThrows(Exception.class, () -> service.saveBillRefund(refund));
	}
	
	@Test
	public void saveBillRefund_shouldRejectBillLevelWhenLineScopedExists() {
		BillRefund refund = buildRefund(BILL_WITH_ACTIVE_LINE_REFUND_UUID, new BigDecimal("10.00"), "Should be rejected");
		
		assertThrows(Exception.class, () -> service.saveBillRefund(refund));
	}
	
	@Test
	public void saveBillRefund_shouldRejectLineScopedWhenBillLevelExists() {
		BillRefund refund = buildLineScopedRefund(BILL_WITH_ACTIVE_BILL_REFUND_UUID, "r0000000-0000-0000-0000-000000000320",
		    new BigDecimal("10.00"), "Should be rejected");
		
		assertThrows(Exception.class, () -> service.saveBillRefund(refund));
	}
	
	@Test
	public void saveBillRefund_shouldPersistLineScopedRefund() {
		BillRefund refund = buildLineScopedRefund(BILL_WITH_ACTIVE_LINE_REFUND_UUID, FREE_LINE_ITEM_UUID,
		    new BigDecimal("30.00"), "Specific line refund");
		
		BillRefund saved = service.saveBillRefund(refund);
		
		assertNotNull(saved);
		assertNotNull(saved.getLineItem());
		assertEquals(FREE_LINE_ITEM_UUID, saved.getLineItem().getUuid());
	}
	
	@Test
	public void saveBillRefund_shouldRejectAmountExceedingLineItemTotal() {
		BillRefund refund = buildLineScopedRefund(BILL_WITH_ACTIVE_LINE_REFUND_UUID, FREE_LINE_ITEM_UUID,
		    new BigDecimal("100.00"), "Exceeds line total");
		
		assertThrows(Exception.class, () -> service.saveBillRefund(refund));
	}
	
	@Test
	public void saveBillRefund_shouldRejectSecondLineScopedRefundOnSameLineItem() {
		BillRefund refund = buildLineScopedRefund(BILL_WITH_ACTIVE_LINE_REFUND_UUID, LINE_ITEM_WITH_REFUND_UUID,
		    new BigDecimal("10.00"), "Duplicate");
		
		assertThrows(Exception.class, () -> service.saveBillRefund(refund));
	}
	
	@Test
	public void saveBillRefund_shouldRejectInitialStatusOtherThanRequested() {
		BillRefund refund = buildRefund(CLEAN_PAID_BILL_UUID, new BigDecimal("10.00"), "Bypass");
		refund.setStatus(RefundStatus.APPROVED);
		refund.setApprover(Context.getUserService().getUser(5506));
		
		assertThrows(Exception.class, () -> service.saveBillRefund(refund));
	}
	
	@Test
	public void saveBillRefund_shouldFlipBillToRefundedOnApproveOfBillLevel() {
		BillRefund refund = service.getBillRefundByUuid(ACTIVE_BILL_REFUND_UUID);
		assertNotNull(refund);
		
		refund.setStatus(RefundStatus.APPROVED);
		refund.setApprover(Context.getUserService().getUser(5506));
		
		service.saveBillRefund(refund);
		
		Bill bill = billService.getBillByUuid(BILL_WITH_ACTIVE_BILL_REFUND_UUID);
		assertEquals(BillStatus.REFUNDED, bill.getStatus());
		assertNotNull(refund.getDateApproved());
	}
	
	@Test
	public void saveBillRefund_shouldAllowSecondBillLevelRefundAfterFirstCompleted() {
		BillRefund first = buildRefund(CLEAN_PAID_BILL_UUID, new BigDecimal("40.00"), "First partial refund");
		BillRefund firstSaved = service.saveBillRefund(first);
		firstSaved.setStatus(RefundStatus.APPROVED);
		firstSaved.setApprover(Context.getUserService().getUser(5506));
		service.saveBillRefund(firstSaved);
		BillRefund approvedFirst = service.getBillRefundByUuid(firstSaved.getUuid());
		approvedFirst.setStatus(RefundStatus.COMPLETED);
		approvedFirst.setCompleter(Context.getUserService().getUser(5506));
		service.saveBillRefund(approvedFirst);
		
		BillRefund second = buildRefund(CLEAN_PAID_BILL_UUID, new BigDecimal("30.00"), "Second partial refund");
		
		BillRefund saved = service.saveBillRefund(second);
		
		assertNotNull(saved.getBillRefundId());
		assertEquals(RefundStatus.REQUESTED, saved.getStatus());
	}
	
	@Test
	public void saveBillRefund_shouldRejectExceedingRemainingAfterCompletedRefund() {
		BillRefund first = buildRefund(CLEAN_PAID_BILL_UUID, new BigDecimal("70.00"), "First partial refund");
		BillRefund firstSaved = service.saveBillRefund(first);
		firstSaved.setStatus(RefundStatus.APPROVED);
		firstSaved.setApprover(Context.getUserService().getUser(5506));
		service.saveBillRefund(firstSaved);
		BillRefund approvedFirst = service.getBillRefundByUuid(firstSaved.getUuid());
		approvedFirst.setStatus(RefundStatus.COMPLETED);
		approvedFirst.setCompleter(Context.getUserService().getUser(5506));
		service.saveBillRefund(approvedFirst);
		
		BillRefund second = buildRefund(CLEAN_PAID_BILL_UUID, new BigDecimal("40.00"), "Over remaining");
		
		assertThrows(Exception.class, () -> service.saveBillRefund(second));
	}
	
	@Test
	public void saveBillRefund_shouldFlipBillToPartiallyRefundedOnApprovingPartialBillLevelRefund() {
		BillRefund partial = buildRefund(CLEAN_PAID_BILL_UUID, new BigDecimal("40.00"), "Partial bill-level refund");
		BillRefund saved = service.saveBillRefund(partial);
		
		saved.setStatus(RefundStatus.APPROVED);
		saved.setApprover(Context.getUserService().getUser(5506));
		service.saveBillRefund(saved);
		
		Bill bill = billService.getBillByUuid(CLEAN_PAID_BILL_UUID);
		assertEquals(BillStatus.PARTIALLY_REFUNDED, bill.getStatus());
	}
	
	@Test
	public void saveBillRefund_shouldFlipBillToPartiallyRefundedOnApproveOfLineScoped() {
		BillRefund refund = service.getBillRefundByUuid("r1000000-0000-0000-0000-000000000003");
		assertNotNull(refund);
		
		refund.setStatus(RefundStatus.APPROVED);
		refund.setApprover(Context.getUserService().getUser(5506));
		
		service.saveBillRefund(refund);
		
		Bill bill = billService.getBillByUuid(BILL_WITH_ACTIVE_LINE_REFUND_UUID);
		assertEquals(BillStatus.PARTIALLY_REFUNDED, bill.getStatus());
	}
	
	@Test
	public void saveBillRefund_shouldFlipBillBackToPaidOnReject() {
		BillRefund refund = service.getBillRefundByUuid(ACTIVE_BILL_REFUND_UUID);
		assertNotNull(refund);
		
		refund.setStatus(RefundStatus.REJECTED);
		refund.setApprover(Context.getUserService().getUser(5506));
		
		service.saveBillRefund(refund);
		
		Bill bill = billService.getBillByUuid(BILL_WITH_ACTIVE_BILL_REFUND_UUID);
		assertEquals(BillStatus.PAID, bill.getStatus());
		assertNotNull(refund.getDateApproved());
	}
	
	@Test
	public void saveBillRefund_shouldRejectApproveWithoutApprover() {
		BillRefund refund = service.getBillRefundByUuid(ACTIVE_BILL_REFUND_UUID);
		assertNotNull(refund);
		refund.setStatus(RefundStatus.APPROVED);
		
		assertThrows(Exception.class, () -> service.saveBillRefund(refund));
	}
	
	@Test
	public void saveBillRefund_shouldRejectInvalidTransitionRequestedToCompleted() {
		BillRefund refund = service.getBillRefundByUuid(ACTIVE_BILL_REFUND_UUID);
		assertNotNull(refund);
		refund.setStatus(RefundStatus.COMPLETED);
		refund.setCompleter(Context.getUserService().getUser(5506));
		
		assertThrows(Exception.class, () -> service.saveBillRefund(refund));
	}
	
	@Test
	public void saveBillRefund_shouldRejectInvalidTransitionApprovedToRequested() {
		BillRefund refund = service.getBillRefundByUuid(ACTIVE_BILL_REFUND_UUID);
		refund.setStatus(RefundStatus.APPROVED);
		refund.setApprover(Context.getUserService().getUser(5506));
		service.saveBillRefund(refund);
		
		BillRefund reloaded = service.getBillRefundByUuid(ACTIVE_BILL_REFUND_UUID);
		reloaded.setStatus(RefundStatus.REQUESTED);
		
		assertThrows(Exception.class, () -> service.saveBillRefund(reloaded));
	}
	
	@Test
	public void saveBillRefund_shouldRejectInvalidTransitionRejectedToApproved() {
		BillRefund refund = service.getBillRefundByUuid(ACTIVE_BILL_REFUND_UUID);
		refund.setStatus(RefundStatus.REJECTED);
		refund.setApprover(Context.getUserService().getUser(5506));
		service.saveBillRefund(refund);
		
		BillRefund reloaded = service.getBillRefundByUuid(ACTIVE_BILL_REFUND_UUID);
		reloaded.setStatus(RefundStatus.APPROVED);
		
		assertThrows(Exception.class, () -> service.saveBillRefund(reloaded));
	}
	
	@Test
	public void saveBillRefund_shouldRejectInvalidTransitionCompletedToAnything() {
		BillRefund refund = service.getBillRefundByUuid(ACTIVE_BILL_REFUND_UUID);
		refund.setStatus(RefundStatus.APPROVED);
		refund.setApprover(Context.getUserService().getUser(5506));
		service.saveBillRefund(refund);
		
		BillRefund approved = service.getBillRefundByUuid(ACTIVE_BILL_REFUND_UUID);
		approved.setStatus(RefundStatus.COMPLETED);
		approved.setCompleter(Context.getUserService().getUser(5506));
		service.saveBillRefund(approved);
		
		BillRefund completed = service.getBillRefundByUuid(ACTIVE_BILL_REFUND_UUID);
		completed.setStatus(RefundStatus.REJECTED);
		
		assertThrows(Exception.class, () -> service.saveBillRefund(completed));
	}
	
	@Test
	public void saveBillRefund_shouldStampDateCompletedOnComplete() {
		BillRefund refund = service.getBillRefundByUuid(ACTIVE_BILL_REFUND_UUID);
		refund.setStatus(RefundStatus.APPROVED);
		refund.setApprover(Context.getUserService().getUser(5506));
		service.saveBillRefund(refund);
		
		BillRefund reloaded = service.getBillRefundByUuid(ACTIVE_BILL_REFUND_UUID);
		reloaded.setStatus(RefundStatus.COMPLETED);
		reloaded.setCompleter(Context.getUserService().getUser(5506));
		
		BillRefund completed = service.saveBillRefund(reloaded);
		
		assertEquals(RefundStatus.COMPLETED, completed.getStatus());
		assertNotNull(completed.getDateCompleted());
		
		Bill bill = billService.getBillByUuid(BILL_WITH_ACTIVE_BILL_REFUND_UUID);
		assertEquals(BillStatus.REFUNDED, bill.getStatus());
	}
	
	@Test
	public void saveBillRefund_voidingApprovedRefundShouldFlowThroughValidator() {
		BillRefund refund = service.getBillRefundByUuid(ACTIVE_BILL_REFUND_UUID);
		refund.setStatus(RefundStatus.APPROVED);
		refund.setApprover(Context.getUserService().getUser(5506));
		service.saveBillRefund(refund);
		
		BillRefund approved = service.getBillRefundByUuid(ACTIVE_BILL_REFUND_UUID);
		approved.setVoided(true);
		approved.setVoidReason("Voiding finalized");
		
		BillRefund voided = service.saveBillRefund(approved);
		
		assertTrue(voided.getVoided());
		Bill bill = billService.getBillByUuid(BILL_WITH_ACTIVE_BILL_REFUND_UUID);
		assertEquals(BillStatus.PAID, bill.getStatus());
	}
	
	@Test
	public void saveBillRefund_shouldRejectAmountWithMoreThanTwoDecimals() {
		BillRefund refund = buildRefund(CLEAN_PAID_BILL_UUID, new BigDecimal("10.999"), "Fractional cents");
		
		assertThrows(Exception.class, () -> service.saveBillRefund(refund));
	}
	
	@Test
	public void saveBillRefund_shouldRejectExceedingRemainingRefundableAcrossMultipleLines() {
		BillRefund first = buildLineScopedRefund(MULTILINE_PAID_BILL_UUID, MULTILINE_PAID_LINE_ITEM_UUID,
		    new BigDecimal("60.00"), "Refund line 1");
		BillRefund firstSaved = service.saveBillRefund(first);
		firstSaved.setStatus(RefundStatus.APPROVED);
		firstSaved.setApprover(Context.getUserService().getUser(5506));
		service.saveBillRefund(firstSaved);
		
		BillRefund second = buildLineScopedRefund(MULTILINE_PAID_BILL_UUID, "r0000000-0000-0000-0000-000000000312",
		    new BigDecimal("50.00"), "Over remaining refundable");
		
		assertThrows(Exception.class, () -> service.saveBillRefund(second));
	}
	
	@Test
	public void saveBillRefund_shouldRevertBillToPaidWhenOnlyActiveRefundIsVoided() {
		BillRefund refund = service.getBillRefundByUuid(ACTIVE_BILL_REFUND_UUID);
		refund.setVoided(true);
		refund.setVoidReason("Mistakenly raised");
		
		service.saveBillRefund(refund);
		
		Bill bill = billService.getBillByUuid(BILL_WITH_ACTIVE_BILL_REFUND_UUID);
		assertEquals(BillStatus.PAID, bill.getStatus());
	}
	
	@Test
	public void saveBillRefund_shouldKeepBillInRefundRequestedWhenCompletingOneLineRefundWhileSiblingPending() {
		BillRefund secondLineRefund = buildLineScopedRefund(BILL_WITH_ACTIVE_LINE_REFUND_UUID, FREE_LINE_ITEM_UUID,
		    new BigDecimal("20.00"), "Second line refund");
		service.saveBillRefund(secondLineRefund);
		
		BillRefund toComplete = service.getBillRefundByUuid("r1000000-0000-0000-0000-000000000003");
		toComplete.setStatus(RefundStatus.APPROVED);
		toComplete.setApprover(Context.getUserService().getUser(5506));
		service.saveBillRefund(toComplete);
		
		BillRefund approved = service.getBillRefundByUuid("r1000000-0000-0000-0000-000000000003");
		approved.setStatus(RefundStatus.COMPLETED);
		approved.setCompleter(Context.getUserService().getUser(5506));
		service.saveBillRefund(approved);
		
		Bill bill = billService.getBillByUuid(BILL_WITH_ACTIVE_LINE_REFUND_UUID);
		assertEquals(BillStatus.REFUND_REQUESTED, bill.getStatus());
	}
	
	@Test
	public void saveBillRefund_shouldKeepBillInRefundRequestedWhenSiblingLineRefundStillPending() {
		BillRefund existingLineRefund = service.getBillRefundByUuid("r1000000-0000-0000-0000-000000000003");
		assertNotNull(existingLineRefund);
		
		BillRefund secondLineRefund = buildLineScopedRefund(BILL_WITH_ACTIVE_LINE_REFUND_UUID, FREE_LINE_ITEM_UUID,
		    new BigDecimal("20.00"), "Second line refund");
		BillRefund secondSaved = service.saveBillRefund(secondLineRefund);
		assertNotNull(secondSaved.getBillRefundId());
		
		BillRefund toReject = service.getBillRefundByUuid("r1000000-0000-0000-0000-000000000003");
		toReject.setStatus(RefundStatus.REJECTED);
		toReject.setApprover(Context.getUserService().getUser(5506));
		service.saveBillRefund(toReject);
		
		Bill bill = billService.getBillByUuid(BILL_WITH_ACTIVE_LINE_REFUND_UUID);
		assertEquals(BillStatus.REFUND_REQUESTED, bill.getStatus());
	}
	
	@Test
	public void getActiveBillRefund_shouldIgnoreVoidedRefund() {
		Bill bill = billService.getBillByUuid(VOIDED_HISTORY_BILL_UUID);
		assertNull(service.getActiveBillRefund(bill.getId()));
	}
	
	@Test
	public void saveBillRefund_shouldAllowReapplyingAfterPreviousRefundVoided() {
		BillRefund refund = buildRefund(VOIDED_HISTORY_BILL_UUID, new BigDecimal("40.00"), "Re-applying");
		
		BillRefund saved = service.saveBillRefund(refund);
		
		assertNotNull(saved);
		assertNotNull(saved.getBillRefundId());
	}
	
	@Test
	public void getRefundsByBillId_shouldReturnFullAuditHistory() {
		Bill bill = billService.getBillByUuid(BILL_WITH_ACTIVE_BILL_REFUND_UUID);
		assertNotNull(bill);
		
		List<BillRefund> history = service.getRefundsByBillId(bill.getId());
		
		assertNotNull(history);
		assertEquals(1, history.size());
		assertFalse(history.get(0).getVoided());
	}
	
	@Test
	public void getActiveLineItemRefund_shouldReturnRefundForLineItem() {
		BillLineItem lineItem = lineItemService.getBillLineItemByUuid(LINE_ITEM_WITH_REFUND_UUID);
		assertNotNull(lineItem);
		
		BillRefund refund = service.getActiveLineItemRefund(lineItem.getId());
		
		assertNotNull(refund);
		assertEquals(lineItem.getId(), refund.getLineItem().getId());
	}
	
	@Test
	public void saveBillRefund_shouldAllowReSavingExistingRefundWithoutFalsePositiveDuplicate() {
		BillRefund existing = service.getBillRefundByUuid(ACTIVE_BILL_REFUND_UUID);
		assertNotNull(existing);
		
		existing.setReason("Updated reason");
		
		BillRefund saved = service.saveBillRefund(existing);
		
		assertNotNull(saved);
		assertEquals("Updated reason", saved.getReason());
	}
	
	@Test
	public void saveBillRefund_shouldFlipLineItemToRefundRequestedOnCreate() {
		BillRefund refund = buildLineScopedRefund(MULTILINE_PAID_BILL_UUID, MULTILINE_PAID_LINE_ITEM_UUID,
		    new BigDecimal("30.00"), "Partial line refund");
		
		service.saveBillRefund(refund);
		
		BillLineItem lineItem = lineItemService.getBillLineItemByUuid(MULTILINE_PAID_LINE_ITEM_UUID);
		assertEquals(BillLineItemStatus.REFUND_REQUESTED, lineItem.getStatus());
	}
	
	@Test
	public void saveBillRefund_shouldFlipLineItemToPartiallyRefundedOnApprovingPartialLineRefund() {
		BillRefund refund = buildLineScopedRefund(MULTILINE_PAID_BILL_UUID, MULTILINE_PAID_LINE_ITEM_UUID,
		    new BigDecimal("30.00"), "Partial line refund");
		BillRefund saved = service.saveBillRefund(refund);
		
		saved.setStatus(RefundStatus.APPROVED);
		saved.setApprover(Context.getUserService().getUser(5506));
		service.saveBillRefund(saved);
		
		BillLineItem lineItem = lineItemService.getBillLineItemByUuid(MULTILINE_PAID_LINE_ITEM_UUID);
		assertEquals(BillLineItemStatus.PARTIALLY_REFUNDED, lineItem.getStatus());
	}
	
	@Test
	public void saveBillRefund_shouldFlipLineItemToRefundedOnApprovingFullLineAmount() {
		BillRefund refund = buildLineScopedRefund(MULTILINE_PAID_BILL_UUID, MULTILINE_PAID_LINE_ITEM_UUID,
		    new BigDecimal("60.00"), "Full line refund");
		BillRefund saved = service.saveBillRefund(refund);
		
		saved.setStatus(RefundStatus.APPROVED);
		saved.setApprover(Context.getUserService().getUser(5506));
		service.saveBillRefund(saved);
		
		BillLineItem lineItem = lineItemService.getBillLineItemByUuid(MULTILINE_PAID_LINE_ITEM_UUID);
		assertEquals(BillLineItemStatus.REFUNDED, lineItem.getStatus());
	}
	
	@Test
	public void saveBillRefund_shouldRevertLineItemToPaidWhenApprovedLineScopedRefundIsVoided() {
		BillRefund refund = buildLineScopedRefund(MULTILINE_PAID_BILL_UUID, MULTILINE_PAID_LINE_ITEM_UUID,
		    new BigDecimal("60.00"), "Full line refund");
		BillRefund saved = service.saveBillRefund(refund);
		
		saved.setStatus(RefundStatus.APPROVED);
		saved.setApprover(Context.getUserService().getUser(5506));
		service.saveBillRefund(saved);
		
		BillRefund approved = service.getBillRefundByUuid(saved.getUuid());
		approved.setVoided(true);
		approved.setVoidReason("Mistakenly raised");
		service.saveBillRefund(approved);
		
		BillLineItem lineItem = lineItemService.getBillLineItemByUuid(MULTILINE_PAID_LINE_ITEM_UUID);
		assertEquals(BillLineItemStatus.PAID, lineItem.getStatus());
	}
	
	@Test
	public void saveBillRefund_billScopedRefundShouldNotChangeLineItemStatus() {
		BillRefund refund = buildRefund(CLEAN_PAID_BILL_UUID, new BigDecimal("40.00"), "Partial bill-level refund");
		BillRefund saved = service.saveBillRefund(refund);
		
		saved.setStatus(RefundStatus.APPROVED);
		saved.setApprover(Context.getUserService().getUser(5506));
		service.saveBillRefund(saved);
		
		BillLineItem lineItem = lineItemService.getBillLineItemByUuid(CLEAN_PAID_LINE_ITEM_UUID);
		assertEquals(BillLineItemStatus.PAID, lineItem.getStatus());
	}
	
	private BillRefund buildRefund(String billUuid, BigDecimal amount, String reason) {
		Bill bill = billService.getBillByUuid(billUuid);
		assertNotNull(bill, "Test dataset missing bill: " + billUuid);
		
		BillRefund refund = new BillRefund();
		refund.setBill(bill);
		refund.setRefundAmount(amount);
		refund.setReason(reason);
		refund.setInitiator(Context.getAuthenticatedUser());
		refund.setStatus(RefundStatus.REQUESTED);
		return refund;
	}
	
	private BillRefund buildLineScopedRefund(String billUuid, String lineItemUuid, BigDecimal amount, String reason) {
		BillRefund refund = buildRefund(billUuid, amount, reason);
		BillLineItem lineItem = lineItemService.getBillLineItemByUuid(lineItemUuid);
		assertNotNull(lineItem, "Test dataset missing line item: " + lineItemUuid);
		refund.setLineItem(lineItem);
		return refund;
	}
}
