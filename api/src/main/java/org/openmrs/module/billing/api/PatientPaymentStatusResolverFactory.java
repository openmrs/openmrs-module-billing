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

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.ModuleSettings;
import org.openmrs.util.OpenmrsClassLoader;

import java.lang.reflect.InvocationTargetException;

@RequiredArgsConstructor
public class PatientPaymentStatusResolverFactory {
	
	private final PatientPaymentStatusResolver defaultPatientPaymentStatusResolver;
	
	private volatile String cachedResolverClassName;
	
	private volatile PatientPaymentStatusResolver cachedResolver;
	
	public PatientPaymentStatusResolver getResolver() {
		String resolverClassName = Context.getAdministrationService()
		        .getGlobalProperty(ModuleSettings.PATIENT_PAYMENT_STATUS_RESOLVER);
		if (StringUtils.isBlank(resolverClassName)) {
			return defaultPatientPaymentStatusResolver;
		}
		synchronized (this) {
			if (!StringUtils.equals(resolverClassName, cachedResolverClassName)) {
				cachedResolver = instantiate(resolverClassName);
				cachedResolverClassName = resolverClassName;
			}
			return cachedResolver;
		}
	}
	
	private PatientPaymentStatusResolver instantiate(String resolverClassName) {
		try {
			Class<?> cls = OpenmrsClassLoader.getInstance().loadClass(resolverClassName);
			if (!PatientPaymentStatusResolver.class.isAssignableFrom(cls)) {
				throw new APIException("Class '" + resolverClassName + "' does not implement PatientPaymentStatusResolver");
			}
			return (PatientPaymentStatusResolver) cls.getDeclaredConstructor().newInstance();
		}
		catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | InstantiationException
		        | NoSuchMethodException e) {
			throw new APIException("Failed to load patient payment status resolver '" + resolverClassName + "'", e);
		}
	}
}
