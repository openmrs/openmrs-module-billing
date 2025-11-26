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
package org.openmrs.module.billing.api.util;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.module.billing.api.model.BillLineItem;

/**
 * Utility class for Bill-related operations.
 */
public class BillUtil {
	
	private BillUtil() {
		// Utility class - prevent instantiation
	}
	
	/**
	 * Compares two line item collections to determine if they contain different items. Uses UUID-based
	 * comparison similar to how BaseOpenmrsObject compares entities. Follows the pattern used in
	 * BaseRestDataResource.syncCollection(). This method is used to distinguish between actual line
	 * item modifications and Hibernate/REST framework synchronization calls where the same items are
	 * being re-set.
	 * 
	 * @param existingItems The current line items collection
	 * @param newItems The new line items collection to compare
	 * @return true if collections contain different items, false if they're the same
	 */
	public static boolean areLineItemsDifferent(List<BillLineItem> existingItems, List<BillLineItem> newItems) {
		// If either collection is null, they're considered the same (Hibernate initialization)
		if (existingItems == null || newItems == null) {
			return false;
		}
		
		// If same reference, they're the same
		if (existingItems == newItems) {
			return false;
		}
		
		// Extract UUID sets for comparison (similar to syncCollection pattern)
		Set<String> existingUuids = existingItems.stream().filter(item -> item != null && item.getUuid() != null)
		        .map(BaseOpenmrsObject::getUuid).collect(Collectors.toSet());
		
		Set<String> newUuids = newItems.stream().filter(item -> item != null && item.getUuid() != null)
		        .map(BaseOpenmrsObject::getUuid).collect(Collectors.toSet());
		
		// Compare UUID sets - if they're equal, collections contain the same items (by UUID)
		return !existingUuids.equals(newUuids);
	}
}
