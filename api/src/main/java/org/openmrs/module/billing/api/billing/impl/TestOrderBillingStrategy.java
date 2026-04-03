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
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.module.billing.api.BillableServiceService;
import org.openmrs.module.billing.api.ItemPriceService;
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
		return HibernateUtil.getRealObjectFromProxy(order) instanceof TestOrder && isSupportedAction(order);
	}
	
	@Override
	protected Optional<BillLineItem> handleNewOrder(Order order) {
		TestOrder testOrder = (TestOrder) order;
		
		if (testOrder.getConcept() == null) {
			log.warn("TestOrder {} has no concept set, cannot generate bill", order.getUuid());
			return Optional.empty();
		}
		
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
		BillLineItem lineItem = createLineItem(resolvePrice(billableService), 1, lineItemStatus, order);
		lineItem.setBillableService(billableService);
		return Optional.of(lineItem);
	}
	
	private BigDecimal resolvePrice(BillableService billableService) {
		ItemPriceService priceService = Context.getService(ItemPriceService.class);
		List<CashierItemPrice> itemPrices = priceService.getServicePrice(billableService);
		if (!itemPrices.isEmpty()) {
			return itemPrices.get(0).getPrice();
		}
		return BigDecimal.ZERO;
	}
}
