package org.openmrs.module.billing.api.db;

import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Data access object for {@link CashierItemPrice} entities.
 */
public interface CashierItemPriceDAO {
	
	/**
	 * Gets the cashier item price with the specified id.
	 *
	 * @param id the cashier item price id
	 * @return the cashier item price or {@code null} if not found
	 */
	@Transactional(readOnly = true)
	CashierItemPrice getCashierItemPrice(@Nonnull Integer id);
	
	/**
	 * Gets the cashier item price with the specified uuid.
	 *
	 * @param uuid the cashier item price uuid
	 * @return the cashier item price or {@code null} if not found
	 */
	@Transactional(readOnly = true)
	CashierItemPrice getCashierItemPriceByUuid(@Nonnull String uuid);
	
	/**
	 * Gets all cashier item prices.
	 *
	 * @param includeRetired whether to include retired cashier item prices
	 * @return a list of all cashier item prices, or an empty list if none found
	 */
	@Transactional(readOnly = true)
	List<CashierItemPrice> getCashierItemPrices(boolean includeRetired);
	
	/**
	 * Saves or updates the specified cashier item price.
	 *
	 * @param cashierItemPrice the cashier item price to save
	 * @return the saved cashier item price
	 */
	@Transactional
	CashierItemPrice saveCashierItemPrice(@Nonnull CashierItemPrice cashierItemPrice);
	
	/**
	 * Permanently deletes the specified cashier item price from the database.
	 *
	 * @param cashierItemPrice the cashier item price to purge
	 */
	@Transactional
	void purgeCashierItemPrice(@Nonnull CashierItemPrice cashierItemPrice);
	
}
