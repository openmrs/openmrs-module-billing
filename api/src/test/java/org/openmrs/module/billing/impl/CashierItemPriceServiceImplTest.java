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

package org.openmrs.module.billing.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.CashierItemPriceService;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CashierItemPriceServiceImplTest extends BaseModuleContextSensitiveTest {
	
	private CashierItemPriceService cashierItemPriceService;
	
	@BeforeEach
	public void setup() {
		cashierItemPriceService = Context.getService(CashierItemPriceService.class);
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "StockOperationType.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillableServiceTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "PaymentModeTest.xml");
	}
	
	/**
	 * @see CashierItemPriceService#getCashierItemPrice(Integer)
	 */
	@Test
	public void getCashierItemPrice_shouldReturnCashierItemPriceWithSpecificId() {
		CashierItemPrice cashierItemPrice = cashierItemPriceService.getCashierItemPrice(1);
		assertNotNull(cashierItemPrice);
		assertEquals(1, cashierItemPrice.getId());
		assertEquals("Standard Price", cashierItemPrice.getName());
	}
	
	/**
	 * @see CashierItemPriceService#getCashierItemPrice(Integer)
	 */
	@Test
	public void getCashierItemPrice_shouldReturnNullIfIdNotFound() {
		assertNull(cashierItemPriceService.getCashierItemPrice(999));
	}
	
	/**
	 * @see CashierItemPriceService#getCashierItemPriceByUuid(String)
	 */
	@Test
	public void getCashierItemPriceByUuid_shouldReturnCashierItemPriceWithSpecificUuid() {
		CashierItemPrice cashierItemPrice = cashierItemPriceService
		        .getCashierItemPriceByUuid("8631b434-78aa-102b-91a0-001e378eb680");
		assertNotNull(cashierItemPrice);
		assertEquals("8631b434-78aa-102b-91a0-001e378eb680", cashierItemPrice.getUuid());
	}
	
	/**
	 * @see CashierItemPriceService#getCashierItemPriceByUuid(String)
	 */
	@Test
	public void getCashierItemPriceByUuid_shouldReturnNullIfUuidNotFound() {
		assertNull(cashierItemPriceService.getCashierItemPriceByUuid("wrong-uuid"));
	}
	
	/**
	 * @see CashierItemPriceService#saveCashierItemPrice(CashierItemPrice)
	 */
	@Test
	public void saveCashierItemPrice_shouldSaveValidCashierItemPrice() {
		CashierItemPrice cashierItemPrice = new CashierItemPrice();
		cashierItemPrice.setName("Test Cashier Item");
		cashierItemPrice.setPrice(BigDecimal.valueOf(100));
		cashierItemPriceService.saveCashierItemPrice(cashierItemPrice);
	}
	
	/**
	 * @see CashierItemPriceService#saveCashierItemPrice(CashierItemPrice)
	 */
	@Test
	public void saveCashierItemPriceShouldThrowAnIllegalArgumentExceptionIfCashierItemPriceIsNull() {
		assertThrows(IllegalArgumentException.class, () -> cashierItemPriceService.saveCashierItemPrice(null));
	}
	
	/**
	 * @see CashierItemPriceService#retireCashierItemPrice(CashierItemPrice, String)
	 */
	@Test
	public void retireCashierItemPrice_shouldRetireCashierItemPrice() {
		CashierItemPrice cashierItemPrice = cashierItemPriceService.getCashierItemPrice(0);
		assertNotNull(cashierItemPrice);
		assertFalse(cashierItemPrice.getRetired());
		cashierItemPriceService.retireCashierItemPrice(cashierItemPrice, "Retire reason");
		
		CashierItemPrice retiredItemPrice = cashierItemPriceService.getCashierItemPrice(0);
		assertNotNull(retiredItemPrice);
		assertTrue(retiredItemPrice.getRetired());
	}
	
	/**
	 * @see CashierItemPriceService#retireCashierItemPrice(CashierItemPrice, String)
	 */
	@Test
	public void retireCashierItemPrice_shouldThrowIllegalArgumentExceptionIfReasonIsEmpty() {
		CashierItemPrice cashierItemPrice = cashierItemPriceService.getCashierItemPrice(0);
		assertNotNull(cashierItemPrice);
		assertThrows(IllegalArgumentException.class,
		    () -> cashierItemPriceService.retireCashierItemPrice(cashierItemPrice, ""));
	}
	
	/**
	 * @see CashierItemPriceService#unretireCashierItemPrice(CashierItemPrice)
	 */
	@Test
	public void unretireCashierItemPrice_shouldUnretireCashierItemPrice() {
		CashierItemPrice retiredItemPrice = cashierItemPriceService.getCashierItemPrice(2);
		assertNotNull(retiredItemPrice);
		assertTrue(retiredItemPrice.getRetired());
		
		CashierItemPrice cashierItemPrice = cashierItemPriceService.unretireCashierItemPrice(retiredItemPrice);
		assertNotNull(cashierItemPrice);
		assertFalse(cashierItemPrice.getRetired());
	}
	
	/**
	 * @see CashierItemPriceService#purgeCashierItemPrice(CashierItemPrice)
	 */
	@Test
	public void purgeCashierItemPrice_shouldPurgeCashierItemPrice() {
		CashierItemPrice cashierItemPrice = cashierItemPriceService.getCashierItemPrice(0);
		assertNotNull(cashierItemPrice);
		
		cashierItemPriceService.purgeCashierItemPrice(cashierItemPrice);
		
		assertNull(cashierItemPriceService.getCashierItemPrice(0));
	}
	
	/**
	 * @see CashierItemPriceService#getCashierItemPrices(boolean)
	 */
	@Test
	public void getCashierItemPrices_shouldReturnAllCashierItemPrices() {
		List<CashierItemPrice> unretiredItems = cashierItemPriceService.getCashierItemPrices(false);
		assertNotNull(unretiredItems);
		assertEquals(2, unretiredItems.size());
		
		List<CashierItemPrice> allItems = cashierItemPriceService.getCashierItemPrices(true);
		assertNotNull(allItems);
		assertEquals(3, allItems.size());
	}
}
