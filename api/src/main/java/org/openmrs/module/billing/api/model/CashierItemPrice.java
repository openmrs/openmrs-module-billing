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

import org.openmrs.BaseChangeableOpenmrsMetadata;
import org.openmrs.module.stockmanagement.api.model.StockItem;

public class CashierItemPrice extends BaseChangeableOpenmrsMetadata {
	
	private static final long serialVersionUID = 0L;
	
	private Integer itemPriceId;
	
	private String name;
	
	private BigDecimal price;
	
	private PaymentMode paymentMode;
	
	private StockItem item;
	
	private BillableService billableService;
	
	public CashierItemPrice() {
		
	}
	
	public CashierItemPrice(String name, BigDecimal price, StockItem item, BillableService billableService) {
		this.name = name;
		this.price = price;
		this.item = item;
		this.billableService = billableService;
	}
	
	public CashierItemPrice(BigDecimal price, String name) {
		super();
		
		this.price = price;
		setName(name);
	}
	
	@Override
	public Integer getId() {
		return itemPriceId;
	}
	
	@Override
	public void setId(Integer id) {
		itemPriceId = id;
	}
	
	public PaymentMode getPaymentMode() {
		return paymentMode;
	}
	
	public void setPaymentMode(PaymentMode paymentMode) {
		this.paymentMode = paymentMode;
	}
	
	public StockItem getItem() {
		return item;
	}
	
	public void setItem(StockItem item) {
		this.item = item;
	}
	
	public BigDecimal getPrice() {
		return price;
	}
	
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public BillableService getBillableService() {
		return billableService;
	}
	
	public void setBillableService(BillableService billableService) {
		this.billableService = billableService;
	}
}
