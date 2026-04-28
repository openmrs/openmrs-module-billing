/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.db;

import org.openmrs.module.billing.api.model.PaymentMode;

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
