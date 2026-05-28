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
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.PatientPaymentStatusResolver;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.PatientPaymentStatus;
import org.openmrs.module.billing.api.model.PatientPaymentStatusResult;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DefaultPatientPaymentStatusResolver implements PatientPaymentStatusResolver {
	
	static final String MSG_OUTSTANDING = "billing.patientPaymentStatus.outstanding";
	
	static final String MSG_NO_OUTSTANDING = "billing.patientPaymentStatus.noOutstanding";
	
	static final String MSG_NO_BILLS = "billing.patientPaymentStatus.noBills";
	
	private final BillService billService;
	
	@Override
	public PatientPaymentStatusResult resolve(Patient patient) {
		List<Bill> bills = billService.getBillsByPatientUuid(patient.getUuid(), null);
		List<Bill> activeBills = bills.stream().filter(b -> !Boolean.TRUE.equals(b.getVoided()))
		        .collect(Collectors.toList());
		
		if (activeBills.isEmpty()) {
			String reason = Context.getMessageSourceService().getMessage(MSG_NO_BILLS);
			return PatientPaymentStatusResult.builder().status(PatientPaymentStatus.UNKNOWN).reason(reason).build();
		}
		
		boolean hasOutstanding = activeBills.stream()
		        .anyMatch(b -> b.getStatus() == BillStatus.PENDING || b.getStatus() == BillStatus.POSTED);
		
		String reason = Context.getMessageSourceService().getMessage(hasOutstanding ? MSG_OUTSTANDING : MSG_NO_OUTSTANDING);
		return PatientPaymentStatusResult.builder()
		        .status(hasOutstanding ? PatientPaymentStatus.UNPAID : PatientPaymentStatus.PAID).reason(reason).build();
	}
}
