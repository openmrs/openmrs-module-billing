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
import java.math.RoundingMode;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.util.PrivilegeConstants;
import org.openmrs.module.stockmanagement.api.model.StockItem;

/**
 * Model class that represents a list of {@link BillLineItem}s and {@link Payment}s created by a
 * cashier for a patient.
 */
@Getter
@Setter
public class Bill extends BaseOpenmrsData {
	
	private static final long serialVersionUID = 0L;
	
	private Integer billId;
	
	private String receiptNumber;
	
	private Provider cashier;
	
	private Patient patient;
	
	private CashPoint cashPoint;
	
	private Bill billAdjusted;
	
	private BillStatus status;
	
	private List<BillLineItem> lineItems;
	
	private Set<Payment> payments;
	
	private Set<Bill> adjustedBy;
	
	private Boolean receiptPrinted = false;
	
	private String adjustmentReason;
	
	private DiscountType discountType;
	
	private BigDecimal discountValue;
	
	private BigDecimal discountAmount;
	
	private String discountReason;
	
	private DiscountStatus discountStatus;
	
	private User discountInitiator;
	
	private User discountApprover;
	
	private Date discountDateInitiated;
	
	private Date discountDateApproved;
	
	public BigDecimal getLineItemsTotal() {
		BigDecimal total = BigDecimal.ZERO;
		
		List<BillLineItem> lineItems = getLineItems();
		if (lineItems != null) {
			for (BillLineItem line : lineItems) {
				if (line != null && !line.getVoided()) {
					total = total.add(line.getTotal());
				}
			}
		}
		
		return total;
	}
	
	public BigDecimal getTotal() {
		BigDecimal total = getLineItemsTotal();
		
		if (discountStatus == DiscountStatus.APPROVED && discountAmount != null) {
			total = total.subtract(discountAmount);
		}
		
		return total;
	}
	
	public BigDecimal getTotalPayments() {
		BigDecimal total = BigDecimal.ZERO;
		
		Set<Payment> payments = getPayments();
		if (payments != null) {
			for (Payment payment : payments) {
				if (payment != null && !payment.getVoided()) {
					total = total.add(payment.getAmountTendered());
				}
			}
		}
		
		return total;
	}
	
	@Override
	public Integer getId() {
		return this.getBillId();
	}
	
	@Override
	public void setId(Integer id) {
		this.setBillId(id);
	}
	
	// Custom setter - updates adjusted bill status
	public void setBillAdjusted(Bill billAdjusted) {
		this.billAdjusted = billAdjusted;
		
		if (billAdjusted != null) {
			billAdjusted.setStatus(BillStatus.ADJUSTED);
		}
	}
	
	public BillLineItem addLineItem(StockItem item, BigDecimal price, String priceName, int quantity) {
		if (item == null) {
			throw new IllegalArgumentException("The item to add must be defined.");
		}
		if (price == null) {
			throw new IllegalArgumentException("The item price must be defined.");
		}
		
		BillLineItem lineItem = new BillLineItem();
		lineItem.setBill(this);
		lineItem.setItem(item);
		lineItem.setPrice(price);
		lineItem.setPriceName(priceName);
		lineItem.setQuantity(quantity);
		
		addLineItem(lineItem);
		
		return lineItem;
	}
	
	public void addLineItem(BillLineItem item) {
		if (item == null) {
			throw new NullPointerException("The list item to add must be defined.");
		}
		
		if (this.lineItems == null) {
			this.lineItems = new ArrayList<>();
		}
		
		this.lineItems.add(item);
		item.setBill(this);
	}
	
	public void removeLineItem(BillLineItem item) {
		if (item != null) {
			if (this.lineItems != null) {
				this.lineItems.remove(item);
			}
		}
	}
	
	public void addPayment(Payment payment) {
		if (payment == null) {
			throw new NullPointerException("The payment to add must be defined.");
		}
		
		if (this.payments == null) {
			this.payments = new HashSet<>();
		}
		
		this.payments.add(payment);
		payment.setBill(this);
		
		this.synchronizeBillStatus();
	}
	
