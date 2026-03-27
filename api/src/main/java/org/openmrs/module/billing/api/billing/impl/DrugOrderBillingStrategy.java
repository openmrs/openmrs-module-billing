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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.ItemPriceService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.openmrs.module.billing.api.model.ExemptionType;
import org.openmrs.module.stockmanagement.api.StockManagementService;
import org.openmrs.module.stockmanagement.api.model.StockItem;

/**
 * Default billing strategy for {@link DrugOrder}s. Creates a bill line item based on the stock item
 * linked to the ordered drug.
 */
@Slf4j
public class DrugOrderBillingStrategy extends AbstractOrderBillingStrategy {
	
	@Override
	public boolean supports(Order order) {
		if (!(order instanceof DrugOrder)) {
			return false;
		}
		Order.Action action = order.getAction();
		return action == Order.Action.NEW || action == Order.Action.REVISE || action == Order.Action.DISCONTINUE;
	}
	
	@Override
	protected Optional<Bill> handleNewOrder(Order order) {
		DrugOrder drugOrder = (DrugOrder) order;
		
		if (drugOrder.getDrug() == null) {
			log.warn("DrugOrder {} has no drug set, cannot generate bill", order.getUuid());
			return Optional.empty();
		}
		
		StockManagementService stockService = Context.getService(StockManagementService.class);
		Integer drugId = drugOrder.getDrug().getDrugId();
		List<StockItem> stockItems = stockService.getStockItemByDrug(drugId);
		
		if (stockItems.isEmpty()) {
			log.debug("No stock item found for drug ID: {}", drugId);
			return Optional.empty();
		}
		
		int quantity = (int) (drugOrder.getQuantity() != null ? drugOrder.getQuantity() : 0.0);
		boolean isExempted = checkIfOrderIsExempted(order, ExemptionType.COMMODITY);
		BillStatus lineItemStatus = isExempted ? BillStatus.EXEMPTED : BillStatus.PENDING;
		
		StockItem stockItem = stockItems.get(0);
		BillLineItem lineItem = createLineItem(stockItem, quantity, lineItemStatus, order);
		
		return saveBill(order.getPatient(), lineItem, order);
	}
	
	private BillLineItem createLineItem(StockItem stockItem, int quantity, BillStatus lineItemStatus, Order order) {
		ItemPriceService priceService = Context.getService(ItemPriceService.class);
		
		BillLineItem lineItem = new BillLineItem();
		lineItem.setItem(stockItem);
		
		List<CashierItemPrice> itemPrices = priceService.getItemPrice(stockItem);
		if (!itemPrices.isEmpty()) {
			lineItem.setPrice(itemPrices.get(0).getPrice());
		} else if (stockItem.getPurchasePrice() != null) {
			lineItem.setPrice(stockItem.getPurchasePrice());
		} else {
			lineItem.setPrice(BigDecimal.ZERO);
		}
		
		lineItem.setQuantity(quantity);
		lineItem.setPaymentStatus(lineItemStatus);
		lineItem.setLineItemOrder(0);
		lineItem.setOrder(order);
		
		return lineItem;
	}
}
