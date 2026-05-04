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
 * The allowable settings for the cashier module.
 */
public class CashierSettings {
	
	public static final long serialVersionUID = 1L;
	
	private Boolean adjustmentReasonField;
	
	private Boolean allowBillAdjustment;
	
	private Boolean autoFillPaymentAmount;
	
	private Integer defaultReceiptReportId;
	
	private Boolean cashierMandatory;
	
	private Integer cashierRoundingToNearest;
	
	private String cashierRoundingMode;
	
	private Integer patientDashboard2BillCount;
	
	private Integer departmentCollectionsReportId;
	
	private Integer departmentRevenueReportId;
	
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
	
	public Integer getPaymentsByPaymentModeReportId() {
		return paymentsByPaymentModeReportId;
	}
	
	public void setPaymentsByPaymentModeReportId(Integer paymentsByPaymentModeReportId) {
		this.paymentsByPaymentModeReportId = paymentsByPaymentModeReportId;
	}
}
