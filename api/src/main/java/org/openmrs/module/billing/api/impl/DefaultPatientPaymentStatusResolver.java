/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.impl;

import lombok.RequiredArgsConstructor;
import org.openmrs.Patient;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.PatientPaymentStatusResolver;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.PatientPaymentStatus;
import org.openmrs.module.billing.api.model.PatientPaymentStatusResult;

import java.util.List;

@RequiredArgsConstructor
public class DefaultPatientPaymentStatusResolver implements PatientPaymentStatusResolver {
	
	private final BillService billService;
	
	@Override
	public PatientPaymentStatusResult resolve(Patient patient) {
		List<Bill> bills = billService.getBillsByPatientUuid(patient.getUuid(), null);
		boolean hasOutstanding = bills.stream().filter(b -> !Boolean.TRUE.equals(b.getVoided()))
		        .anyMatch(b -> b.getStatus() == BillStatus.PENDING || b.getStatus() == BillStatus.POSTED);
		
		return PatientPaymentStatusResult.builder()
		        .status(hasOutstanding ? PatientPaymentStatus.UNPAID : PatientPaymentStatus.PAID)
		        .reason(hasOutstanding ? "Outstanding bill(s) present" : "No outstanding bills").build();
	}
}
