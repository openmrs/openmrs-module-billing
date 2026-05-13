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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "bill_refund")
public class BillRefund extends BaseOpenmrsData {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "bill_refund_id")
	private Integer billRefundId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bill_id", nullable = false)
	private Bill bill;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bill_line_item_id")
	private BillLineItem lineItem;
	
	@Column(name = "refund_amount", nullable = false)
	private BigDecimal refundAmount;
	
	@Column(name = "reason", nullable = false, length = 1000)
	private String reason;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "refund_status", nullable = false, length = 20)
	private RefundStatus status = RefundStatus.REQUESTED;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "initiator_id", nullable = false)
	private User initiator;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "approver_id")
	private User approver;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "completer_id")
	private User completer;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "date_approved")
	private Date dateApproved;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "date_completed")
	private Date dateCompleted;
	
	@Override
	public Integer getId() {
		return billRefundId;
	}
	
	@Override
	public void setId(Integer id) {
		this.billRefundId = id;
	}
}