	public void synchronizeBillStatus() {
		if (!this.getPayments().isEmpty() && getTotalPayments().compareTo(BigDecimal.ZERO) > 0) {
			boolean billFullySettled = getTotalPayments().compareTo(getTotal()) >= 0;
			if (billFullySettled) {
				this.setStatus(BillStatus.PAID);
				// Update all non-voided bill line items to PAID status
				if (this.lineItems != null) {
					for (BillLineItem lineItem : this.lineItems) {
						if (lineItem != null && !lineItem.getVoided()) {
							lineItem.setPaymentStatus(BillStatus.PAID);
						}
					}
				}
			} else {
				this.setStatus(BillStatus.POSTED);
			}
		}
	}
	
	public void initiateDiscount(DiscountType type, BigDecimal value, String reason) {
		if (status != null && status != BillStatus.PENDING && status != BillStatus.POSTED) {
			throw new APIException("billing.discount.invalidBillStatus");
		}
		if (discountStatus != null) {
			throw new APIException("billing.discount.alreadyExists");
		}
		if (type == null || value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
			throw new APIException("Discount type and a positive discount value are required.");
		}
		if (StringUtils.isBlank(reason)) {
			throw new APIException("A discount reason is required.");
		}
		
		BigDecimal computedAmount;
		BigDecimal lineTotal = getLineItemsTotal();
		if (type == DiscountType.PERCENTAGE) {
			if (value.compareTo(new BigDecimal("100")) > 0) {
				throw new APIException("billing.discount.percentageExceeds100");
			}
			computedAmount = lineTotal.multiply(value).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
		} else {
			computedAmount = value;
		}
		
		if (computedAmount.compareTo(lineTotal) > 0) {
			throw new APIException("billing.discount.exceedsBillTotal");
		}
		
		this.discountType = type;
		this.discountValue = value;
		this.discountReason = reason;
		this.discountAmount = computedAmount;
		this.discountStatus = DiscountStatus.PENDING;
		this.discountInitiator = Context.getAuthenticatedUser();
		this.discountDateInitiated = new Date();
	}
	
	public void approveDiscount() {
		if (!Context.hasPrivilege(PrivilegeConstants.MANAGE_DISCOUNTS)) {
			throw new APIException("You do not have permission to approve discounts.");
		}
		if (discountStatus != DiscountStatus.PENDING) {
			throw new APIException("billing.discount.notPending");
		}
		this.discountStatus = DiscountStatus.APPROVED;
		this.discountApprover = Context.getAuthenticatedUser();
		this.discountDateApproved = new Date();
		synchronizeBillStatus();
	}
	
	public void rejectDiscount() {
		if (discountStatus != DiscountStatus.PENDING) {
			throw new APIException("billing.discount.notPending");
		}
		this.discountStatus = DiscountStatus.REJECTED;
	}
	
	public void removePayment(Payment payment) {
		if (payment != null && this.payments != null) {
			this.payments.remove(payment);
		}
	}
	
	public void addAdjustedBy(Bill adjustedBill) {
		checkAuthorizedToAdjust();
		if (adjustedBill == null) {
			throw new NullPointerException("The adjusted bill to add must be defined.");
		}
		
		if (this.adjustedBy == null) {
			this.adjustedBy = new HashSet<>();
		}
		
		adjustedBill.setBillAdjusted(this);
		this.adjustedBy.add(adjustedBill);
	}
	
	private void checkAuthorizedToAdjust() {
		if (!Context.hasPrivilege(PrivilegeConstants.ADJUST_BILLS)) {
			throw new AccessControlException("Access denied to adjust bill.");
		}
	}
	
	/**
	 * Checks if the bill is in PENDING state.
	 * 
	 * @return {@code true} if the bill is new (no ID) or is in PENDING state, {@code false} otherwise
	 */
	public boolean editable() {
		// New bills (no ID) are considered pending, existing bills must be in PENDING
		// state
		return getStatus() == null || this.getId() == null || this.getStatus() == BillStatus.PENDING;
	}
	
	public void recalculateLineItemOrder() {
		int orderCounter = 0;
		for (BillLineItem lineItem : this.getLineItems()) {
			lineItem.setLineItemOrder(orderCounter++);
		}
	}
	
}
