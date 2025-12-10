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

import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.util.PrivilegeConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service interface for managing bill line items in the billing module.
 */
@Transactional
public interface BillLineItemService extends OpenmrsService {
	
	/**
	 * Retrieves a bill line item by its database ID.
	 *
	 * @param id the database ID of the bill line item to retrieve
	 * @return the bill line item with the specified ID, or null if not found
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_BILLS)
	BillLineItem getBillLineItem(Integer id);
	
	/**
	 * Retrieves a bill line item by its UUID.
	 *
	 * @param uuid the UUID of the bill line item to retrieve
	 * @return the bill line item with the specified UUID, or null if not found
	 */
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_BILLS)
	BillLineItem getBillLineItemByUuid(String uuid);
	
	/**
	 * Saves a bill line item to the database.
	 *
	 * @param billLineItem the bill line item to save
	 * @return the saved bill line item
	 * @throws NullPointerException if billLineItem is null
	 */
	@Transactional
	@Authorized(PrivilegeConstants.MANAGE_BILLS)
	BillLineItem saveBillLineItem(BillLineItem billLineItem);
	
	/**
	 * Marks a bill line item as voided (soft delete).
	 *
	 * @param billLineItem the bill line item to void
	 * @param voidReason the reason for voiding the bill line item
	 * @return the voided bill line item
	 * @throws NullPointerException if voidReason is null
	 */
	@Transactional
	@Authorized(PrivilegeConstants.DELETE_BILLS)
	BillLineItem voidBillLineItem(BillLineItem billLineItem, String voidReason);
	
	/**
	 * Restores a previously voided bill line item.
	 *
	 * @param billLineItem the bill line item to unvoid
	 * @return the unvoided bill line item
	 */
	@Transactional
	@Authorized(PrivilegeConstants.DELETE_BILLS)
	BillLineItem unvoidBillLineItem(BillLineItem billLineItem);
	
	/**
	 * Permanently deletes a bill line item from the database.
	 *
	 * @param billLineItem the bill line item to permanently delete
	 */
	@Transactional
	@Authorized(PrivilegeConstants.PURGE_BILLS)
	void purgeBillLineItem(BillLineItem billLineItem);
	
}
