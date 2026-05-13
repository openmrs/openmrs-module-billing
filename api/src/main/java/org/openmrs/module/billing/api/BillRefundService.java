/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api;

import java.util.List;

import org.openmrs.annotation.Authorized;
import org.openmrs.module.billing.api.model.BillRefund;
import org.openmrs.module.billing.api.model.RefundStatus;
import org.openmrs.module.billing.api.util.PrivilegeConstants;

/**
 * Service for managing {@link BillRefund} records. Saving a refund also reconciles the parent
 * bill's status across the refund lifecycle.
 * <p>
 * Throughout this interface, a refund is considered <b>"active"</b> when {@code voided=false} AND
 * its {@link RefundStatus} is {@code REQUESTED} or {@code APPROVED}. {@code COMPLETED} and
 * {@code REJECTED} refunds are not active — completing or rejecting frees the scope so further
 * refunds can target any remaining unrefunded amount.
 */
public interface BillRefundService {
	
	/** @return the refund with the given id, or {@code null} */
	@Authorized(PrivilegeConstants.VIEW_REFUNDS)
	BillRefund getBillRefundById(Integer id);
	
	/** @return the refund with the given UUID, or {@code null} */
	@Authorized(PrivilegeConstants.VIEW_REFUNDS)
	BillRefund getBillRefundByUuid(String uuid);
	
	/** @return the active bill-level refund on the bill, or {@code null} */
	@Authorized(PrivilegeConstants.VIEW_REFUNDS)
	BillRefund getActiveBillRefund(Integer billId);
	
	/** @return the active refund targeting the given line item, or {@code null} */
	@Authorized(PrivilegeConstants.VIEW_REFUNDS)
	BillRefund getActiveLineItemRefund(Integer lineItemId);
	
	/** @return every active line-scoped refund on the bill; never {@code null} */
	@Authorized(PrivilegeConstants.VIEW_REFUNDS)
	List<BillRefund> getActiveLineScopedRefunds(Integer billId);
	
	/**
	 * Returns every refund associated with the bill, including voided and finalized
	 * (REJECTED/COMPLETED) rows, ordered newest-first by {@code dateCreated}. Never returns null.
	 */
	@Authorized(PrivilegeConstants.VIEW_REFUNDS)
	List<BillRefund> getRefundsByBillId(Integer billId);
	
	/**
	 * Inserts or updates the refund and reconciles the parent bill's status. Side effects:
	 * <ul>
	 * <li>Transition timestamps ({@code dateApproved}/{@code dateCompleted}) are auto-stamped
	 * server-side when status moves into {@code APPROVED}/{@code REJECTED} or {@code COMPLETED}.</li>
	 * <li>Parent bill status reconciliation runs under a scoped {@code MANAGE_BILLS} proxy
	 * privilege.</li>
	 * </ul>
	 */
	@Authorized({ PrivilegeConstants.REQUEST_REFUNDS, PrivilegeConstants.APPROVE_REFUNDS,
	        PrivilegeConstants.COMPLETE_REFUNDS })
	BillRefund saveBillRefund(BillRefund billRefund);
	
	/**
	 * Reads the persisted {@link RefundStatus} for the refund via a projection that returns just the
	 * status column (the BillRefund entity is not hydrated). Used by the validator to detect status
	 * transitions on update.
	 *
	 * @return the persisted status, or null if no refund with that id exists.
	 */
	@Authorized(PrivilegeConstants.VIEW_REFUNDS)
	RefundStatus getStatusById(Integer id);
}
