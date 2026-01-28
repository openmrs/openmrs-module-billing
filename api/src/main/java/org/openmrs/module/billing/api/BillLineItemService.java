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
import org.openmrs.module.billing.api.model.BillLineItem;

import javax.annotation.Nullable;
import java.util.List;

public interface BillLineItemService extends OpenmrsService {
	
	/**
	 * Retrieves the IDs of line items currently persisted in the database for a bill.
	 *
	 * @param billId the ID of the bill
	 * @return a list of line item IDs, or an empty list if the bill has no line items
	 */
	List<Integer> getPersistedLineItemIds(Integer billId);
	
	/**
	 * Retrieves a bill line item by its UUID.
	 *
	 * @param uuid the UUID of the bill line item
	 * @return the bill line item with the specified UUID, or null if not found
	 */
	@Nullable
	BillLineItem getBillLineItemByUuid(String uuid);
	
}
