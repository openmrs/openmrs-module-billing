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

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing {@link CashierItemPrice} entities.
 */
@Transactional
public interface CashierItemPriceService extends OpenmrsService {
	
	/**
	 * Gets the cashier item price with the specified id.
	 *
	 * @param id the cashier item price id
	 * @return the cashier item price or {@code null} if not found
	 */
	@Transactional(readOnly = true)
	CashierItemPrice getCashierItemPrice(Integer id);
	
	/**
	 * Gets the cashier item price with the specified uuid.
	 *
	 * @param uuid the cashier item price uuid
	 * @return the cashier item price or {@code null} if not found
	 */
	@Transactional(readOnly = true)
	CashierItemPrice getCashierItemPriceByUuid(String uuid);
	
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
	 * @throws IllegalArgumentException if cashierItemPrice is null
	 */
	@Transactional
	CashierItemPrice saveCashierItemPrice(CashierItemPrice cashierItemPrice);
	
	/**
	 * Retires the specified cashier item price with the given reason.
	 *
	 * @param cashierItemPrice the cashier item price to retire
	 * @param reason the reason for retiring
	 * @throws IllegalArgumentException if cashierItemPrice is null or reason is empty
	 */
	@Transactional
	void retireCashierItemPrice(CashierItemPrice cashierItemPrice, String reason);
	
	/**
	 * Unretires the specified cashier item price.
	 *
	 * @param cashierItemPrice the cashier item price to unretire
	 * @return the unretired cashier item price
	 * @throws IllegalArgumentException if cashierItemPrice is null
	 */
	@Transactional
	CashierItemPrice unretireCashierItemPrice(CashierItemPrice cashierItemPrice);
	
	/**
	 * Permanently deletes the specified cashier item price from the database.
	 *
	 * @param cashierItemPrice the cashier item price to purge
	 * @throws IllegalArgumentException if cashierItemPrice is null
	 */
	@Transactional
	void purgeCashierItemPrice(CashierItemPrice cashierItemPrice);
}
