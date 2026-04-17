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
package org.openmrs.module.billing.api.billing.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
import org.openmrs.Order;
import org.openmrs.TestOrder;
import org.openmrs.module.billing.api.BillExemptionService;
import org.openmrs.module.billing.api.ItemPriceService;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
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
		assertEquals(BillStatus.PENDING, lineItem.getPaymentStatus());
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
