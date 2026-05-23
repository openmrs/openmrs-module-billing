/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.billing.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.TestOrder;
import org.openmrs.module.billing.api.BillExemptionService;
import org.openmrs.module.billing.api.ItemPriceService;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillLineItemStatus;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.openmrs.module.stockmanagement.api.StockManagementService;
import org.openmrs.module.stockmanagement.api.model.StockItem;

@ExtendWith(MockitoExtension.class)
public class DrugOrderBillingStrategyTest {
	
	@Mock
	private StockManagementService stockManagementService;
	
	@Mock
	private ItemPriceService itemPriceService;
	
	@Mock
	private BillExemptionService billExemptionService;
	
	@InjectMocks
	private DrugOrderBillingStrategy strategy;
	
	private Drug drug;
	
	private DrugOrder drugOrder;
	
	private StockItem stockItem;
	
	@BeforeEach
	public void setup() {
		drug = new Drug();
		drug.setDrugId(1);
		drug.setConcept(new Concept());
		
		drugOrder = new DrugOrder();
		drugOrder.setDrug(drug);
		drugOrder.setQuantity(5.0);
		drugOrder.setUuid("test-order-uuid");
		
		stockItem = new StockItem();
		stockItem.setUuid("test-stock-item-uuid");
		stockItem.setPurchasePrice(new BigDecimal("100.00"));
	}
	
	@Test
	public void supportsOrder_shouldReturnTrueForDrugOrder() {
		assertTrue(strategy.supportsOrder(drugOrder));
	}
	
	@Test
	public void supportsOrder_shouldReturnFalseForTestOrder() {
		assertFalse(strategy.supportsOrder(new TestOrder()));
	}
	
	@Test
	public void createBillLineItem_shouldCreateLineItemWithItemPrice() {
		CashierItemPrice itemPrice = new CashierItemPrice();
		itemPrice.setPrice(new BigDecimal("200.00"));
		
		when(stockManagementService.getStockItemByDrug(1)).thenReturn(Collections.singletonList(stockItem));
		when(itemPriceService.getItemPrice(stockItem)).thenReturn(Collections.singletonList(itemPrice));
		
		Optional<BillLineItem> result = strategy.createBillLineItem(drugOrder);
		
		assertTrue(result.isPresent());
		BillLineItem lineItem = result.get();
		assertEquals(new BigDecimal("200.00"), lineItem.getPrice());
		assertEquals(5, lineItem.getQuantity());
		assertEquals(BillLineItemStatus.PENDING, lineItem.getStatus());
		assertEquals(stockItem, lineItem.getItem());
	}
	
	@Test
	public void createBillLineItem_shouldFallBackToPurchasePrice() {
		when(stockManagementService.getStockItemByDrug(1)).thenReturn(Collections.singletonList(stockItem));
		when(itemPriceService.getItemPrice(stockItem)).thenReturn(Collections.emptyList());
		
		Optional<BillLineItem> result = strategy.createBillLineItem(drugOrder);
		
		assertTrue(result.isPresent());
		assertEquals(new BigDecimal("100.00"), result.get().getPrice());
	}
	
	@Test
	public void createBillLineItem_shouldReturnZeroPriceWhenNoPriceAvailable() {
		stockItem.setPurchasePrice(null);
		when(stockManagementService.getStockItemByDrug(1)).thenReturn(Collections.singletonList(stockItem));
		when(itemPriceService.getItemPrice(stockItem)).thenReturn(Collections.emptyList());
		
		Optional<BillLineItem> result = strategy.createBillLineItem(drugOrder);
		
		assertTrue(result.isPresent());
		assertEquals(BigDecimal.ZERO, result.get().getPrice());
	}
	
	@Test
	public void createBillLineItem_shouldReturnEmptyWhenNoStockItem() {
		when(stockManagementService.getStockItemByDrug(1)).thenReturn(Collections.emptyList());
		
		Optional<BillLineItem> result = strategy.createBillLineItem(drugOrder);
		
		assertFalse(result.isPresent());
	}
	
	@Test
	public void createBillLineItem_shouldReturnEmptyWhenDrugIsNull() {
		drugOrder.setDrug(null);
		
		Optional<BillLineItem> result = strategy.createBillLineItem(drugOrder);
		
		assertFalse(result.isPresent());
	}
	
	@Test
	public void createBillLineItem_shouldDefaultQuantityToZeroWhenNull() {
		drugOrder.setQuantity(null);
		when(stockManagementService.getStockItemByDrug(1)).thenReturn(Collections.singletonList(stockItem));
		when(itemPriceService.getItemPrice(stockItem)).thenReturn(Collections.emptyList());
		
		Optional<BillLineItem> result = strategy.createBillLineItem(drugOrder);
		
		assertTrue(result.isPresent());
		assertEquals(0, result.get().getQuantity());
	}
}
