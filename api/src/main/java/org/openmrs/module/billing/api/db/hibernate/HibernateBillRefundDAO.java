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

import java.util.Arrays;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.module.billing.api.db.BillRefundDAO;
import org.openmrs.module.billing.api.model.BillRefund;
import org.openmrs.module.billing.api.model.RefundStatus;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@RequiredArgsConstructor
public class HibernateBillRefundDAO implements BillRefundDAO {
	
	private static final List<RefundStatus> ACTIVE_STATUSES = Arrays.asList(RefundStatus.REQUESTED, RefundStatus.APPROVED);
	
	private final SessionFactory sessionFactory;
	
	@Override
	public BillRefund getBillRefundById(Integer id) {
		return sessionFactory.getCurrentSession().get(BillRefund.class, id);
	}
	
	@Override
	public BillRefund getBillRefundByUuid(String uuid) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<BillRefund> query = cb.createQuery(BillRefund.class);
		Root<BillRefund> root = query.from(BillRefund.class);
		
		query.select(root).where(cb.equal(root.get("uuid"), uuid));
		return session.createQuery(query).uniqueResult();
	}
	
	@Override
	public BillRefund getActiveBillRefund(Integer billId) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<BillRefund> query = cb.createQuery(BillRefund.class);
		Root<BillRefund> root = query.from(BillRefund.class);
		
		query.select(root).where(cb.equal(root.get("bill").get("id"), billId), cb.isNull(root.get("lineItem")),
		    cb.isFalse(root.get("voided")), root.get("status").in(ACTIVE_STATUSES));
		return session.createQuery(query).uniqueResult();
	}
	
	@Override
	public BillRefund getActiveLineItemRefund(Integer lineItemId) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<BillRefund> query = cb.createQuery(BillRefund.class);
		Root<BillRefund> root = query.from(BillRefund.class);
		
		query.select(root).where(cb.equal(root.get("lineItem").get("id"), lineItemId), cb.isFalse(root.get("voided")),
		    root.get("status").in(ACTIVE_STATUSES));
		return session.createQuery(query).uniqueResult();
	}
	
	@Override
	public List<BillRefund> getActiveLineScopedRefunds(Integer billId) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<BillRefund> query = cb.createQuery(BillRefund.class);
		Root<BillRefund> root = query.from(BillRefund.class);
		
		query.select(root).where(cb.equal(root.get("bill").get("id"), billId), cb.isNotNull(root.get("lineItem")),
		    cb.isFalse(root.get("voided")), root.get("status").in(ACTIVE_STATUSES));
		return session.createQuery(query).getResultList();
	}
	
	@Override
	public List<BillRefund> getRefundsByBillId(Integer billId) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<BillRefund> query = cb.createQuery(BillRefund.class);
		Root<BillRefund> root = query.from(BillRefund.class);
		
		query.select(root).where(cb.equal(root.get("bill").get("id"), billId)).orderBy(cb.desc(root.get("dateCreated")));
		return session.createQuery(query).getResultList();
	}
	
	@Override
	public RefundStatus getStatusById(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<RefundStatus> query = cb.createQuery(RefundStatus.class);
		Root<BillRefund> root = query.from(BillRefund.class);
		
		query.select(root.<RefundStatus> get("status")).where(cb.equal(root.get("billRefundId"), id));
		return session.createQuery(query).uniqueResult();
	}
	
	@Override
	public BillRefund saveBillRefund(BillRefund billRefund) {
		sessionFactory.getCurrentSession().saveOrUpdate(billRefund);
		return billRefund;
	}
}
