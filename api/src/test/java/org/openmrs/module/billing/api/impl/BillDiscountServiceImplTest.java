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
import org.openmrs.module.billing.api.BillDiscountService;
import org.openmrs.module.billing.api.BillLineItemService;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillDiscount;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.DiscountType;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BillDiscountServiceImplTest extends BaseModuleContextSensitiveTest {
	
	// Clean POSTED bill (200) — no existing discounts. Used for bill-level happy-path and rejection tests.
	private static final String POSTED_BILL_UUID = "d0000000-0000-0000-0000-000000000200";

	// POSTED bill (100) with a pre-existing line-scoped discount on line item 100. Used for line-scope tests.
	private static final String LINE_SCOPED_BILL_UUID = "d0000000-0000-0000-0000-000000000100";

	private static final String CANCELLED_BILL_UUID = "d0000000-0000-0000-0000-000000000101";

	private static final String PAID_BILL_UUID = "5028814B39B565A20139B95D74360004";

	private static final String PENDING_BILL_UUID = "6028814B39B565A20139B95D74360004";

	private static final String BILL_WITH_ACTIVE_DISCOUNT_UUID = "4028814B39B565A20139B95D74360004";

	private static final String LINE_ITEM_WITH_DISCOUNT_UUID = "d0000000-0000-0000-0000-000000000110";

	private static final String FREE_LINE_ITEM_UUID = "d0000000-0000-0000-0000-000000000112";

	private static final String OTHER_BILL_LINE_ITEM_UUID = "4028814B39B565A20139B95FB3440005";
	
	private BillDiscountService service;
	
	private BillService billService;
	
	private BillLineItemService lineItemService;
	
	@BeforeEach
	public void setup() throws Exception {
		service = Context.getService(BillDiscountService.class);
		billService = Context.getService(BillService.class);
		lineItemService = Context.getService(BillLineItemService.class);
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "StockOperationType.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "PaymentModeTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillDiscountTest.xml");
	}
	
	@Test
	public void saveBillDiscount_shouldPersistValidDiscountOnPostedBill() {
		BillDiscount discount = buildDiscount(POSTED_BILL_UUID, DiscountType.FIXED_AMOUNT, new BigDecimal("50.00"),
		    new BigDecimal("50.00"), "Approved financial aid");
		
		BillDiscount saved = service.saveBillDiscount(discount);
		
		assertNotNull(saved);
		assertNotNull(saved.getBillDiscountId());
		assertEquals(0, new BigDecimal("50.00").compareTo(saved.getDiscountAmount()));
	}
	
	@Test
	public void saveBillDiscount_shouldAllowReapplyingAfterPreviousDiscountVoided() {
		// Bill 2 has only a voided discount - a new one must be accepted.
		BillDiscount discount = buildDiscount(PENDING_BILL_UUID, DiscountType.PERCENTAGE, new BigDecimal("20.00"),
		    new BigDecimal("25.10"), "Financial hardship");
		
		BillDiscount saved = service.saveBillDiscount(discount);
		
		assertNotNull(saved);
		assertNotNull(saved.getBillDiscountId());
	}
	
	@Test
	public void saveBillDiscount_shouldRejectWhenFeatureDisabled() {
		Context.getAdministrationService().setGlobalProperty(ModuleSettings.DISCOUNT_ENABLED, "false");
		BillDiscount discount = buildDiscount(POSTED_BILL_UUID, DiscountType.FIXED_AMOUNT, new BigDecimal("10.00"),
		    new BigDecimal("10.00"), "any");
		
		assertThrows(Exception.class, () -> service.saveBillDiscount(discount));
	}
	
	@Test
	public void saveBillDiscount_shouldRejectDiscountOnPaidBill() {
		BillDiscount discount = buildDiscount(PAID_BILL_UUID, DiscountType.FIXED_AMOUNT, new BigDecimal("10.00"),
		    new BigDecimal("10.00"), "Too late");
		
		assertThrows(Exception.class, () -> service.saveBillDiscount(discount));
	}
	
	@Test
	public void saveBillDiscount_shouldRejectDiscountOnCancelledBill() {
		BillDiscount discount = buildDiscount(CANCELLED_BILL_UUID, DiscountType.FIXED_AMOUNT, new BigDecimal("10.00"),
		    new BigDecimal("10.00"), "Too late");
		
		assertThrows(Exception.class, () -> service.saveBillDiscount(discount));
	}
	
	@Test
	public void saveBillDiscount_shouldRejectWithoutJustification() {
		BillDiscount discount = buildDiscount(POSTED_BILL_UUID, DiscountType.FIXED_AMOUNT, new BigDecimal("10.00"),
		    new BigDecimal("10.00"), "");
		
		assertThrows(Exception.class, () -> service.saveBillDiscount(discount));
	}
	
	@Test
	public void saveBillDiscount_shouldRejectNonPositiveValue() {
		BillDiscount discount = buildDiscount(POSTED_BILL_UUID, DiscountType.FIXED_AMOUNT, BigDecimal.ZERO, BigDecimal.ZERO,
		    "Zero value");
		
		assertThrows(Exception.class, () -> service.saveBillDiscount(discount));
	}
	
	@Test
	public void saveBillDiscount_shouldRejectWhenAmountExceedsBillTotal() {
		// Clean POSTED bill 200 total = 200.00. Try to discount 300.00.
		BillDiscount discount = buildDiscount(POSTED_BILL_UUID, DiscountType.FIXED_AMOUNT, new BigDecimal("300.00"),
		    new BigDecimal("300.00"), "Exceeds total");
		
		assertThrows(Exception.class, () -> service.saveBillDiscount(discount));
	}
	
	@Test
	public void saveBillDiscount_shouldRejectSecondDiscountOnSameBill() {
		// Bill with UUID 4028... already has an active discount (id=1) in the dataset.
		BillDiscount discount = buildDiscount(BILL_WITH_ACTIVE_DISCOUNT_UUID, DiscountType.FIXED_AMOUNT,
		    new BigDecimal("10.00"), new BigDecimal("10.00"), "Duplicate");
		
		assertThrows(Exception.class, () -> service.saveBillDiscount(discount));
	}
	
	@Test
	public void getBillDiscountByBillId_shouldReturnActiveDiscount() {
		Bill bill = billService.getBillByUuid(BILL_WITH_ACTIVE_DISCOUNT_UUID);
		assertNotNull(bill);
		
		BillDiscount discount = service.getBillDiscountByBillId(bill.getId());
		
		assertNotNull(discount);
		assertTrue(discount.getId() > 0);
	}
	
	@Test
	public void getBillDiscountByBillId_shouldIgnoreVoidedDiscount() {
		Bill bill = billService.getBillByUuid(PENDING_BILL_UUID);
		assertNotNull(bill);
		
		assertNull(service.getBillDiscountByBillId(bill.getId()));
	}
	
	@Test
	public void saveBillDiscount_shouldPersistLineScopedDiscount() {
		// Line item 102 on bill 100 has no existing discount; happy path.
		BillDiscount discount = buildLineScopedDiscount(LINE_SCOPED_BILL_UUID, FREE_LINE_ITEM_UUID,
		    DiscountType.FIXED_AMOUNT, new BigDecimal("10.00"), new BigDecimal("10.00"), "Item-specific waiver");
		
		BillDiscount saved = service.saveBillDiscount(discount);
		
		assertNotNull(saved);
		assertNotNull(saved.getBillDiscountId());
		assertNotNull(saved.getLineItem());
		assertEquals(FREE_LINE_ITEM_UUID, saved.getLineItem().getUuid());
	}
	
	@Test
	public void saveBillDiscount_shouldRejectSecondLineScopedDiscountOnSameLineItem() {
		// Line item 100 already has an active line-scoped discount in the dataset.
		BillDiscount discount = buildLineScopedDiscount(LINE_SCOPED_BILL_UUID, LINE_ITEM_WITH_DISCOUNT_UUID,
		    DiscountType.FIXED_AMOUNT, new BigDecimal("5.00"), new BigDecimal("5.00"), "Duplicate line discount");
		
		assertThrows(Exception.class, () -> service.saveBillDiscount(discount));
	}
	
	@Test
	public void saveBillDiscount_shouldRejectBillLevelDiscountWhenLineScopedExists() {
		// Bill 100 has an active line-scoped discount; bill-level and line-scoped must not coexist.
		BillDiscount discount = buildDiscount(LINE_SCOPED_BILL_UUID, DiscountType.FIXED_AMOUNT, new BigDecimal("15.00"),
		    new BigDecimal("15.00"), "Should be rejected");

		assertThrows(Exception.class, () -> service.saveBillDiscount(discount));
	}

	@Test
	public void saveBillDiscount_shouldRejectLineScopedDiscountWhenBillLevelExists() {
		// Bill 0 already has an active bill-level discount; cannot add a line-scoped one on the same bill.
		BillDiscount discount = buildLineScopedDiscount(BILL_WITH_ACTIVE_DISCOUNT_UUID, OTHER_BILL_LINE_ITEM_UUID,
		    DiscountType.FIXED_AMOUNT, new BigDecimal("5.00"), new BigDecimal("5.00"), "Should be rejected");

		assertThrows(Exception.class, () -> service.saveBillDiscount(discount));
	}
	
	@Test
	public void saveBillDiscount_shouldRejectLineItemFromDifferentBill() {
		// Set bill = clean POSTED bill 200 but lineItem belongs to bill 0.
		BillDiscount discount = buildLineScopedDiscount(POSTED_BILL_UUID, OTHER_BILL_LINE_ITEM_UUID,
		    DiscountType.FIXED_AMOUNT, new BigDecimal("5.00"), new BigDecimal("5.00"), "Wrong bill");
		
		assertThrows(Exception.class, () -> service.saveBillDiscount(discount));
	}
	
	@Test
	public void saveBillDiscount_shouldRejectWhenAmountExceedsLineItemTotal() {
		// Line item 102 total = 80.00; try 100.00.
		BillDiscount discount = buildLineScopedDiscount(LINE_SCOPED_BILL_UUID, FREE_LINE_ITEM_UUID,
		    DiscountType.FIXED_AMOUNT, new BigDecimal("100.00"), new BigDecimal("100.00"), "Exceeds line total");
		
		assertThrows(Exception.class, () -> service.saveBillDiscount(discount));
	}
	
	@Test
	public void saveBillDiscount_shouldRejectScopeFlipFromBillLevelToLineScopedWhenLineAlreadyHasOne() {
		// Regression: validator must run on update too. Take bill 100's existing line-scoped
		// discount on line item 100, flip it to bill-level, save → must reject because no
		// existing bill-level row remains, BUT another line-scoped (itself, pre-flip) was the
		// only line discount; this should pass actually. Construct the harder case: load bill 0's
		// bill-level discount, point it at bill 100's line item, save → must reject because
		// line item 100 already has its own active line-scoped discount.
		BillDiscount existing = service.getBillDiscountByUuid("d1000000-0000-0000-0000-000000000001");
		assertNotNull(existing);

		BillLineItem lineWithDiscount = lineItemService.getBillLineItemByUuid(LINE_ITEM_WITH_DISCOUNT_UUID);
		assertNotNull(lineWithDiscount);

		// Move from bill-level on bill 0 to line-scoped on bill 100's already-discounted line.
		existing.setBill(lineWithDiscount.getBill());
		existing.setLineItem(lineWithDiscount);

		assertThrows(Exception.class, () -> service.saveBillDiscount(existing));
	}

	@Test
	public void saveBillDiscount_shouldRejectScopeFlipFromLineScopedToBillLevelWhenBillLevelExists() {
		// Take bill 100's line-scoped discount and null out lineItem to make it bill-level.
		// But bill 100 currently has no bill-level discount. To trigger conflict, point at bill 0
		// which already has an active bill-level discount.
		BillDiscount existing = service.getBillDiscountByUuid("d1000000-0000-0000-0000-000000000003");
		assertNotNull(existing);

		Bill billWithBillLevel = billService.getBillByUuid(BILL_WITH_ACTIVE_DISCOUNT_UUID);
		assertNotNull(billWithBillLevel);

		existing.setBill(billWithBillLevel);
		existing.setLineItem(null);

		assertThrows(Exception.class, () -> service.saveBillDiscount(existing));
	}

	@Test
	public void saveBillDiscount_shouldAllowReSavingExistingDiscountWithoutFalsePositiveDuplicate() {
		// Self-exclusion regression: re-saving the same row (no scope change) must not
		// trip the alreadyExists check by matching against itself.
		BillDiscount existing = service.getBillDiscountByUuid("d1000000-0000-0000-0000-000000000001");
		assertNotNull(existing);

		existing.setJustification("Updated justification");

		BillDiscount saved = service.saveBillDiscount(existing);

		assertNotNull(saved);
		assertEquals("Updated justification", saved.getJustification());
	}

	@Test
	public void getDiscountsByBillId_shouldReturnFullAuditHistory() {
		Bill bill = billService.getBillByUuid(BILL_WITH_ACTIVE_DISCOUNT_UUID);
		assertNotNull(bill);

		List<BillDiscount> history = service.getDiscountsByBillId(bill.getId());

		assertNotNull(history);
		assertEquals(1, history.size());
		assertFalse(history.get(0).getVoided());
	}

	@Test
	public void getActiveLineItemDiscount_shouldReturnDiscountForLineItem() {
		BillLineItem lineItem = lineItemService.getBillLineItemByUuid(LINE_ITEM_WITH_DISCOUNT_UUID);
		assertNotNull(lineItem);
		
		BillDiscount discount = service.getActiveLineItemDiscount(lineItem.getId());
		
		assertNotNull(discount);
		assertEquals(lineItem.getId(), discount.getLineItem().getId());
	}
	
	private BillDiscount buildDiscount(String billUuid, DiscountType type, BigDecimal value, BigDecimal amount,
	        String justification) {
		Bill bill = billService.getBillByUuid(billUuid);
		assertNotNull(bill, "Test dataset missing bill: " + billUuid);

		BillDiscount discount = new BillDiscount();
		discount.setBill(bill);
		discount.setDiscountType(type);
		discount.setDiscountValue(value);
		discount.setJustification(justification);
		discount.setInitiator(Context.getAuthenticatedUser());
		return discount;
	}

	private BillDiscount buildLineScopedDiscount(String billUuid, String lineItemUuid, DiscountType type, BigDecimal value,
	        BigDecimal amount, String justification) {
		BillDiscount discount = buildDiscount(billUuid, type, value, amount, justification);
		BillLineItem lineItem = lineItemService.getBillLineItemByUuid(lineItemUuid);
		assertNotNull(lineItem, "Test dataset missing line item: " + lineItemUuid);
		discount.setLineItem(lineItem);
		return discount;
	}
}
