/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.db.hibernate;

import lombok.AllArgsConstructor;
import org.hibernate.SessionFactory;
import org.openmrs.Order;
import org.openmrs.module.billing.api.db.BillLineItemDAO;
import org.openmrs.module.billing.api.model.BillLineItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Hibernate implementation of {@link BillLineItemDAO}.
 */
@AllArgsConstructor
public class HibernateBillLineItemDAO implements BillLineItemDAO {
	
	private final SessionFactory sessionFactory;
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Integer> getLineItemIdsByBillId(@Nonnull Integer billId) {
		return sessionFactory.getCurrentSession()
		        .createNativeQuery("SELECT bill_line_item_id FROM cashier_bill_line_item WHERE bill_id = :billId")
		        .setParameter("billId", billId).getResultList();
	}
	
	@Override
	@Nullable
	public BillLineItem getBillLineItemByUuid(@Nonnull String uuid) {
		TypedQuery<BillLineItem> query = sessionFactory.getCurrentSession()
		        .createQuery("select b from BillLineItem b where b.uuid = :uuid", BillLineItem.class);
		query.setParameter("uuid", uuid);
		return query.getResultStream().findFirst().orElse(null);
	}
	
	@Override
	@Nullable
	public BillLineItem getBillLineItemByOrder(@Nonnull Order order) {
		TypedQuery<BillLineItem> query = sessionFactory.getCurrentSession()
		        .createQuery("select b from BillLineItem b where b.order = :order and b.voided = false", BillLineItem.class);
		query.setParameter("order", order);
		return query.getResultStream().findFirst().orElse(null);
	}
	
	@Override
	public BillLineItem saveBillLineItem(@Nonnull BillLineItem lineItem) {
		sessionFactory.getCurrentSession().saveOrUpdate(lineItem);
		return lineItem;
	}
	
}
