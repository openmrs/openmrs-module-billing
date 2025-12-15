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
package org.openmrs.module.billing.api.model;

import java.math.BigDecimal;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.module.stockmanagement.api.model.StockItem;

public class CashierItemPrice extends BaseOpenmrsData {
	
	public static final long serialVersionUID = 0L;
	
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
