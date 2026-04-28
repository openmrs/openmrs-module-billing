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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.Provider;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.api.ItemPriceService;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.openmrs.module.billing.api.model.ExemptionType;
import org.openmrs.module.stockmanagement.api.StockManagementService;
import org.openmrs.module.stockmanagement.api.model.StockItem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default billing strategy for {@link DrugOrder}s. Creates a bill line item based on the stock item
 * linked to the ordered drug.
 */
@Slf4j
@Setter(onMethod_ = @Autowired)
public class DrugOrderBillingStrategy extends AbstractDefaultOrderBillingStrategy {
	
	private StockManagementService stockManagementService;
	
	private ItemPriceService itemPriceService;
	
	@Override
	protected boolean supportsOrder(Order order) {
		return order instanceof DrugOrder;
	}
	
	@Override
	protected Optional<BillLineItem> createBillLineItem(Order order) {
		DrugOrder drugOrder = (DrugOrder) order;
		
		if (drugOrder.getDrug() == null) {
			log.warn("DrugOrder {} has no drug set, cannot generate bill", order.getUuid());
			return Optional.empty();
		}
		
		Integer drugId = drugOrder.getDrug().getDrugId();
		List<StockItem> stockItems = stockManagementService.getStockItemByDrug(drugId);
		
		if (stockItems.isEmpty()) {
			log.debug("No stock item found for drug ID: {}", drugId);
			return Optional.empty();
		}
		
		int quantity = (int) (drugOrder.getQuantity() != null ? drugOrder.getQuantity() : 0.0);
		boolean isExempted = checkIfOrderIsExempted(order, ExemptionType.COMMODITY);
		BillStatus lineItemStatus = isExempted ? BillStatus.EXEMPTED : BillStatus.PENDING;
		
		StockItem stockItem = stockItems.get(0);
		BillLineItem lineItem = createLineItem(resolvePrice(stockItem), quantity, lineItemStatus, order);
		lineItem.setItem(stockItem);
		return Optional.of(lineItem);
	}
	
	private BigDecimal resolvePrice(StockItem stockItem) {
		List<CashierItemPrice> itemPrices = itemPriceService.getItemPrice(stockItem);
		if (!itemPrices.isEmpty()) {
			return itemPrices.get(0).getPrice();
		} else if (stockItem.getPurchasePrice() != null) {
			return stockItem.getPurchasePrice();
		}
		return BigDecimal.ZERO;
	}
	
	@Override
	public Provider resolveCashier(Order order) {
		return order.getOrderer();
	}
	
	@Override
	public CashPoint resolveCashPoint() {
		List<CashPoint> cashPoints = cashPointService.getAllCashPoints(false);
		return cashPoints.isEmpty() ? null : cashPoints.get(0);
	}
}
