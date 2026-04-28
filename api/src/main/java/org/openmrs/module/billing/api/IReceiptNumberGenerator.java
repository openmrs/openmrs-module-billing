/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api;

import org.openmrs.module.billing.api.model.Bill;

/**
 * Represents classes that can generate receipt numbers.
 */
public interface IReceiptNumberGenerator {
	
	/**
	 * The name of the receipt number generator.
	 *
	 * @return The generator name.
	 */
	String getName();
	
	/**
	 * A description of the receipt number generator.
	 *
	 * @return The generator description.
	 */
	String getDescription();
	
	/**
	 * Gets the optional configuration page URL to configure this generator.
	 *
	 * @return The configuration page or a {@code null} or empty string if there is no configuration
	 *         page.
	 */
	String getConfigurationPage();
	
	/**
	 * Gets whether this generator has been loaded.
	 *
	 * @return {@code true} if this generator has been loaded; otherwise, {@code false}.
	 */
	boolean isLoaded();
	
	/**
	 * Performs any loading needed by the generator.
	 */
	void load();
	
	/**
	 * Generates a new receipt number for the specified {@link Bill}. Note that the receipt number field
	 * for the specified bill is NOT set.
	 *
	 * @param bill The bill to generate a new receipt number for.
	 * @return The generated receipt number.
	 */
	String generateNumber(Bill bill);
}
