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
import org.openmrs.Order;
import org.openmrs.Provider;
import org.openmrs.TestOrder;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.api.BillableServiceService;
import org.openmrs.module.billing.api.ItemPriceService;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.BillableServiceStatus;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.openmrs.module.billing.api.model.ExemptionType;
import org.openmrs.module.billing.api.search.BillableServiceSearch;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default billing strategy for {@link TestOrder}s. Creates a bill line item based on the billable
 * service linked to the test order's concept.
 */
@Slf4j
@Setter(onMethod_ = @Autowired)
public class TestOrderBillingStrategy extends AbstractDefaultOrderBillingStrategy {
	
	private BillableServiceService billableServiceService;
	
	private ItemPriceService itemPriceService;
	
	@Override
	protected boolean supportsOrder(Order order) {
		return order instanceof TestOrder;
	}
	
	@Override
	protected Optional<BillLineItem> createBillLineItem(Order order) {
		TestOrder testOrder = (TestOrder) order;
		
		if (testOrder.getConcept() == null) {
			log.warn("TestOrder {} has no concept set, cannot generate bill", order.getUuid());
			return Optional.empty();
		}
		
		BillableServiceSearch searchTemplate = new BillableServiceSearch();
		searchTemplate.setConceptUuid(testOrder.getConcept().getUuid());
		searchTemplate.setServiceStatus(BillableServiceStatus.ENABLED);
		
		List<BillableService> searchResult = billableServiceService.getBillableServices(searchTemplate, null);
		if (searchResult.isEmpty()) {
			log.debug("No billable service found for concept: {}", testOrder.getConcept().getUuid());
			return Optional.empty();
		}
		
		BillableService billableService = searchResult.get(0);
		BillStatus lineItemStatus = checkIfOrderIsExempted(order, ExemptionType.SERVICE) ? BillStatus.EXEMPTED
		        : BillStatus.PENDING;
		
		BillLineItem lineItem = createLineItem(resolvePrice(billableService), 1, lineItemStatus, order);
		lineItem.setBillableService(billableService);
		return Optional.of(lineItem);
	}
	
	private BigDecimal resolvePrice(BillableService billableService) {
		List<CashierItemPrice> itemPrices = itemPriceService.getServicePrice(billableService);
		if (!itemPrices.isEmpty()) {
			return itemPrices.get(0).getPrice();
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
