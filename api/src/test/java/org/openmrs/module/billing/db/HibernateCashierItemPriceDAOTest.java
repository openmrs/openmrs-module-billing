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

package org.openmrs.module.billing.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.db.CashierItemPriceDAO;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class HibernateCashierItemPriceDAOTest extends BaseModuleContextSensitiveTest {
	
	private CashierItemPriceDAO cashierItemPriceDAO;
	
	@BeforeEach
	public void setup() {
		cashierItemPriceDAO = Context.getRegisteredComponent("cashierItemPriceDAO", CashierItemPriceDAO.class);
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "StockOperationType.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillableServiceTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "PaymentModeTest.xml");
	}
	
	/**
	 * @see CashierItemPriceDAO#getCashierItemPrice(Integer)
	 */
	@Test
	public void getCashierItemPrice_shouldReturnCashierItemPriceWithSpecificId() {
		CashierItemPrice cashierItemPrice = cashierItemPriceDAO.getCashierItemPrice(1);
		assertNotNull(cashierItemPrice);
		assertEquals("Standard Price", cashierItemPrice.getName());
	}
	
	/**
	 * @see CashierItemPriceDAO#getCashierItemPrice(Integer)
	 */
	@Test
	public void getCashierItemPrice_shouldReturnNullIfIdNotFound() {
		assertNull(cashierItemPriceDAO.getCashierItemPrice(999));
	}
	
	/**
	 * @see CashierItemPriceDAO#getCashierItemPriceByUuid(String)
	 */
	@Test
	public void getCashierItemPriceByUuid_shouldReturnCashierItemPriceWithSpecificUuid() {
		CashierItemPrice cashierItemPrice = cashierItemPriceDAO
		        .getCashierItemPriceByUuid("8631b434-78aa-102b-91a0-001e378eb680");
		assertNotNull(cashierItemPrice);
		assertEquals("8631b434-78aa-102b-91a0-001e378eb680", cashierItemPrice.getUuid());
	}
	
	/**
	 * @see CashierItemPriceDAO#getCashierItemPriceByUuid(String)
	 */
	@Test
	public void getCashierItemPriceByUuid_shouldReturnNullIfUuidNotFound() {
		assertNull(cashierItemPriceDAO.getCashierItemPriceByUuid("wrong-uuid"));
	}
	
	/**
	 * @see CashierItemPriceDAO#saveCashierItemPrice(CashierItemPrice)
	 */
	@Test
	public void saveCashierItemPrice_shouldSaveValidCashierItemPrice() {
		CashierItemPrice cashierItemPrice = new CashierItemPrice();
		cashierItemPrice.setName("Test Cashier Item");
		cashierItemPrice.setPrice(BigDecimal.valueOf(100));
		cashierItemPriceDAO.saveCashierItemPrice(cashierItemPrice);
	}
	
	/**
	 * @see CashierItemPriceDAO#purgeCashierItemPrice(CashierItemPrice)
	 */
	@Test
	public void purgeCashierItemPrice_shouldPurgeCashierItemPrice() {
		CashierItemPrice cashierItemPrice = cashierItemPriceDAO.getCashierItemPrice(0);
		assertNotNull(cashierItemPrice);
		
		cashierItemPriceDAO.purgeCashierItemPrice(cashierItemPrice);
		
		assertNull(cashierItemPriceDAO.getCashierItemPrice(0));
	}
	
	/**
	 * @see CashierItemPriceDAO#getCashierItemPrices(boolean)
	 */
	@Test
	public void getCashierItemPrices_shouldReturnAllCashierItemPrices() {
		List<CashierItemPrice> unretiredItems = cashierItemPriceDAO.getCashierItemPrices(false);
		assertNotNull(unretiredItems);
		assertEquals(2, unretiredItems.size());
		
		List<CashierItemPrice> allItems = cashierItemPriceDAO.getCashierItemPrices(true);
		assertNotNull(allItems);
		assertEquals(3, allItems.size());
	}
	
}
