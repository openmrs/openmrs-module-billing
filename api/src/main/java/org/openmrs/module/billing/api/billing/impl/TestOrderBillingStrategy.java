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
import org.openmrs.Order;
import org.openmrs.TestOrder;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillableServiceService;
import org.openmrs.module.billing.api.ItemPriceService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.BillableServiceStatus;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.openmrs.module.billing.api.model.ExemptionType;
import org.openmrs.module.billing.api.search.BillableServiceSearch;

/**
 * Default billing strategy for {@link TestOrder}s. Creates a bill line item based on the billable
 * service linked to the test order's concept.
 */
@Slf4j
public class TestOrderBillingStrategy extends AbstractOrderBillingStrategy {
	
	@Override
	public boolean supports(Order order) {
		if (!(order instanceof TestOrder)) {
			return false;
		}
		Order.Action action = order.getAction();
		return action == Order.Action.NEW || action == Order.Action.REVISE || action == Order.Action.DISCONTINUE;
	}
	
	@Override
	protected Optional<Bill> handleNewOrder(Order order) {
		TestOrder testOrder = (TestOrder) order;
		
		BillableServiceService serviceService = Context.getService(BillableServiceService.class);
		BillableServiceSearch searchTemplate = new BillableServiceSearch();
		searchTemplate.setConceptUuid(testOrder.getConcept().getUuid());
		searchTemplate.setServiceStatus(BillableServiceStatus.ENABLED);
		
		List<BillableService> searchResult = serviceService.getBillableServices(searchTemplate, null);
		if (searchResult.isEmpty()) {
			log.debug("No billable service found for concept: {}", testOrder.getConcept().getUuid());
			return Optional.empty();
		}
		
		boolean isExempted = checkIfOrderIsExempted(order, ExemptionType.SERVICE);
		BillStatus lineItemStatus = isExempted ? BillStatus.EXEMPTED : BillStatus.PENDING;
		
		BillableService billableService = searchResult.get(0);
		BillLineItem lineItem = createLineItem(billableService, lineItemStatus, order);
		
		return saveBill(order.getPatient(), lineItem, order);
	}
	
	private BillLineItem createLineItem(BillableService billableService, BillStatus lineItemStatus, Order order) {
		ItemPriceService priceService = Context.getService(ItemPriceService.class);
		
		BillLineItem lineItem = new BillLineItem();
		lineItem.setBillableService(billableService);
		
		List<CashierItemPrice> itemPrices = priceService.getServicePrice(billableService);
		if (!itemPrices.isEmpty()) {
			lineItem.setPrice(itemPrices.get(0).getPrice());
		} else {
			lineItem.setPrice(BigDecimal.ZERO);
		}
		
		lineItem.setQuantity(1);
		lineItem.setPaymentStatus(lineItemStatus);
		lineItem.setLineItemOrder(0);
		lineItem.setOrder(order);
		
		return lineItem;
	}
}
