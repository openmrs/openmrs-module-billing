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
package org.openmrs.module.billing.api.billing;

import org.openmrs.Order;
import org.openmrs.module.billing.api.model.Bill;

import java.util.Optional;

/**
 * Strategy for generating a bill from an order. Implementations are Spring beans registered by
 * name. To override the default behavior for a given order type, register a bean with the same name
 * (e.g. "drugOrderBillingStrategy") and mark it {@code @Primary}, or register a custom bean and
 * give it a higher {@code @org.springframework.core.annotation.Order} priority.
 */
public interface OrderBillingStrategy {
	
	/**
	 * Whether this strategy can handle the given order.
	 *
	 * @param order the order to evaluate
	 * @return true if this strategy should process the order
	 */
	boolean supports(Order order);
	
	/**
	 * Generate and persist a bill for the given order. Implementations should check for duplicates
	 * (idempotency) before creating a new bill.
	 *
	 * @param order a persisted order (guaranteed to exist in the database)
	 * @return the created bill, or empty if the order should not be billed
	 */
	Optional<Bill> generateBill(Order order);
}
