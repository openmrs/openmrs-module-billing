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

import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.openmrs.module.billing.api.db.PaymentModeDAO;
import org.openmrs.module.billing.api.model.PaymentMode;

import javax.annotation.Nonnull;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Hibernate implementation of {@link PaymentModeDAO}.
 */
@RequiredArgsConstructor
public class HibernatePaymentModeDAOImpl implements PaymentModeDAO {
	
	private final SessionFactory sessionFactory;
	
	/**
	 * @inheritDoc
	 */
	@Override
	public PaymentMode getPaymentMode(@Nonnull Integer id) {
		return sessionFactory.getCurrentSession().get(PaymentMode.class, id);
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	public PaymentMode getPaymentModeByUuid(@Nonnull String uuid) {
		TypedQuery<PaymentMode> query = sessionFactory.getCurrentSession()
		        .createQuery("select b from PaymentMode b where b.uuid = :uuid", PaymentMode.class);
		query.setParameter("uuid", uuid);
		return query.getResultStream().findFirst().orElse(null);
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	public List<PaymentMode> getPaymentModes(boolean includeRetired) {
		String hql = "from PaymentMode" + (includeRetired ? "" : " where retired = false");
		return sessionFactory.getCurrentSession().createQuery(hql, PaymentMode.class).getResultList();
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	public PaymentMode savePaymentMode(@Nonnull PaymentMode paymentMode) {
		sessionFactory.getCurrentSession().saveOrUpdate(paymentMode);
		return paymentMode;
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	public void purgePaymentMode(@Nonnull PaymentMode paymentMode) {
		sessionFactory.getCurrentSession().delete(paymentMode);
	}
}
