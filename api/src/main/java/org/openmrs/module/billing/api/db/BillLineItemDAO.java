package org.openmrs.module.billing.api.db;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Data Access Object (DAO) interface for BillLineItem persistence operations.
 */
public interface BillLineItemDAO {
	
	/**
	 * Retrieves the IDs of line items currently persisted in the database for a bill.
	 * <p>
	 * This method uses a native query to bypass Hibernate's first-level cache, ensuring it returns the
	 * actual persisted state of line items rather than any in-memory modifications.
	 * </p>
	 *
	 * @param billId the ID of the bill (must not be null)
	 * @return a list of line item IDs, or an empty list if the bill has no line items
	 */
	List<Integer> getLineItemIdsByBillId(@Nonnull Integer billId);
	
}
