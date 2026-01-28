/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
