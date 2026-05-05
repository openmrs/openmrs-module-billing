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

import lombok.Getter;
import lombok.Setter;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "bill_discount")
public class BillDiscount extends BaseOpenmrsData {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "bill_discount_id")
	private Integer billDiscountId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bill_id", nullable = false)
	private Bill bill;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bill_line_item_id")
	private BillLineItem lineItem;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "discount_type", nullable = false)
	private DiscountType discountType;
	
	@Column(name = "discount_value", nullable = false)
	private BigDecimal discountValue;
	
	@Column(name = "discount_amount", nullable = false)
	private BigDecimal discountAmount;
	
	@Column(name = "justification", nullable = false, length = 1000)
	private String justification;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "initiator_id", nullable = false)
	private User initiator;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "approver_id")
	private User approver;
	
	@Override
	public Integer getId() {
		return billDiscountId;
	}
	
	@Override
	public void setId(Integer id) {
		this.billDiscountId = id;
	}
}
