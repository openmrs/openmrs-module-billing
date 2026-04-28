/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.web.legacyweb.controller;

import java.util.Date;

import org.openmrs.module.billing.api.model.Timesheet;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validates the Timesheet entry. Implents {@link Validator}
 */
public class TimesheetEntryValidator implements Validator {
	
	@Override
	public boolean supports(Class<?> clazz) {
		return Timesheet.class.isAssignableFrom(clazz);
	}
	
	@Override
	public void validate(Object target, Errors errors) {
		Timesheet timesheet = (Timesheet) target;
		
		if (timesheet.getClockIn() == null) {
			errors.rejectValue("clockIn", "openhmis.cashier.timesheet.entry.error.clockIn.empty");
		} else if (timesheet.getClockIn().after(new Date())) {
			errors.rejectValue("clockIn", "openhmis.cashier.timesheet.entry.error.clockIn.future");
		}
		
		if (timesheet.getClockOut() != null && timesheet.getClockOut().after(new Date())) {
			errors.rejectValue("clockOut", "openhmis.cashier.timesheet.entry.error.clockOut.future");
		}
		
		if (timesheet.getClockOut() != null && timesheet.getClockOut().before(timesheet.getClockIn())) {
			errors.rejectValue("clockOut", "openhmis.cashier.timesheet.entry.error.clockOut.before.clockIn");
		}
	}
}
