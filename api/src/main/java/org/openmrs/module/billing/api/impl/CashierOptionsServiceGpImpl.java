/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.ModuleSettings;
import org.openmrs.module.billing.api.ICashierOptionsService;
import org.openmrs.module.billing.api.model.CashierOptions;
import org.openmrs.module.stockmanagement.api.model.StockItem;

/**
 * Service to load CashierOptions from global options
 *
 * @author daniel
 */
@Slf4j
public class CashierOptionsServiceGpImpl implements ICashierOptionsService {
	
	public CashierOptionsServiceGpImpl() {
		
	}
	
	/**
	 * Loads the cashier options from the database.
	 *
	 * @return The {@link CashierOptions}
	 * @should throw APIException if rounding is set but rounding item is not
	 * @should throw APIException if rounding is set but rounding item cannot be found
	 * @should not throw exception if numeric options are null * @should load cashier options from the
	 *         database
	 */
	public CashierOptions getOptions() {
		CashierOptions options = new CashierOptions();
		
		setDefaultReceiptReportId(options);
		//		setRoundingOptions(options);
		if (StringUtils.isEmpty(options.getRoundingItemUuid())) {
			setRoundingOptionsForEmptyUuid(options);
		}
		
		return options;
	}
	
	private void setRoundingOptions(CashierOptions options) {
		String roundingModeProperty = Context.getAdministrationService()
		        .getGlobalProperty(ModuleSettings.ROUNDING_MODE_PROPERTY);
		if (StringUtils.isNotEmpty(roundingModeProperty)) {
			try {
				options.setRoundingMode(CashierOptions.RoundingMode.valueOf(roundingModeProperty));
				
				String roundToNearestProperty = Context.getAdministrationService()
				        .getGlobalProperty(ModuleSettings.ROUND_TO_NEAREST_PROPERTY);
				if (StringUtils.isNotEmpty(roundToNearestProperty)) {
					options.setRoundToNearest(Integer.valueOf(roundToNearestProperty));
					
					String roundingItemId = Context.getAdministrationService()
					        .getGlobalProperty(ModuleSettings.ROUNDING_ITEM_ID);
					if (StringUtils.isNotEmpty(roundingItemId)) {
						StockItem roundingItem = new StockItem();
						try {
							Integer itemId = Integer.parseInt(roundingItemId);
							// TODO Rounding logic
							//							roundingItem = Context.getService(IItemDataService.class).getById(itemId);
						}
						catch (Exception e) {
							log.error("Did not find rounding item by ID with ID <{}>", roundingItemId, e);
						}
						if (roundingItem != null) {
							options.setRoundingItemUuid(roundingItem.getUuid());
						} else {
							log.error("Rounding item is NULL. Check your ID");
						}
					}
				}
			}
			catch (IllegalArgumentException iae) {
				/* Use default if option is not set */
				log.error("IllegalArgumentException occured", iae);
			}
			catch (NullPointerException e) {
				/* Use default if option is not set */
				log.error("NullPointerException occured", e);
			}
		}
	}
	
	private void setDefaultReceiptReportId(CashierOptions options) {
		String receiptReportIdProperty = Context.getAdministrationService()
		        .getGlobalProperty(ModuleSettings.RECEIPT_REPORT_ID_PROPERTY);
		if (StringUtils.isNotEmpty(receiptReportIdProperty)) {
			try {
				options.setDefaultReceiptReportId(Integer.parseInt(receiptReportIdProperty));
			}
			catch (NumberFormatException e) {
				/* Leave unset; must be handled, e.g. in ReceiptController */
				log.error("Error parsing ReceiptReportId <{}>", receiptReportIdProperty, e);
			}
		}
	}
	
	private void setRoundingOptionsForEmptyUuid(CashierOptions options) {
		options.setRoundingMode(CashierOptions.RoundingMode.MID);
		options.setRoundToNearest(0);
	}
}
