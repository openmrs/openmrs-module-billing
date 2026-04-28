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

import org.openmrs.module.billing.api.model.CashierOptions;

/**
 * Interface that represents classes which perform data operations for {@link CashierOptions}s.
 */
public interface ICashierOptionsService {
	
	/**
	 * Load cashier options from wherever
	 *
	 * @return CashierOptions Loaded options
	 * @should load options
	 * @should throw APIException if a rounding item ID is specified but the item cannot be retrieved
	 * @should revert to defaults if there are problems loading options
	 */
	CashierOptions getOptions();
}
