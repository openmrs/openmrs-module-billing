/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.querystore.bootstrap;

import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.querystore.serialization.BillRecordSerializer;
import org.openmrs.module.querystore.bootstrap.HibernateTypeBootstrapper;
import org.openmrs.module.querystore.serialization.ClinicalRecordSerializer;

/**
 * Backfills existing {@link Bill}s into the query store. Bill maps both {@code patient} (not-null)
 * and {@code dateChanged}, so the base HibernateTypeBootstrapper defaults ({@code e.patient.uuid}
 * scope, {@code COALESCE(e.dateChanged, e.dateCreated)} cursor) apply unchanged.
 */
public class BillBootstrapper extends HibernateTypeBootstrapper<Bill> {
	
	private final BillRecordSerializer serializer;
	
	public BillBootstrapper(BillRecordSerializer serializer, DbSessionFactory sessionFactory) {
		super(sessionFactory);
		this.serializer = serializer;
	}
	
	@Override
	protected ClinicalRecordSerializer<Bill> getSerializer() {
		return serializer;
	}
}
