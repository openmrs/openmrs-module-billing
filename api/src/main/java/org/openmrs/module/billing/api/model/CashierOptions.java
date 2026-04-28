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

/**
 * Model class that give the options a {@link org.openmrs.Person} (Cashier) has.
 */
public class CashierOptions {
	
	public static final long serialVersionUID = 0L;
	
	private Integer roundToNearest = 0;
	
	private RoundingMode roundingMode = RoundingMode.MID;
	
	private String roundingItemUuid;
	
	private int defaultReceiptReportId;
	
	private boolean timesheetRequired = false;
	
	public String getRoundingItemUuid() {
		return roundingItemUuid;
	}
	
	public void setRoundingItemUuid(String roundingItemUuid) {
		this.roundingItemUuid = roundingItemUuid;
	}
	
	// Getters & setters
	public Integer getRoundToNearest() {
		return roundToNearest;
	}
	
	public void setRoundToNearest(Integer roundToNearest) {
		this.roundToNearest = roundToNearest;
	}
	
	public RoundingMode getRoundingMode() {
		return roundingMode;
	}
	
	public void setRoundingMode(RoundingMode roundingMode) {
		this.roundingMode = roundingMode;
	}
	
	public int getDefaultReceiptReportId() {
		return defaultReceiptReportId;
	}
	
	public void setDefaultReceiptReportId(int defaultReceiptReportId) {
		this.defaultReceiptReportId = defaultReceiptReportId;
	}
	
	public boolean isTimesheetRequired() {
		return timesheetRequired;
	}
	
	public void setTimesheetRequired(boolean timesheetRequired) {
		this.timesheetRequired = timesheetRequired;
	}
	
	/**
	 * Defines the collection of constants to be used for setting the rounding mode
	 */
	public enum RoundingMode {
		
		FLOOR(),
		MID(),
		CEILING();
		
		RoundingMode() {
		}
	}
}
