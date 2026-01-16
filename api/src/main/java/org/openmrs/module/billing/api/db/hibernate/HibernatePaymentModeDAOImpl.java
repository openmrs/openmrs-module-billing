/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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
	
	@Override
	public List<PaymentMode> getPaymentModes() {
		return sessionFactory.getCurrentSession().createQuery("from PaymentMode", PaymentMode.class).getResultList();
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
