package org.openmrs.module.billing.api.db;

import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.search.BillSearch;
import org.springframework.transaction.annotation.Transactional;

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
	@Transactional(readOnly = true)
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
	@Transactional(readOnly = true)
	Bill getBillByUuid(@Nonnull String uuid);
	
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
	@Transactional
	Bill saveBill(@Nonnull Bill bill);
	
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
	@Transactional(readOnly = true)
	Bill getBillByReceiptNumber(@Nonnull String receiptNumber);
	
	/**
	 * Retrieves all bills for a specific patient.
	 * <p>
	 * Note: This method may return voided bills. Consider filtering voided records at the service layer
	 * if needed.
	 * </p>
	 *
	 * @param patientUuid the UUID of the patient (must not be null)
	 * @param pagingInfo optional paging information (can be null for no paging)
	 * @return a list of bills for the patient, or an empty list if none found
	 */
	@Transactional(readOnly = true)
	List<Bill> getBillsByPatientUuid(@Nonnull String patientUuid, PagingInfo pagingInfo);
	
	/**
	 * Searches for bills using the specified search criteria.
	 * <p>
	 * By default, voided bills are excluded from results unless
	 * {@link BillSearch#setIncludeVoided(Boolean)} is set to true. The search criteria support
	 * filtering by patient, cashier, cash point, and status.
	 * </p>
	 *
	 * @param billSearch the search criteria (must not be null)
	 * @param pagingInfo optional paging information (can be null for no paging). When provided with
	 *            {@code loadRecordCount=true}, the total count will be populated in the pagingInfo
	 * @return a list of bills matching the search criteria, or an empty list if none found
	 * @see BillSearch
	 */
	@Transactional(readOnly = true)
	List<Bill> getBills(@Nonnull BillSearch billSearch, PagingInfo pagingInfo);
	
	/**
	 * Permanently deletes a bill from the database.
	 * <p>
	 * <strong>Warning:</strong> This operation cannot be undone. All associated data (line items,
	 * payments, etc.) will also be removed due to cascade delete rules.
	 * </p>
	 *
	 * @param bill the bill to permanently delete (must not be null)
	 */
	@Transactional
	void purgeBill(@Nonnull Bill bill);
	
}
