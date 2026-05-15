/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.model;

import java.math.BigDecimal;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.openmrs.BaseChangeableOpenmrsData;
import org.openmrs.Order;
import org.openmrs.module.stockmanagement.api.model.StockItem;

/**
 * A LineItem represents a line on a {@link Bill} which will bill some quantity of a particular
 * {@link StockItem}.
 */
@Setter
@Getter
public class BillLineItem extends BaseChangeableOpenmrsData {
	
	private static final long serialVersionUID = 0L;
	
	@Setter(AccessLevel.NONE)
	private Integer billLineItemId;
	
	private Bill bill;
	
	private StockItem item;
	
	private BillableService billableService;
	
	private BigDecimal price;
	
	private String priceName;
	
	private CashierItemPrice itemPrice;
	
	private Integer quantity;
	
	private Integer lineItemOrder;
	
	private BillLineItemStatus status;
	
	private Order order;
	
	@Override
	public Integer getId() {
		return billLineItemId;
	}
	
	@Override
	public void setId(Integer id) {
		billLineItemId = id;
	}
	
	/**
	 * Get the total price for the line item
	 *
	 * @return double the total price for the line item
	 */
	public BigDecimal getTotal() {
		return price.multiply(BigDecimal.valueOf(quantity));
	}
	
}
