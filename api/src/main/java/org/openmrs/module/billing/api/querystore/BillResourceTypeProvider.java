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

import org.openmrs.module.querystore.bootstrap.TypeBootstrapper;
import org.openmrs.module.querystore.serialization.ClinicalRecordSerializer;
import org.openmrs.module.querystore.spi.ResourceTypeProvider;

public class BillResourceTypeProvider implements ResourceTypeProvider {
	
	private final BillRecordSerializer serializer;
	
	public BillResourceTypeProvider(BillRecordSerializer serializer) {
		this.serializer = serializer;
	}
	
	@Override
	public String getResourceType() {
		return BillingQueryStoreConstants.RESOURCE_TYPE_BILL;
	}
	
	@Override
	public ClinicalRecordSerializer<?> getSerializer() {
		return serializer;
	}
	
	@Override
	public TypeBootstrapper<?> getBootstrapper() {
		// No historical-record bootstrap for this v1; the AOP advice on BillService projects
		// ongoing mutations, and any pre-existing bills will not appear in the index until a
		// bootstrap path lands (separate slice).
		return null;
	}
}
