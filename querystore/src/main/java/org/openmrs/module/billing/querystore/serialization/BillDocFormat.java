/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.querystore.serialization;

import java.math.BigDecimal;

/**
 * Small formatting helpers shared by the billing record serializers so bill, discount and refund
 * documents render money and enum/status text the same way.
 */
final class BillDocFormat {
	
	private BillDocFormat() {
	}
	
	/** Plain (non-scientific, no grouping) decimal string; treats null as zero. */
	static String money(BigDecimal amount) {
		return (amount != null ? amount : BigDecimal.ZERO).toPlainString();
	}
	
	/** Turns an enum constant such as {@code PARTIALLY_REFUNDED} into {@code partially refunded}. */
	static String readable(String enumName) {
		return enumName == null ? "" : enumName.toLowerCase().replace('_', ' ');
	}
	
	/** Trims and returns null when null/blank, so callers null-check once. */
	static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
