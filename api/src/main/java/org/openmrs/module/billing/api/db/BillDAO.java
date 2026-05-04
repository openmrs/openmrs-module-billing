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

import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.search.BillSearch;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Data Access Object (DAO) interface for {@link Bill} persistence operations.
 *
 * @see Bill
 * @see BillSearch
 */
public interface BillDAO {
	
	/**
	 * Retrieves a bill by its database ID.
	 *
	 * @param id the database ID of the bill (must not be null)
	 * @return the bill with the specified ID, or null if not found
	 */
	Bill getBill(@Nonnull Integer id);
	
	/**
	 * Retrieves a bill by its UUID.
	 * <p>
	 * Note: This method may return voided bills. Consider filtering voided records at the service layer
	 * if needed.
	 * </p>
	 *
	 * @param uuid the UUID of the bill (must not be null)
	 * @return the bill with the specified UUID, or null if not found
	 */
	Bill getBillByUuid(@Nonnull String uuid);
	
	/**
	 * Retrieves a bill by its receipt number.
	 * <p>
	 * Note: This method may return voided bills. Consider filtering voided records at the service layer
	 * if needed.
	 * </p>
	 *
	 * @param receiptNumber the receipt number of the bill (must not be null)
	 * @return the bill with the specified receipt number, or null if not found
	 */
	Bill getBillByReceiptNumber(@Nonnull String receiptNumber);
	
	/**
	 * Retrieves all bills for a specific patient.
	 * <p>
	 * Results are ordered by date created descending (most recent first). Note: This method may return
	 * voided bills. Consider filtering voided records at the service layer if needed.
	 * </p>
	 *
	 * @param patientUuid the UUID of the patient (must not be null)
	 * @param pagingInfo optional paging information (can be null for no paging)
	 * @return a list of bills for the patient, or an empty list if none found
	 */
	List<Bill> getBillsByPatientUuid(@Nonnull String patientUuid, PagingInfo pagingInfo);
	
	/**
	 * Searches for bills using the specified search criteria.
	 * <p>
	 * Results are ordered by date created descending (most recent first). By default, voided bills are
	 * excluded from results unless {@link BillSearch#setIncludeVoided(Boolean)} is set to true. The
	 * search criteria support filtering by patient, cashier, cash point, and status.
	 * </p>
	 *
	 * @param billSearch the search criteria (must not be null)
	 * @param pagingInfo optional paging information (can be null for no paging). When provided with
	 *            {@code loadRecordCount=true}, the total count will be populated in the pagingInfo
	 * @return a list of bills matching the search criteria, or an empty list if none found
	 * @see BillSearch
	 */
	List<Bill> getBills(@Nonnull BillSearch billSearch, PagingInfo pagingInfo);
	
	/**
	 * Persists a bill to the database.
	 * <p>
	 * If the bill has no ID, it will be created as a new record. If it has an ID, the existing record
	 * will be updated.
	 * </p>
	 *
	 * @param bill the bill to save (must not be null)
	 * @return the saved bill with updated metadata (timestamps, IDs, etc.)
	 */
	Bill saveBill(@Nonnull Bill bill);
	
	/**
	 * Permanently deletes a bill from the database.
	 * <p>
	 * <strong>Warning:</strong> This operation cannot be undone. All associated data (line items,
	 * payments, etc.) will also be removed due to cascade delete rules.
	 * </p>
	 *
	 * @param bill the bill to permanently delete (must not be null)
	 */
	void purgeBill(@Nonnull Bill bill);
	
	/**
	 * Reads the bill's status directly from the database, bypassing Hibernate's session cache. This
	 * returns the persisted (pre-mutation) value even when the managed entity in the current session
	 * has been modified, so callers can compare the DB truth against in-memory changes (e.g., for
	 * status-transition validation).
	 *
	 * @param billId the database ID of the bill (must not be null)
	 * @return the persisted status, or null if no bill with that ID exists
	 */
	BillStatus getPersistedBillStatus(@Nonnull Integer billId);
	
}
