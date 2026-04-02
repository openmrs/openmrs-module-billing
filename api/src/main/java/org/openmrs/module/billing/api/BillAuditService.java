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
package org.openmrs.module.billing.api;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillAudit;
import org.openmrs.module.billing.api.model.BillAuditAction;
import org.openmrs.module.billing.api.util.PrivilegeConstants;

import java.util.Date;
import java.util.List;

/**
 * Service interface for managing bill audit trail operations.
 */
public interface BillAuditService extends OpenmrsService {
	
	/**
	 * Saves an audit entry to the database.
	 *
	 * @param audit the audit entry to save
	 * @return the saved audit entry with updated metadata
	 * @throws org.openmrs.api.APIAuthenticationException if the user lacks MANAGE_BILLS privilege
	 */
	@Authorized(PrivilegeConstants.MANAGE_BILLS)
	BillAudit saveBillAudit(BillAudit audit);
	
	/**
	 * Retrieves an audit entry by its database ID.
	 *
	 * @param id the database ID of the audit entry
	 * @return the audit entry with the specified ID, or null if not found
	 * @throws org.openmrs.api.APIAuthenticationException if the user lacks VIEW_BILLS privilege
	 */
	@Authorized(PrivilegeConstants.VIEW_BILLS)
	BillAudit getBillAudit(Integer id);
	
	/**
	 * Retrieves an audit entry by its UUID.
	 *
	 * @param uuid the UUID of the audit entry
	 * @return the audit entry with the specified UUID, or null if not found
	 * @throws org.openmrs.api.APIAuthenticationException if the user lacks VIEW_BILLS privilege
	 */
	@Authorized(PrivilegeConstants.VIEW_BILLS)
	BillAudit getBillAuditByUuid(String uuid);
	
	/**
	 * Retrieves the complete audit history for a specific bill.
	 *
	 * @param bill the bill whose audit history to retrieve
	 * @param pagingInfo optional paging information (can be null for no paging)
	 * @return a list of audit entries for the bill, ordered by audit date descending, or an empty list
	 *         if none found
	 * @throws org.openmrs.api.APIAuthenticationException if the user lacks VIEW_BILLS privilege
	 */
	@Authorized(PrivilegeConstants.VIEW_BILLS)
	List<BillAudit> getBillAuditHistory(Bill bill, PagingInfo pagingInfo);
	
	/**
	 * Retrieves audit entries for a specific bill filtered by action type.
	 *
	 * @param bill the bill whose audit history to retrieve
	 * @param action the action type to filter by
	 * @param pagingInfo optional paging information (can be null for no paging)
	 * @return a list of audit entries matching the criteria, ordered by audit date descending, or an
	 *         empty list if none found
	 * @throws org.openmrs.api.APIAuthenticationException if the user lacks VIEW_BILLS privilege
	 */
	@Authorized(PrivilegeConstants.VIEW_BILLS)
	List<BillAudit> getBillAuditsByAction(Bill bill, BillAuditAction action, PagingInfo pagingInfo);
	
	/**
	 * Retrieves audit entries for a specific bill within a date range.
	 *
	 * @param bill the bill whose audit history to retrieve
	 * @param startDate the start date of the range (inclusive, can be null for no lower bound)
	 * @param endDate the end date of the range (inclusive, can be null for no upper bound)
	 * @param pagingInfo optional paging information (can be null for no paging)
	 * @return a list of audit entries within the date range, ordered by audit date descending, or an
	 *         empty list if none found
	 * @throws org.openmrs.api.APIAuthenticationException if the user lacks VIEW_BILLS privilege
	 */
	@Authorized(PrivilegeConstants.VIEW_BILLS)
	List<BillAudit> getBillAuditsByDateRange(Bill bill, Date startDate, Date endDate, PagingInfo pagingInfo);
	
	/**
	 * Creates and saves an audit entry for a bill modification.
	 *
	 * @param bill the bill that was modified
	 * @param action the type of action performed
	 * @param fieldName the name of the field that was changed (can be null)
	 * @param oldValue the previous value (can be null)
	 * @param newValue the new value (can be null)
	 * @param reason the reason for the change (can be null)
	 * @return the created audit entry
	 * @throws org.openmrs.api.APIAuthenticationException if the user lacks MANAGE_BILLS privilege
	 */
	@Authorized(PrivilegeConstants.MANAGE_BILLS)
	BillAudit createBillAudit(Bill bill, BillAuditAction action, String fieldName, String oldValue, String newValue,
	        String reason);
	
	/**
	 * Permanently deletes an audit entry from the database.
	 *
	 * @param audit the audit entry to delete
	 * @throws org.openmrs.api.APIAuthenticationException if the user lacks PURGE_BILLS privilege
	 */
	@Authorized(PrivilegeConstants.PURGE_BILLS)
	void purgeBillAudit(BillAudit audit);
}
