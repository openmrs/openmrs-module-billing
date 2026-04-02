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

import lombok.Getter;
import lombok.Setter;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.User;

import javax.persistence.*;
import java.util.Date;

/**
 * Represents an audit trail entry for bill modifications.
 * <p>
 * The BillAudit entity captures every change made to a {@link Bill}, providing a complete history
 * of modifications for compliance, accountability, and troubleshooting purposes. Each audit entry
 * records what was changed, who made the change, when it occurred, and optionally why the
 * modification was necessary.
 * </p>
 * <h3>Purpose and Benefits</h3>
 * <p>
 * The audit trail serves several critical functions in healthcare financial systems:
 * </p>
 * <ul>
 * <li><strong>Regulatory Compliance:</strong> Maintains complete change history required by
 * healthcare regulations and financial auditing standards</li>
 * <li><strong>Accountability:</strong> Tracks which user made each modification, ensuring
 * transparency in financial operations</li>
 * <li><strong>Dispute Resolution:</strong> Provides detailed history to resolve billing disputes or
 * discrepancies</li>
 * <li><strong>Troubleshooting:</strong> Helps identify when and how errors were introduced into
 * bills</li>
 * <li><strong>Analytics:</strong> Enables analysis of billing patterns and operational
 * workflows</li>
 * </ul>
 * <h3>What is Tracked</h3>
 * <p>
 * The audit trail automatically captures the following types of changes:
 * </p>
 * <ul>
 * <li>Line item additions, removals, and modifications</li>
 * <li>Quantity and price changes for individual line items</li>
 * <li>Payment additions and removals</li>
 * <li>Bill status transitions (PENDING → POSTED → PAID)</li>
 * <li>Bill adjustments and adjustment reason updates</li>
 * <li>Bill void and unvoid operations</li>
 * </ul>
 * <h3>Automatic Operation</h3>
 * <p>
 * Audit entries are created automatically by the {@link org.openmrs.module.billing.api.BillService}
 * whenever bills are modified. Application code does not need to explicitly create audit entries -
 * the system handles this transparently by comparing the new bill state with the existing database
 * state and generating appropriate audit records for each detected change.
 * </p>
 * <h3>Usage Examples</h3>
 * <h4>Retrieving Audit History Programmatically</h4> <pre>
 * {@code
 * // Get the audit service
 * BillAuditService auditService = Context.getService(BillAuditService.class);
 * 
 * // Retrieve complete audit history for a bill
 * List<BillAudit> audits = auditService.getBillAuditHistory(bill, null);
 * 
 * // Filter by specific action type
 * List<BillAudit> lineItemChanges = auditService.getBillAuditsByAction(
 *     bill, 
 *     BillAuditAction.LINE_ITEM_ADDED, 
 *     null
 * );
 * 
 * // Filter by date range
 * Date startDate = // some date
 * Date endDate = // some date
 * List<BillAudit> recentChanges = auditService.getBillAuditsByDateRange(
 *     bill, 
 *     startDate, 
 *     endDate, 
 *     null
 * );
 * }
 * </pre>
 * <h4>Accessing Audit History via REST API</h4> <pre>
 * {@code
 * // Get complete audit history for a bill
 * GET /rest/v1/billing/billAudit?billUuid={uuid}
 * 
 * // Filter by action type
 * GET /rest/v1/billing/billAudit?billUuid={uuid}&action=LINE_ITEM_ADDED
 * 
 * // Filter by date range
 * GET /rest/v1/billing/billAudit?billUuid={uuid}&startDate=2024-01-01&endDate=2024-12-31
 * 
 * // With pagination
 * GET /rest/v1/billing/billAudit?billUuid={uuid}&page=1&limit=20
 * }
 * </pre>
 * <h3>Action Types</h3>
 * <p>
 * The {@link BillAuditAction} enum defines all possible audit actions:
 * </p>
 * <ul>
 * <li>{@link BillAuditAction#BILL_CREATED} - New bill was created</li>
 * <li>{@link BillAuditAction#LINE_ITEM_ADDED} - Line item was added to the bill</li>
 * <li>{@link BillAuditAction#LINE_ITEM_REMOVED} - Line item was removed from the bill</li>
 * <li>{@link BillAuditAction#LINE_ITEM_MODIFIED} - Line item was modified</li>
 * <li>{@link BillAuditAction#QUANTITY_CHANGED} - Line item quantity was changed</li>
 * <li>{@link BillAuditAction#PRICE_CHANGED} - Line item price was changed</li>
 * <li>{@link BillAuditAction#STATUS_CHANGED} - Bill status was changed</li>
 * <li>{@link BillAuditAction#PAYMENT_ADDED} - Payment was added to the bill</li>
 * <li>{@link BillAuditAction#PAYMENT_REMOVED} - Payment was removed from the bill</li>
 * <li>{@link BillAuditAction#BILL_ADJUSTED} - Bill was adjusted</li>
 * <li>{@link BillAuditAction#ADJUSTMENT_REASON_UPDATED} - Adjustment reason was updated</li>
 * <li>{@link BillAuditAction#BILL_VOIDED} - Bill was voided</li>
 * <li>{@link BillAuditAction#BILL_UNVOIDED} - Bill void was reversed</li>
 * </ul>
 * <h3>Data Storage Format</h3>
 * <p>
 * Old and new values are stored as JSON strings to provide flexibility in capturing different data
 * types and complex objects. For example, when a line item is added, the newValue field contains a
 * JSON representation of the line item including its UUID, item name, quantity, price, and total.
 * </p>
 * <h3>Security and Access Control</h3>
 * <p>
 * Access to audit trail information is controlled through the existing OpenMRS privilege system:
 * </p>
 * <ul>
 * <li>Users with "View Cashier Bills" privilege can view audit histories</li>
 * <li>Users with "Manage Cashier Bills" privilege can trigger audit logging through bill
 * modifications</li>
 * <li>Audit entries inherit the same security boundaries as the bills they document</li>
 * </ul>
 * 
 * @see Bill
 * @see BillAuditAction
 * @see org.openmrs.module.billing.api.BillAuditService
 * @since 1.4.0
 */
