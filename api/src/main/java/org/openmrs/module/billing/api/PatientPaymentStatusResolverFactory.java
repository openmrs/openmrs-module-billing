/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.ModuleSettings;
import org.openmrs.module.billing.api.impl.DefaultPatientPaymentStatusResolver;

public class PatientPaymentStatusResolverFactory {
	
	public PatientPaymentStatusResolver getResolver() {
		String configured = Context.getAdministrationService()
		        .getGlobalProperty(ModuleSettings.PATIENT_PAYMENT_STATUS_RESOLVER);
		String target = StringUtils.isBlank(configured) ? DefaultPatientPaymentStatusResolver.class.getName() : configured;
		
		return Context.getRegisteredComponents(PatientPaymentStatusResolver.class).stream()
		        .filter(resolver -> target.equals(resolver.getClass().getName())).findFirst()
		        .orElseThrow(() -> new APIException("No registered PatientPaymentStatusResolver bean matches class '"
		                + target + "'. Ensure the resolver is a Spring component scanned by an OpenMRS module."));
	}
}
