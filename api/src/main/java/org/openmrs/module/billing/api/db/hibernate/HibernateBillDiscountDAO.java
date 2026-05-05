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

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.module.billing.api.db.BillDiscountDAO;
import org.openmrs.module.billing.api.model.BillDiscount;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@RequiredArgsConstructor
public class HibernateBillDiscountDAO implements BillDiscountDAO {
	
	private final SessionFactory sessionFactory;
	
	@Override
	public BillDiscount getBillDiscountById(Integer id) {
		return sessionFactory.getCurrentSession().get(BillDiscount.class, id);
	}
	
	@Override
	public BillDiscount getBillDiscountByUuid(String uuid) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<BillDiscount> query = cb.createQuery(BillDiscount.class);
		Root<BillDiscount> root = query.from(BillDiscount.class);
		
		query.select(root).where(cb.equal(root.get("uuid"), uuid));
		return session.createQuery(query).uniqueResult();
	}
	
	@Override
	public BillDiscount getBillDiscountByBillId(Integer billId) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<BillDiscount> query = cb.createQuery(BillDiscount.class);
		Root<BillDiscount> root = query.from(BillDiscount.class);
		
		query.select(root).where(cb.equal(root.get("bill").get("id"), billId), cb.isNull(root.get("lineItem")),
		    cb.isFalse(root.get("voided")));
		return session.createQuery(query).uniqueResult();
	}
	
	@Override
	public BillDiscount getActiveLineItemDiscount(Integer lineItemId) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<BillDiscount> query = cb.createQuery(BillDiscount.class);
		Root<BillDiscount> root = query.from(BillDiscount.class);
		
		query.select(root).where(cb.equal(root.get("lineItem").get("id"), lineItemId), cb.isFalse(root.get("voided")));
		return session.createQuery(query).uniqueResult();
	}
	
	@Override
	public List<BillDiscount> getDiscountsByBillId(Integer billId) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<BillDiscount> query = cb.createQuery(BillDiscount.class);
		Root<BillDiscount> root = query.from(BillDiscount.class);
		
		query.select(root).where(cb.equal(root.get("bill").get("id"), billId)).orderBy(cb.desc(root.get("dateCreated")));
		return session.createQuery(query).getResultList();
	}
	
	@Override
	public BillDiscount saveBillDiscount(BillDiscount billDiscount) {
		sessionFactory.getCurrentSession().saveOrUpdate(billDiscount);
		return billDiscount;
	}
}
