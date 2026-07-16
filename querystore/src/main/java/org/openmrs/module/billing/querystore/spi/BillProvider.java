/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.querystore.spi;

import org.openmrs.module.billing.querystore.bootstrap.BillBootstrapper;
import org.openmrs.module.billing.querystore.serialization.BillRecordSerializer;
import org.openmrs.module.querystore.bootstrap.TypeBootstrapper;
import org.openmrs.module.querystore.serialization.ClinicalRecordSerializer;
import org.openmrs.module.querystore.spi.ResourceTypeProvider;

/**
 * Registers the {@code billing_bill} resource type with the query store. Discovered by querystore
 * via {@code Context.getRegisteredComponents(ResourceTypeProvider.class)}.
 */
public class BillProvider implements ResourceTypeProvider {
	
	private final BillRecordSerializer serializer;
	
	private final BillBootstrapper bootstrapper;
	
	public BillProvider(BillRecordSerializer serializer, BillBootstrapper bootstrapper) {
		this.serializer = serializer;
		this.bootstrapper = bootstrapper;
	}
	
	@Override
	public String getResourceType() {
		return BillRecordSerializer.RESOURCE_TYPE;
	}
	
	@Override
	public ClinicalRecordSerializer<?> getSerializer() {
		return serializer;
	}
	
	@Override
	public TypeBootstrapper<?> getBootstrapper() {
		return bootstrapper;
	}
}
