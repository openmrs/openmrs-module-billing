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
 * Billing-specific formatting helpers shared by the bill / discount / refund serializers so money
 * and enum-status text render the same way in every document. (String trimming is provided by the
 * SPI base {@code AbstractRecordSerializer.trimToNull}.)
 */
final class BillDocFormat {
	
	private BillDocFormat() {
	}
	
	/**
	 * Plain (non-scientific, no grouping) decimal string; treats null as zero. Use for a bare numeric
	 * value such as a percentage, where {@link #money} would misread at the call site.
	 */
	static String plain(BigDecimal value) {
		return (value != null ? value : BigDecimal.ZERO).toPlainString();
	}
	
	/** A currency amount rendered as a plain decimal string; treats null as zero. */
	static String money(BigDecimal amount) {
		return plain(amount);
	}
	
	/** Turns an enum constant such as {@code PARTIALLY_REFUNDED} into {@code partially refunded}. */
	static String readable(String enumName) {
		return enumName == null ? "" : enumName.toLowerCase().replace('_', ' ');
	}
}
