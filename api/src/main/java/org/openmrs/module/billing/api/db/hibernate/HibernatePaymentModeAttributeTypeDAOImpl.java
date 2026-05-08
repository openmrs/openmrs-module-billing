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
import org.openmrs.module.billing.api.db.PaymentModeAttributeTypeDAO;
import org.openmrs.module.billing.api.model.PaymentModeAttributeType;

import javax.annotation.Nonnull;
import javax.persistence.TypedQuery;
import java.util.List;

@RequiredArgsConstructor
public class HibernatePaymentModeAttributeTypeDAOImpl implements PaymentModeAttributeTypeDAO {

    private final SessionFactory sessionFactory;

    @Override
    public PaymentModeAttributeType getPaymentModeAttributeType(@Nonnull Integer id) {
        return sessionFactory.getCurrentSession().get(PaymentModeAttributeType.class, id);
    }

    @Override
    public PaymentModeAttributeType getPaymentModeAttributeTypeByUuid(@Nonnull String uuid) {
        TypedQuery<PaymentModeAttributeType> query = sessionFactory.getCurrentSession()
                .createQuery("select p from PaymentModeAttributeType p where p.uuid = :uuid",
                        PaymentModeAttributeType.class);
        query.setParameter("uuid", uuid);
        return query.getResultStream().findFirst().orElse(null);
    }

    @Override
    public List<PaymentModeAttributeType> getAllPaymentModeAttributeTypes(boolean includeRetired) {
        String hql = "from PaymentModeAttributeType" + (includeRetired ? "" : " where retired = false");
        return sessionFactory.getCurrentSession()
                .createQuery(hql, PaymentModeAttributeType.class).getResultList();
    }

    @Override
    public PaymentModeAttributeType savePaymentModeAttributeType(@Nonnull PaymentModeAttributeType attributeType) {
        sessionFactory.getCurrentSession().saveOrUpdate(attributeType);
        return attributeType;
    }

    @Override
    public void purgePaymentModeAttributeType(@Nonnull PaymentModeAttributeType attributeType) {
        sessionFactory.getCurrentSession().delete(attributeType);
    }
}
