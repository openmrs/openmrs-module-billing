/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.web.rest.controller;

import lombok.RequiredArgsConstructor;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.PatientPaymentStatusResolverFactory;
import org.openmrs.module.billing.api.model.PatientPaymentStatusResult;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/rest/" + RestConstants.VERSION_1 + "/billing/patientPaymentStatus")
@RequiredArgsConstructor
public class PatientPaymentStatusController {
	
	private final PatientPaymentStatusResolverFactory patientPaymentStatusResolverFactory;
	
	@GetMapping("{patientUuid}")
	public ResponseEntity<PatientPaymentStatusResult> getPatientPaymentStatus(@PathVariable String patientUuid) {
		Patient patient = Context.getPatientService().getPatientByUuid(patientUuid);
		
		if (patient == null) {
			return ResponseEntity.notFound().build();
		}
		
		PatientPaymentStatusResult result = patientPaymentStatusResolverFactory.getResolver().resolve(patient);
		return ResponseEntity.ok(result);
	}
}
