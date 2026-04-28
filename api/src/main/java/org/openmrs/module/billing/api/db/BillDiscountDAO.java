package org.openmrs.module.billing.api.db;

import java.util.List;

import org.openmrs.module.billing.api.model.BillDiscount;

/**
 * Data-access contract for {@link BillDiscount}. Read methods filter by scope and voided state
 * where indicated; write goes through {@link #saveBillDiscount(BillDiscount)} for both inserts
 * and updates.
 */
public interface BillDiscountDAO {

	/** @return the discount for the given primary key, or {@code null} if none. */
	BillDiscount getBillDiscountById(Integer id);

	/** @return the discount with the given UUID, or {@code null} if none. */
	BillDiscount getBillDiscountByUuid(String uuid);

	/**
	 * @return the active <em>bill-level</em> discount on the bill (where {@code lineItem IS NULL}
	 *         and {@code voided = false}), or {@code null}
	 */
	BillDiscount getBillDiscountByBillId(Integer billId);

	/** @return the active discount targeting the given line item, or {@code null}. */
	BillDiscount getActiveLineItemDiscount(Integer lineItemId);

	/**
	 * @return every discount on the bill (active and voided), newest first; never {@code null}
	 */
	List<BillDiscount> getDiscountsByBillId(Integer billId);

	/**
	 * Inserts or updates the given discount via {@code saveOrUpdate}.
	 *
	 * @return the persisted instance
	 */
	BillDiscount saveBillDiscount(BillDiscount billDiscount);
}
