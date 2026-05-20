/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.querystore;

import java.time.LocalDate;
import java.util.Date;

import org.openmrs.Provider;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.api.model.Timesheet;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.serialization.AbstractRecordSerializer;
import org.openmrs.module.querystore.util.DateFormatUtil;

public class TimesheetRecordSerializer extends AbstractRecordSerializer<Timesheet> {
	
	@Override
	public String getResourceType() {
		return BillingQueryStoreConstants.RESOURCE_TYPE_TIMESHEET;
	}
	
	@Override
	public Class<Timesheet> getSupportedType() {
		return Timesheet.class;
	}
	
	@Override
	protected String getPatientUuid(Timesheet timesheet) {
		// Timesheets are provider-scoped, not patient-scoped — return null. AbstractRecordSerializer
		// allows null patientUuid for administrative documents; the document is still indexed under
		// the resource type and queryable via provider_uuid.
		return null;
	}
	
	@Override
	protected String getResourceUuid(Timesheet timesheet) {
		return timesheet.getUuid();
	}
	
	@Override
	protected LocalDate getDate(Timesheet timesheet) {
		// Use clockIn when available — that's the natural calendar key for "who was on duty on
		// 2026-05-20". Fall back to dateCreated for in-progress rows that haven't clocked in yet
		// (rare but possible for half-constructed records).
		Date anchor = timesheet.getClockIn() != null ? timesheet.getClockIn() : timesheet.getDateCreated();
		return DateFormatUtil.toLocalDate(anchor);
	}
	
	@Override
	protected void populate(Timesheet timesheet, QueryDocument doc) {
		Provider cashier = timesheet.getCashier();
		CashPoint cashPoint = timesheet.getCashPoint();
		String cashierName = cashier != null && cashier.getName() != null ? cashier.getName() : "";
		String cashPointName = cashPoint != null && cashPoint.getName() != null ? cashPoint.getName() : "";
		
		doc.setText(String.format("Timesheet for %s at %s. Clock in: %s. Clock out: %s.",
		    cashierName.isEmpty() ? timesheet.getUuid() : cashierName,
		    cashPointName.isEmpty() ? "unspecified" : cashPointName,
		    timesheet.getClockIn() != null ? timesheet.getClockIn().toString() : "—",
		    timesheet.getClockOut() != null ? timesheet.getClockOut().toString() : "open"));
		
		if (cashier != null) {
			doc.putMetadata(BillingQueryStoreConstants.FIELD_PROVIDER_UUID, cashier.getUuid());
			if (cashier.getName() != null && !cashier.getName().trim().isEmpty()) {
				doc.putMetadata(BillingQueryStoreConstants.FIELD_CASHIER_NAME, cashier.getName());
			}
		}
		if (cashPoint != null) {
			doc.putMetadata(BillingQueryStoreConstants.FIELD_CASH_POINT_UUID, cashPoint.getUuid());
			if (cashPoint.getName() != null && !cashPoint.getName().trim().isEmpty()) {
				doc.putMetadata(BillingQueryStoreConstants.FIELD_CASH_POINT_NAME, cashPoint.getName());
			}
		}
		if (timesheet.getClockIn() != null) {
			doc.putMetadata(BillingQueryStoreConstants.FIELD_CLOCK_IN, timesheet.getClockIn());
		}
		if (timesheet.getClockOut() != null) {
			doc.putMetadata(BillingQueryStoreConstants.FIELD_CLOCK_OUT, timesheet.getClockOut());
		}
		doc.putMetadata(BillingQueryStoreConstants.FIELD_VOIDED, timesheet.getVoided());
		
		BillingAuditFields.populate(doc, timesheet);
	}
}
