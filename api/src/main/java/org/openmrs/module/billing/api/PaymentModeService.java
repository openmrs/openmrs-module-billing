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
import org.openmrs.module.billing.api.model.PaymentMode;

import java.util.List;

/**
 * Service for managing {@link PaymentMode} entities.
 */
public interface PaymentModeService extends OpenmrsService {
	
	/**
	 * Gets the payment mode with the specified id.
	 *
	 * @param id the payment mode id
	 * @return the payment mode or {@code null} if not found
	 */
	PaymentMode getPaymentMode(Integer id);
	
	/**
	 * Gets the payment mode with the specified uuid.
	 *
	 * @param uuid the payment mode uuid
	 * @return the payment mode or {@code null} if not found
	 */
	PaymentMode getPaymentModeByUuid(String uuid);
	
	/**
	 * Gets all payment modes.
	 *
	 * @param includeRetired whether to include retired payment modes
	 * @return a list of payment modes, or an empty list if none found
	 */
	List<PaymentMode> getPaymentModes(boolean includeRetired);
	
	/**
	 * Saves or updates the specified payment mode.
	 *
	 * @param paymentMode the payment mode to save
	 * @return the saved payment mode
	 * @throws NullPointerException if paymentMode is null
	 */
	PaymentMode savePaymentMode(PaymentMode paymentMode);
	
	/**
	 * Retires the specified payment mode with the given reason.
	 *
	 * @param paymentMode the payment mode to retire
	 * @param reason the reason for retiring
	 * @return the retired payment mode
	 * @throws IllegalArgumentException if reason is empty or null
	 */
	PaymentMode retirePaymentMode(PaymentMode paymentMode, String reason);
	
	/**
	 * Unretires the specified payment mode.
	 *
	 * @param paymentMode the payment mode to unretire
	 * @return the unretired payment mode
	 */
	PaymentMode unretirePaymentMode(PaymentMode paymentMode);
	
	/**
	 * Permanently deletes the specified payment mode from the database.
	 *
	 * @param paymentMode the payment mode to purge
	 * @throws NullPointerException if paymentMode is null
	 */
	void purgePaymentMode(PaymentMode paymentMode);
}
