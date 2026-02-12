package org.openmrs.module.billing.api.db;

import org.openmrs.module.billing.api.model.BillLineItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
	
	/**
	 * Retrieves a bill line item by its UUID.
	 *
	 * @param uuid the UUID of the bill line item
	 * @return the bill line item with the specified UUID, or null if not found
	 */
	@Nullable
	BillLineItem getBillLineItemByUuid(@Nonnull String uuid);
	
}
