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
package org.openmrs.module.billing.api.model;

/**
 * The allowable settings for the billing module.
 */
public class CashierSettings {
	
	public static final long serialVersionUID = 1L;
	
	private Boolean adjustmentReasonField;
	
	private Boolean allowBillAdjustment;
	
	private Boolean autoFillPaymentAmount;
	
	private Integer defaultReceiptReportId;
	
	private Integer defaultShiftReportId;
	
	private Boolean cashierMandatory;
	
	private Integer cashierRoundingToNearest;
	
	private String cashierRoundingMode;
	
	private Boolean cashierTimesheetRequired;
	
	private Integer patientDashboard2BillCount;
	
	private Integer departmentCollectionsReportId;
	
	private Integer departmentRevenueReportId;
	
	private Integer shiftSummaryReportId;
	
	private Integer dailyShiftSummaryReportId;
	
	private Integer paymentsByPaymentModeReportId;
	
	public Boolean getAdjustmentReasonField() {
		return adjustmentReasonField;
	}
	
	public void setAdjustmentReasonField(Boolean adjustmentReasonField) {
		this.adjustmentReasonField = adjustmentReasonField;
	}
	
	public Boolean getAllowBillAdjustment() {
		return allowBillAdjustment;
	}
	
	public void setAllowBillAdjustment(Boolean allowBillAdjustment) {
		this.allowBillAdjustment = allowBillAdjustment;
	}
	
	public Boolean getAutoFillPaymentAmount() {
		return autoFillPaymentAmount;
	}
	
	public void setAutoFillPaymentAmount(Boolean autoFillPaymentAmount) {
		this.autoFillPaymentAmount = autoFillPaymentAmount;
	}
	
	public Integer getDefaultReceiptReportId() {
		return defaultReceiptReportId;
	}
	
	public void setDefaultReceiptReportId(Integer defaultReceiptReportId) {
		this.defaultReceiptReportId = defaultReceiptReportId;
	}
	
	public Integer getDefaultShiftReportId() {
		return defaultShiftReportId;
	}
	
	public void setDefaultShiftReportId(Integer defaultShiftReportId) {
		this.defaultShiftReportId = defaultShiftReportId;
	}
	
	public Boolean getCashierMandatory() {
		return cashierMandatory;
	}
	
	public void setCashierMandatory(Boolean cashierMandatory) {
		this.cashierMandatory = cashierMandatory;
	}
	
	public Integer getCashierRoundingToNearest() {
		return cashierRoundingToNearest;
	}
	
	public void setCashierRoundingToNearest(Integer cashierRoundingToNearest) {
		this.cashierRoundingToNearest = cashierRoundingToNearest;
	}
	
	public String getCashierRoundingMode() {
		return cashierRoundingMode;
	}
	
	public void setCashierRoundingMode(String cashierRoundingMode) {
		this.cashierRoundingMode = cashierRoundingMode;
	}
	
	public Boolean getCashierTimesheetRequired() {
		return cashierTimesheetRequired;
	}
	
	public void setCashierTimesheetRequired(Boolean cashierTimesheetRequired) {
		this.cashierTimesheetRequired = cashierTimesheetRequired;
	}
	
	public Integer getPatientDashboard2BillCount() {
		return patientDashboard2BillCount;
	}
	
	public void setPatientDashboard2BillCount(Integer numberOfBillsToShowOnEachPage) {
		this.patientDashboard2BillCount = numberOfBillsToShowOnEachPage;
	}
	
	public Integer getDepartmentCollectionsReportId() {
		return departmentCollectionsReportId;
	}
	
	public void setDepartmentCollectionsReportId(Integer departmentCollectionsReportId) {
		this.departmentCollectionsReportId = departmentCollectionsReportId;
	}
	
	public Integer getDepartmentRevenueReportId() {
		return departmentRevenueReportId;
	}
	
	public void setDepartmentRevenueReportId(Integer departmentRevenueReportId) {
		this.departmentRevenueReportId = departmentRevenueReportId;
	}
	
	public Integer getShiftSummaryReportId() {
		return shiftSummaryReportId;
	}
	
	public void setShiftSummaryReportId(Integer shiftSummaryReportId) {
		this.shiftSummaryReportId = shiftSummaryReportId;
	}
	
	public Integer getDailyShiftSummaryReportId() {
		return dailyShiftSummaryReportId;
	}
	
	public void setDailyShiftSummaryReportId(Integer dailyShiftSummaryReportId) {
		this.dailyShiftSummaryReportId = dailyShiftSummaryReportId;
	}
	
	public Integer getPaymentsByPaymentModeReportId() {
		return paymentsByPaymentModeReportId;
	}
	
	public void setPaymentsByPaymentModeReportId(Integer paymentsByPaymentModeReportId) {
		this.paymentsByPaymentModeReportId = paymentsByPaymentModeReportId;
	}
}
