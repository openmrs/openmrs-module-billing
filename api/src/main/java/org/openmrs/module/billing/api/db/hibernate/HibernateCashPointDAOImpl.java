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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.db.CashPointDAO;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.api.search.CashPointSearch;

/**
 * Hibernate implementation of {@link CashPointDAO}.
 */
@RequiredArgsConstructor
public class HibernateCashPointDAOImpl implements CashPointDAO {
	
	private final SessionFactory sessionFactory;
	
	/**
	 * @inheritDoc
	 */
	@Override
	public CashPoint getCashPoint(@Nonnull Integer id) {
		return sessionFactory.getCurrentSession().get(CashPoint.class, id);
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	public CashPoint getCashPointByUuid(@Nonnull String uuid) {
		TypedQuery<CashPoint> query = sessionFactory.getCurrentSession()
		        .createQuery("select c from CashPoint c where c.uuid = :uuid", CashPoint.class);
		query.setParameter("uuid", uuid);
		return query.getResultStream().findFirst().orElse(null);
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	public List<CashPoint> getCashPoints(@Nonnull CashPointSearch cashPointSearch, PagingInfo pagingInfo) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		CriteriaQuery<CashPoint> criteriaQuery = criteriaBuilder.createQuery(CashPoint.class);
		
		Root<CashPoint> root = criteriaQuery.from(CashPoint.class);
		
		List<Predicate> predicates = new ArrayList<>();
		
		if (StringUtils.isNotEmpty(cashPointSearch.getLocationUuid())) {
			predicates.add(criteriaBuilder.equal(root.get("location").get("uuid"), cashPointSearch.getLocationUuid()));
		}
		
		if (StringUtils.isNotEmpty(cashPointSearch.getName())) {
			predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
			    "%" + cashPointSearch.getName().toLowerCase() + "%"));
		}
		
		if (!cashPointSearch.getIncludeRetired()) {
			predicates.add(criteriaBuilder.equal(root.get("retired"), false));
		}
		
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		
		TypedQuery<CashPoint> typedQuery = session.createQuery(criteriaQuery);
		PagingUtil.applyPaging(typedQuery, pagingInfo, predicates, sessionFactory, CashPoint.class);
		
		return typedQuery.getResultList();
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	public CashPoint saveCashPoint(@Nonnull CashPoint cashPoint) {
		sessionFactory.getCurrentSession().saveOrUpdate(cashPoint);
		return cashPoint;
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	public void purgeCashPoint(@Nonnull CashPoint cashPoint) {
		sessionFactory.getCurrentSession().delete(cashPoint);
	}
}
