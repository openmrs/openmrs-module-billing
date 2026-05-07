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
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.util.PrivilegeConstants;
import org.openmrs.module.stockmanagement.api.model.StockItem;

/**
 * Model class that represents a list of {@link BillLineItem}s and {@link Payment}s created by a
 * cashier for a patient.
 */
@Getter
@Setter
@Slf4j
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
	
	private Set<BillDiscount> discounts;
	
	/**
	 * Returns every non-voided discount on this bill (bill-level and line-item scoped). Voided rows are
	 * excluded — for the full audit history, query {@code BillDiscountService.getDiscountsByBillId} (or
	 * the equivalent REST search at {@code /billDiscount?bill=<uuid>}).
	 */
	public List<BillDiscount> getActiveDiscounts() {
		if (discounts == null) {
			return Collections.emptyList();
		}
		List<BillDiscount> active = new ArrayList<>();
		for (BillDiscount d : discounts) {
			if (d != null && !d.getVoided()) {
				active.add(d);
			}
		}
		return active;
	}
	
	/**
	 * Bill total net of every approved, non-voided discount. Pending and rejected discounts are visible
	 * on the bill but do not affect the total or the status flip.
	 */
	public BigDecimal getAmountAfterDiscount() {
		return effectiveTotal().max(BigDecimal.ZERO);
	}
	
	/**
	 * @return {@code true} when the sum of approved discount amounts exceeds {@link #getTotal()} —
	 *         typically because a line item was voided after a FIXED_AMOUNT discount was approved.
	 *         Callers (notably {@link #synchronizeBillStatus()}) should refuse to auto-flip PAID in
	 *         this state so the drift surfaces for manual reconciliation.
	 */
	public boolean hasDiscountDrift() {
		return effectiveTotal().compareTo(BigDecimal.ZERO) < 0;
	}
	
	private BigDecimal effectiveTotal() {
		BigDecimal total = getTotal();
		if (discounts != null) {
			for (BillDiscount d : discounts) {
				if (d != null && !d.getVoided() && d.getStatus() == DiscountStatus.APPROVED) {
					total = total.subtract(d.getDiscountAmount());
				}
			}
		}
		return total;
	}
	
	public BigDecimal getTotal() {
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
	
	public void addDiscount(BillDiscount discount) {
		if (discount == null) {
			throw new NullPointerException("The discount to add must be defined.");
		}
		if (this.discounts == null) {
			this.discounts = new HashSet<>();
		}
		this.discounts.add(discount);
		discount.setBill(this);
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
			// Approved discount exceeds the current bill total — likely a line item was voided
			// after approval. Stay POSTED so a human can void/reapply rather than letting any
			// non-zero payment silently flip the bill to PAID.
			if (hasDiscountDrift()) {
				log.warn("Bill {} has discount drift (total={}, effectiveTotal={}); staying POSTED for manual review",
				    getUuid(), getTotal(), effectiveTotal());
				this.setStatus(BillStatus.POSTED);
				return;
			}
			boolean billFullySettled = getTotalPayments().compareTo(getAmountAfterDiscount()) >= 0;
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
