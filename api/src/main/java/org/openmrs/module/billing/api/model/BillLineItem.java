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
import java.util.Objects;

import org.openmrs.BaseChangeableOpenmrsData;
import org.openmrs.Order;
import org.openmrs.module.stockmanagement.api.model.StockItem;

/**
 * A LineItem represents a line on a {@link Bill} which will bill some quantity of a particular
 * {@link StockItem}.
 */
public class BillLineItem extends BaseChangeableOpenmrsData {
	
	private static final long serialVersionUID = 0L;
	
	private int billLineItemId;
	
	private Bill bill;
	
	private StockItem item;
	
	private BillableService billableService;
	
	private BigDecimal price;
	
	private String priceName;
	
	private CashierItemPrice itemPrice;
	
	private Integer quantity;
	
	private Integer lineItemOrder;
	
	private BillStatus paymentStatus; // this should only be set to either
	// pending or paid
	
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
	
	public CashierItemPrice getItemPrice() {
		return itemPrice;
	}
	
	public void setItemPrice(CashierItemPrice itemPrice) {
		this.itemPrice = itemPrice;
	}
	
	public BillableService getBillableService() {
		return billableService;
	}
	
	public void setBillableService(BillableService billableService) {
		this.billableService = billableService;
	}
	
	public Integer getQuantity() {
		return quantity;
	}
	
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	
	public Bill getBill() {
		return bill;
	}
	
	public void setBill(Bill bill) {
		this.bill = bill;
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
	
	public String getPriceName() {
		return priceName;
	}
	
	public void setPriceName(String priceName) {
		this.priceName = priceName;
	}
	
	public Integer getLineItemOrder() {
		return lineItemOrder;
	}
	
	public void setLineItemOrder(Integer lineItemOrder) {
		this.lineItemOrder = lineItemOrder;
	}
	
	public BillStatus getPaymentStatus() {
		return paymentStatus;
	}
	
	public void setPaymentStatus(BillStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}
	
	public Order getOrder() {
		return order;
	}
	
	public void setOrder(Order order) {
		this.order = order;
	}
	
	/**
	 * Compares this line item with another based on key properties (quantity, price, item, etc.). This
	 * is used to detect actual changes in line items, not just object identity.
	 * 
	 * @param obj The object to compare with
	 * @return true if the line items have the same key properties, false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		BillLineItem that = (BillLineItem) obj;
		
		// Compare UUID (if both have UUIDs, they should match for same item)
		if (this.getUuid() != null && that.getUuid() != null) {
			if (!Objects.equals(this.getUuid(), that.getUuid())) {
				return false;
			}
		}
		
		// Compare key properties that determine if line items are actually different
		return Objects.equals(quantity, that.quantity) && areBigDecimalsEqual(price, that.price)
		        && Objects.equals(getItemUuid(), that.getItemUuid())
		        && Objects.equals(getBillableServiceUuid(), that.getBillableServiceUuid())
		        && Objects.equals(getVoided(), that.getVoided());
	}
	
	/**
	 * Returns hash code based on key properties.
	 * 
	 * @return hash code
	 */
	@Override
	public int hashCode() {
		return Objects.hash(getUuid(), quantity, price, getItemUuid(), getBillableServiceUuid(), getVoided());
	}
	
	/**
	 * Gets the UUID of the item, handling null cases.
	 * 
	 * @return item UUID or null
	 */
	private String getItemUuid() {
		return item != null ? item.getUuid() : null;
	}
	
	/**
	 * Gets the UUID of the billable service, handling null cases.
	 * 
	 * @return billable service UUID or null
	 */
	private String getBillableServiceUuid() {
		return billableService != null ? billableService.getUuid() : null;
	}
	
	/**
	 * Compares two BigDecimal values for equality, handling null cases.
	 * 
	 * @param bd1 First BigDecimal
	 * @param bd2 Second BigDecimal
	 * @return true if both are null or equal, false otherwise
	 */
	private boolean areBigDecimalsEqual(BigDecimal bd1, BigDecimal bd2) {
		if (bd1 == null && bd2 == null) {
			return true;
		}
		if (bd1 == null || bd2 == null) {
			return false;
		}
		return bd1.compareTo(bd2) == 0;
	}
}
