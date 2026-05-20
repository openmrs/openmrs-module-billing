/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.querystore;

import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.stockmanagement.api.model.StockItem;

// Display-name lookup scoped to the querystore SPI — shared only by BillRecordSerializer and
// BillRefundRecordSerializer so their indexed item names stay consistent. Intentionally NOT the
// source for ReceiptGenerator (which prefers Drug.name on printed receipts) or the FHIR
// translator (which keys off Concept presence). Pulling this helper out of the querystore
// package and "consolidating" the three consumers would silently change user-visible receipts.
final class BillingDisplayNames {
	
	private BillingDisplayNames() {
	}
	
	static String lineItemDisplayName(BillLineItem lineItem) {
		if (lineItem == null) {
			return null;
		}
		BillableService service = lineItem.getBillableService();
		if (service != null && service.getName() != null && !service.getName().isEmpty()) {
			return service.getName();
		}
		StockItem item = lineItem.getItem();
		if (item != null && item.getCommonName() != null && !item.getCommonName().isEmpty()) {
			return item.getCommonName();
		}
		return null;
	}
}
