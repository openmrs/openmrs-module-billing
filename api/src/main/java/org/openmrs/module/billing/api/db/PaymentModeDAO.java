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
package org.openmrs.module.billing.api.db;

import org.openmrs.module.billing.api.model.PaymentMode;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Data access object for {@link PaymentMode} entities.
 */
public interface PaymentModeDAO {
	
	/**
	 * Gets the payment mode with the specified id.
	 *
	 * @param id the payment mode id
	 * @return the payment mode or {@code null} if not found
	 */
	PaymentMode getPaymentMode(@Nonnull Integer id);
	
	/**
	 * Gets the payment mode with the specified uuid.
	 *
	 * @param uuid the payment mode uuid
	 * @return the payment mode or {@code null} if not found
	 */
	PaymentMode getPaymentModeByUuid(@Nonnull String uuid);
	
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
	 */
	PaymentMode savePaymentMode(@Nonnull PaymentMode paymentMode);
	
	/**
	 * Permanently deletes the specified payment mode from the database.
	 *
	 * @param paymentMode the payment mode to purge
	 */
	void purgePaymentMode(@Nonnull PaymentMode paymentMode);
}
