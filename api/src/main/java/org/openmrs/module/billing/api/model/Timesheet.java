/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.model;

import java.util.Date;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Provider;

/**
 * Model class to represent a cashier timesheet entry.
 */
public class Timesheet extends BaseOpenmrsData {
	
	private static final long serialVersionUID = 0L;
	
	private Integer timesheetId;
	
	private Provider cashier;
	
	private CashPoint cashPoint;
	
	private Date clockIn;
	
	private Date clockOut;
	
	@Override
	public Integer getId() {
		return timesheetId;
	}
	
	@Override
	public void setId(Integer id) {
		this.timesheetId = id;
	}
	
	public Provider getCashier() {
		return cashier;
	}
	
	public void setCashier(Provider cashier) {
		this.cashier = cashier;
	}
	
	public CashPoint getCashPoint() {
		return cashPoint;
	}
	
	public void setCashPoint(CashPoint cashPoint) {
		this.cashPoint = cashPoint;
	}
	
	public Date getClockIn() {
		return clockIn;
	}
	
	public void setClockIn(Date clockIn) {
		this.clockIn = clockIn;
	}
	
	public Date getClockOut() {
		return clockOut;
	}
	
	public void setClockOut(Date clockOut) {
		this.clockOut = clockOut;
	}
}
