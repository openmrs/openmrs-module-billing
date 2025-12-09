package org.openmrs.module.billing.api;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.search.BillSearch;
import org.openmrs.module.billing.api.util.PrivilegeConstants;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service interface for managing billing operations
 *
 * @see Bill
 * @see BillSearch
 */
public interface BillService extends OpenmrsService {
	
	/**
	 * Retrieves a bill by its database ID.
	 *
	 * @param id the database ID of the bill
	 * @return the bill with the specified ID, or null if not found
	 * @throws org.openmrs.api.APIAuthenticationException if the user lacks VIEW_BILLS privilege
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_BILLS)
	Bill getBill(Integer id);
	
	/**
	 * Retrieves a bill by its UUID.
	 *
	 * @param uuid the UUID of the bill
	 * @return the bill with the specified UUID, or null if not found
	 * @throws org.openmrs.api.APIAuthenticationException if the user lacks VIEW_BILLS privilege
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_BILLS)
	Bill getBillByUuid(String uuid);
	
	/**
	 * Retrieves a bill by its receipt number.
	 *
	 * @param receiptNumber the receipt number of the bill
	 * @return the bill with the specified receipt number, or null if not found
	 * @throws org.openmrs.api.APIAuthenticationException if the user lacks VIEW_BILLS privilege
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_BILLS)
	Bill getBillByReceiptNumber(String receiptNumber);
	
	/**
	 * Retrieves all bills for a specific patient.
	 *
	 * @param patientUuid the UUID of the patient
	 * @param pagingInfo optional paging information (can be null for no paging)
	 * @return a list of bills for the patient, or an empty list if none found
	 * @throws org.openmrs.api.APIAuthenticationException if the user lacks VIEW_BILLS privilege
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_BILLS)
	List<Bill> getBillsByPatientUuid(String patientUuid, PagingInfo pagingInfo);
	
	/**
	 * Searches for bills using the specified search criteria.
	 * <p>
	 * By default, voided bills are excluded from search results unless explicitly included via
	 * {@link BillSearch#setIncludeVoided(Boolean)}.
	 * </p>
	 *
	 * @param billSearch the search criteria
	 * @param pagingInfo optional paging information (can be null for no paging)
	 * @return a list of bills matching the search criteria, or an empty list if none found
	 * @throws org.openmrs.api.APIAuthenticationException if the user lacks VIEW_BILLS privilege
	 * @see BillSearch
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_BILLS)
	List<Bill> getBills(BillSearch billSearch, PagingInfo pagingInfo);
	
	/**
	 * Generates and downloads a receipt for the specified bill.
	 *
	 * @param bill the bill for which to generate a receipt
	 * @return a byte array containing the receipt data (typically a PDF)
	 * @throws org.openmrs.api.APIAuthenticationException if the user lacks VIEW_BILLS privilege
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_BILLS)
	byte[] downloadBillReceipt(Bill bill);
	
	/**
	 * Saves a bill to the database.
	 * <p>
	 * If the bill is new (no ID), it will be created. If it already exists, it will be updated. The
	 * bill's status will be synchronized based on its payments.
	 * </p>
	 *
	 * @param bill the bill to save
	 * @return the saved bill with updated metadata
	 * @throws org.openmrs.api.APIAuthenticationException if the user lacks MANAGE_BILLS privilege
	 * @throws IllegalArgumentException if the bill is null or invalid
	 */
	@Transactional
	@Authorized(PrivilegeConstants.MANAGE_BILLS)
	Bill saveBill(Bill bill);
	
	/**
	 * Permanently deletes a bill from the database.
	 * <p>
	 * <strong>Warning:</strong> This operation cannot be undone. Consider using
	 * {@link #voidBill(Bill, String)} instead for soft deletion.
	 * </p>
	 *
	 * @param bill the bill to permanently delete
	 * @throws org.openmrs.api.APIAuthenticationException if the user lacks PURGE_BILLS privilege
	 */
	@Authorized(PrivilegeConstants.PURGE_BILLS)
	void purgeBill(Bill bill);
	
	/**
	 * Voids (soft deletes) a bill with a specified reason.
	 * <p>
	 * Voided bills are hidden from normal queries but remain in the database for audit purposes. Voided
	 * bills can be restored using {@link #unvoidBill(Bill)}.
	 * </p>
	 *
	 * @param bill the bill to void
	 * @param voidReason the reason for voiding the bill (required)
	 * @return the voided bill
	 * @throws org.openmrs.api.APIAuthenticationException if the user lacks DELETE_BILLS privilege
	 * @throws IllegalArgumentException if voidReason is null or empty
	 */
	@Authorized(PrivilegeConstants.DELETE_BILLS)
	Bill voidBill(Bill bill, String voidReason);
	
	/**
	 * Restores a previously voided bill.
	 * <p>
	 * This operation removes the void flag and makes the bill visible in normal queries again.
	 * </p>
	 *
	 * @param bill the bill to restore
	 * @return the restored bill
	 * @throws org.openmrs.api.APIAuthenticationException if the user lacks DELETE_BILLS privilege
	 */
	@Authorized(PrivilegeConstants.DELETE_BILLS)
	Bill unvoidBill(Bill bill);
	
}
