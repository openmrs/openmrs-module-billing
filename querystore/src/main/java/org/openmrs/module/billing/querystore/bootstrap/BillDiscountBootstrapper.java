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
import org.openmrs.module.billing.api.model.BillDiscount;
import org.openmrs.module.billing.querystore.serialization.BillDiscountRecordSerializer;
import org.openmrs.module.querystore.bootstrap.HibernateTypeBootstrapper;
import org.openmrs.module.querystore.serialization.ClinicalRecordSerializer;

/**
 * Backfills existing {@link BillDiscount}s. A discount is patient-scoped through its parent bill,
 * so the patient association is {@code e.bill.patient.uuid} (which also forces the inner joins that
 * drop any dump-orphaned bill/patient FK). It is a JPA {@code @Entity} extending
 * {@code BaseOpenmrsData} and does not map {@code dateChanged}, so the cursor uses
 * {@code e.dateCreated} - live sync handles later status changes, so the backfill cursor only needs
 * to be monotonic for a single pass.
 */
public class BillDiscountBootstrapper extends HibernateTypeBootstrapper<BillDiscount> {
	
	private final BillDiscountRecordSerializer serializer;
	
	public BillDiscountBootstrapper(BillDiscountRecordSerializer serializer, DbSessionFactory sessionFactory) {
		super(sessionFactory);
		this.serializer = serializer;
	}
	
	@Override
	protected ClinicalRecordSerializer<BillDiscount> getSerializer() {
		return serializer;
	}
	
	@Override
	protected String patientAssociationExpr() {
		return "e.bill.patient.uuid";
	}
	
	@Override
	protected String cursorDateExpr() {
		return "e.dateCreated";
	}
}
