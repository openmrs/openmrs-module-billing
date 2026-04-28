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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BillDiscountServiceImplTest extends BaseModuleContextSensitiveTest {
	
	private static final String POSTED_BILL_UUID = "d0000000-0000-0000-0000-000000000100";
	
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
		// POSTED_BILL_UUID total = 280.00 (line items 100 + 102). Try to discount 300.00.
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
		BillDiscount discount = buildLineScopedDiscount(POSTED_BILL_UUID, FREE_LINE_ITEM_UUID, DiscountType.FIXED_AMOUNT,
		    new BigDecimal("10.00"), new BigDecimal("10.00"), "Item-specific waiver");
		
		BillDiscount saved = service.saveBillDiscount(discount);
		
		assertNotNull(saved);
		assertNotNull(saved.getBillDiscountId());
		assertNotNull(saved.getLineItem());
		assertEquals(FREE_LINE_ITEM_UUID, saved.getLineItem().getUuid());
	}
	
	@Test
	public void saveBillDiscount_shouldRejectSecondLineScopedDiscountOnSameLineItem() {
		// Line item 100 already has an active line-scoped discount in the dataset.
		BillDiscount discount = buildLineScopedDiscount(POSTED_BILL_UUID, LINE_ITEM_WITH_DISCOUNT_UUID,
		    DiscountType.FIXED_AMOUNT, new BigDecimal("5.00"), new BigDecimal("5.00"), "Duplicate line discount");
		
		assertThrows(Exception.class, () -> service.saveBillDiscount(discount));
	}
	
	@Test
	public void saveBillDiscount_shouldAllowBillLevelDiscountAlongsideLineScoped() {
		// Bill 100 has a line-scoped discount but no bill-level discount; bill-level apply must succeed.
		BillDiscount discount = buildDiscount(POSTED_BILL_UUID, DiscountType.FIXED_AMOUNT, new BigDecimal("15.00"),
		    new BigDecimal("15.00"), "Bill-level on top of line discount");
		
		BillDiscount saved = service.saveBillDiscount(discount);
		
		assertNotNull(saved);
		assertNull(saved.getLineItem());
	}
	
	@Test
	public void saveBillDiscount_shouldRejectLineItemFromDifferentBill() {
		// Set bill = bill 100 but lineItem belongs to bill 0.
		BillDiscount discount = buildLineScopedDiscount(POSTED_BILL_UUID, OTHER_BILL_LINE_ITEM_UUID,
		    DiscountType.FIXED_AMOUNT, new BigDecimal("5.00"), new BigDecimal("5.00"), "Wrong bill");
		
		assertThrows(Exception.class, () -> service.saveBillDiscount(discount));
	}
	
	@Test
	public void saveBillDiscount_shouldRejectWhenAmountExceedsLineItemTotal() {
		// Line item 102 total = 80.00; try 100.00.
		BillDiscount discount = buildLineScopedDiscount(POSTED_BILL_UUID, FREE_LINE_ITEM_UUID, DiscountType.FIXED_AMOUNT,
		    new BigDecimal("100.00"), new BigDecimal("100.00"), "Exceeds line total");
		
		assertThrows(Exception.class, () -> service.saveBillDiscount(discount));
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
		discount.setDiscountAmount(amount);
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
