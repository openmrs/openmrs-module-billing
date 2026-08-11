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
import org.openmrs.module.billing.api.model.BillRefund;
import org.openmrs.module.billing.querystore.serialization.BillRefundRecordSerializer;
import org.openmrs.module.querystore.bootstrap.HibernateTypeBootstrapper;
import org.openmrs.module.querystore.serialization.ClinicalRecordSerializer;

/**
 * Backfills existing {@link BillRefund}s. Patient-scoped through the parent bill
 * ({@code e.bill.patient.uuid}); like {@link BillDiscountBootstrapper} it is a JPA {@code @Entity}
 * that does not map {@code dateChanged}, so the cursor uses {@code e.dateCreated}.
 */
public class BillRefundBootstrapper extends HibernateTypeBootstrapper<BillRefund> {
	
	private final BillRefundRecordSerializer serializer;
	
	public BillRefundBootstrapper(BillRefundRecordSerializer serializer, DbSessionFactory sessionFactory) {
		super(sessionFactory);
		this.serializer = serializer;
	}
	
	@Override
	protected ClinicalRecordSerializer<BillRefund> getSerializer() {
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
