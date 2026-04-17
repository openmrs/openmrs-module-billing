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

import lombok.Getter;
import org.openmrs.module.billing.api.model.Bill;

/**
 * Represents the outcome of a billing strategy processing an order.
 */
@Getter
public class BillingResult {
	
	public enum Action {
		CREATED,
		UPDATED,
		SKIPPED,
		DISCONTINUED
	}
	
	private final Action action;
	
	private final Bill bill;
	
	private final String reason;
	
	private BillingResult(Action action, Bill bill, String reason) {
		this.action = action;
		this.bill = bill;
		this.reason = reason;
	}
	
	public static BillingResult created(Bill bill) {
		return new BillingResult(Action.CREATED, bill, null);
	}
	
	public static BillingResult updated(Bill bill) {
		return new BillingResult(Action.UPDATED, bill, null);
	}
	
	public static BillingResult discontinued() {
		return new BillingResult(Action.DISCONTINUED, null, null);
	}
	
	public static BillingResult skipped(String reason) {
		return new BillingResult(Action.SKIPPED, null, reason);
	}
}
