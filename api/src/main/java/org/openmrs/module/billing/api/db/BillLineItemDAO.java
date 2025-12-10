package org.openmrs.module.billing.api.db;

import org.openmrs.module.billing.api.model.BillLineItem;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;

/**
 * Data Access Object (DAO) interface for bill line item persistence operations.
 */
public interface BillLineItemDAO {
	
	/**
	 * Retrieves a bill line item from the database by its primary key ID.
	 *
	 * @param id the primary key ID of the bill line item to retrieve
	 * @return the bill line item with the specified ID, or null if not found
	 */
	@Transactional(readOnly = true)
	BillLineItem getBillLineItem(@Nonnull Integer id);
	
	/**
	 * Retrieves a bill line item from the database by its UUID.
	 *
	 * @param uuid the UUID of the bill line item to retrieve
	 * @return the bill line item with the specified UUID
	 * @throws javax.persistence.NoResultException if no bill line item with the UUID exists
	 */
	@Transactional(readOnly = true)
	BillLineItem getBillLineItemByUuid(@Nonnull String uuid);
	
	/**
	 * Persists or updates a bill line item in the database.
	 *
	 * @param billLineItem the bill line item to save or update
	 * @return the managed bill line item instance
	 */
	@Transactional(readOnly = true)
	BillLineItem saveBillLineItem(@Nonnull BillLineItem billLineItem);
	
	/**
	 * Permanently removes a bill line item from the database.
	 *
	 * @param billLineItem the bill line item to permanently delete
	 */
	@Transactional
	void purgeBillLineItem(@Nonnull BillLineItem billLineItem);
	
}
