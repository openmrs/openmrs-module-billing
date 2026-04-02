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
package org.openmrs.module.billing.api.db;

import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillAudit;
import org.openmrs.module.billing.api.model.BillAuditAction;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.List;

/**
 * Data Access Object interface for {@link BillAudit} persistence operations.
 */
public interface BillAuditDAO {
	
	/**
	 * Saves an audit entry to the database.
	 *
	 * @param audit the audit entry to save (must not be null)
	 * @return the saved audit entry with updated metadata
	 */
	BillAudit saveBillAudit(@Nonnull BillAudit audit);
	
	/**
	 * Retrieves an audit entry by its database ID.
	 *
	 * @param id the database ID of the audit entry (must not be null)
	 * @return the audit entry with the specified ID, or null if not found
	 */
	BillAudit getBillAudit(@Nonnull Integer id);
	
	/**
	 * Retrieves an audit entry by its UUID.
	 *
	 * @param uuid the UUID of the audit entry (must not be null)
	 * @return the audit entry with the specified UUID, or null if not found
	 */
	BillAudit getBillAuditByUuid(@Nonnull String uuid);
	
	/**
	 * Retrieves the complete audit history for a specific bill.
	 *
	 * @param bill the bill whose audit history to retrieve (must not be null)
	 * @param pagingInfo optional paging information (can be null for no paging)
	 * @return a list of audit entries for the bill, ordered by audit date descending, or an empty list
	 *         if none found
	 */
	List<BillAudit> getBillAuditHistory(@Nonnull Bill bill, PagingInfo pagingInfo);
	
	/**
	 * Retrieves audit entries for a specific bill filtered by action type.
	 *
	 * @param bill the bill whose audit history to retrieve (must not be null)
	 * @param action the action type to filter by (must not be null)
	 * @param pagingInfo optional paging information (can be null for no paging)
	 * @return a list of audit entries matching the criteria, ordered by audit date descending, or an
	 *         empty list if none found
	 */
	List<BillAudit> getBillAuditsByAction(@Nonnull Bill bill, @Nonnull BillAuditAction action, PagingInfo pagingInfo);
	
	/**
	 * Retrieves audit entries for a specific bill within a date range.
	 *
	 * @param bill the bill whose audit history to retrieve (must not be null)
	 * @param startDate the start date of the range (inclusive, can be null for no lower bound)
	 * @param endDate the end date of the range (inclusive, can be null for no upper bound)
	 * @param pagingInfo optional paging information (can be null for no paging)
	 * @return a list of audit entries within the date range, ordered by audit date descending, or an
	 *         empty list if none found
	 */
	List<BillAudit> getBillAuditsByDateRange(@Nonnull Bill bill, Date startDate, Date endDate, PagingInfo pagingInfo);
	
	/**
	 * Permanently deletes an audit entry from the database.
	 *
	 * @param audit the audit entry to delete (must not be null)
	 */
	void purgeBillAudit(@Nonnull BillAudit audit);
}