@Entity
@Table(name = "billing_bill_audit")
@Getter
@Setter
public class BillAudit extends BaseOpenmrsData {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * The unique identifier for this audit entry.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "bill_audit_id")
	private Integer billAuditId;
	
	/**
	 * The bill that was modified. This establishes a many-to-one relationship where each bill can have
	 * multiple audit entries tracking its complete modification history.
	 */
	@ManyToOne(optional = false)
	@JoinColumn(name = "bill_id", nullable = false)
	private Bill bill;
	
	/**
	 * The type of action that was performed on the bill. This categorizes the modification to enable
	 * filtering and analysis of specific change types.
	 * 
	 * @see BillAuditAction for all possible action types
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "action", nullable = false, length = 50)
	private BillAuditAction action;
	
	/**
	 * The name of the field that was changed. For simple field changes like status updates, this
	 * contains the field name (e.g., "status"). For line item changes, this may be null or contain a
	 * descriptor like "lineItem" or "lineItem[123].quantity".
	 */
	@Column(name = "field_name", length = 100)
	private String fieldName;
	
	/**
	 * The previous value before the change, stored as a JSON string. For new additions, this will be
	 * null. For removals, this contains the removed object. For modifications, this contains the state
	 * before the change.
	 * <p>
	 * Example for a quantity change: "5"
	 * </p>
	 * <p>
	 * Example for a line item addition: null (since there was no previous value)
	 * </p>
	 */
	@Column(name = "old_value", columnDefinition = "TEXT")
	private String oldValue;
	
	/**
	 * The new value after the change, stored as a JSON string. For additions, this contains the added
	 * object. For removals, this will be null. For modifications, this contains the state after the
	 * change.
	 * <p>
	 * Example for a quantity change: "10"
	 * </p>
	 * <p>
	 * Example for a line item addition: {"uuid":"...", "itemName":"Paracetamol", "quantity":10,
	 * "price":50.00}
	 * </p>
	 */
	@Column(name = "new_value", columnDefinition = "TEXT")
	private String newValue;
	
	/**
	 * Optional reason or explanation for why the change was made. This is particularly important for
	 * bill adjustments and voids where business justification is required. The reason may be
	 * user-provided or system-generated depending on the type of modification.
	 * <p>
	 * Example: "Customer requested additional medication"
	 * </p>
	 * <p>
	 * Example: "Bill created in error"
	 * </p>
	 */
	@Column(name = "reason", columnDefinition = "TEXT")
	private String reason;
	
	/**
	 * The authenticated user who performed the action that triggered this audit entry. This provides
	 * accountability by linking each change to a specific user account.
	 */
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	/**
	 * The timestamp when the audited action occurred. This is automatically set when the audit entry is
	 * created and provides precise timing information for change tracking and analysis.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "audit_date", nullable = false)
	private Date auditDate;
	
	@Override
	public Integer getId() {
		return billAuditId;
	}
	
	@Override
	public void setId(Integer id) {
		this.billAuditId = id;
	}
	
	/**
	 * Lifecycle callback that automatically sets the audit date to the current timestamp when the
	 * entity is first persisted. This ensures that every audit entry has an accurate creation timestamp
	 * without requiring explicit setting by application code.
	 */
	@PrePersist
	protected void onCreate() {
		if (auditDate == null) {
			auditDate = new Date();
		}
	}
}
