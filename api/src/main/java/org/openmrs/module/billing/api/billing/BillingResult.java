/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
