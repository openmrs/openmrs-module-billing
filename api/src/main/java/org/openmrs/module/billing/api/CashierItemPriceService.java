/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.billing.api.model.CashierItemPrice;

import java.util.List;

/**
 * Service for managing {@link CashierItemPrice} entities.
 */
public interface CashierItemPriceService extends OpenmrsService {
	
	/**
	 * Gets the cashier item price with the specified id.
	 *
	 * @param id the cashier item price id
	 * @return the cashier item price or {@code null} if not found
	 */
	CashierItemPrice getCashierItemPrice(Integer id);
	
	/**
	 * Gets the cashier item price with the specified uuid.
	 *
	 * @param uuid the cashier item price uuid
	 * @return the cashier item price or {@code null} if not found
	 */
	CashierItemPrice getCashierItemPriceByUuid(String uuid);
	
	/**
	 * Gets all cashier item prices.
	 *
	 * @param includeRetired whether to include retired cashier item prices
	 * @return a list of all cashier item prices, or an empty list if none found
	 */
	List<CashierItemPrice> getCashierItemPrices(boolean includeRetired);
	
	/**
	 * Saves or updates the specified cashier item price.
	 *
	 * @param cashierItemPrice the cashier item price to save
	 * @return the saved cashier item price
	 * @throws IllegalArgumentException if cashierItemPrice is null
	 */
	CashierItemPrice saveCashierItemPrice(CashierItemPrice cashierItemPrice);
	
	/**
	 * Retires the specified cashier item price with the given reason.
	 *
	 * @param cashierItemPrice the cashier item price to retire
	 * @param reason the reason for retiring
	 * @throws IllegalArgumentException if cashierItemPrice is null or reason is empty
	 */
	void retireCashierItemPrice(CashierItemPrice cashierItemPrice, String reason);
	
	/**
	 * Unretires the specified cashier item price.
	 *
	 * @param cashierItemPrice the cashier item price to unretire
	 * @return the unretired cashier item price
	 * @throws IllegalArgumentException if cashierItemPrice is null
	 */
	CashierItemPrice unretireCashierItemPrice(CashierItemPrice cashierItemPrice);
	
	/**
	 * Permanently deletes the specified cashier item price from the database.
	 *
	 * @param cashierItemPrice the cashier item price to purge
	 * @throws IllegalArgumentException if cashierItemPrice is null
	 */
	void purgeCashierItemPrice(CashierItemPrice cashierItemPrice);
}
