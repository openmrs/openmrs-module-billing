/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.db;

import java.util.List;

import org.openmrs.module.billing.api.model.BillRefund;
import org.openmrs.module.billing.api.model.RefundStatus;

/** Data-access contract for {@link BillRefund}. */
public interface BillRefundDAO {
	
	/** @return the refund for the given primary key, or {@code null} */
	BillRefund getBillRefundById(Integer id);
	
	/** @return the refund with the given UUID, or {@code null} */
	BillRefund getBillRefundByUuid(String uuid);
	
	/** @return the active bill-level refund on the bill, or {@code null} */
	BillRefund getActiveBillRefund(Integer billId);
	
	/** @return the active refund targeting the given line item, or {@code null} */
	BillRefund getActiveLineItemRefund(Integer lineItemId);
	
	/** @return every active line-scoped refund on the bill; never {@code null} */
	List<BillRefund> getActiveLineScopedRefunds(Integer billId);
	
	/**
	 * @return every refund on the bill (including voided/finalized), newest first; never {@code null}
	 */
	List<BillRefund> getRefundsByBillId(Integer billId);
	
	/** @return the persisted status via a scalar query (does not load the entity), or {@code null} */
	RefundStatus getStatusById(Integer id);
	
	/** Inserts or updates the refund. */
	BillRefund saveBillRefund(BillRefund billRefund);
}
