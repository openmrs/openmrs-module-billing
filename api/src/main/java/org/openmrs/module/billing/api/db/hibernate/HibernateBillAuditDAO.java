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

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.db.BillAuditDAO;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillAudit;
import org.openmrs.module.billing.api.model.BillAuditAction;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.List;

/**
 * Hibernate implementation of {@link BillAuditDAO}.
 */
@Transactional
public class HibernateBillAuditDAO implements BillAuditDAO {
	
	private SessionFactory sessionFactory;
	
	public HibernateBillAuditDAO(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	@Transactional
	public BillAudit saveBillAudit(@Nonnull BillAudit audit) {
		if (audit == null) {
			throw new NullPointerException("The audit entry must be defined.");
		}
		sessionFactory.getCurrentSession().saveOrUpdate(audit);
		return audit;
	}
	
	@Override
	@Transactional(readOnly = true)
	public BillAudit getBillAudit(@Nonnull Integer id) {
		if (id == null) {
			throw new NullPointerException("The audit entry ID must be defined.");
		}
		return (BillAudit) sessionFactory.getCurrentSession().get(BillAudit.class, id);
	}
	
	@Override
	@Transactional(readOnly = true)
	public BillAudit getBillAuditByUuid(@Nonnull String uuid) {
		if (uuid == null) {
			throw new NullPointerException("The audit entry UUID must be defined.");
		}
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(BillAudit.class);
		criteria.add(Restrictions.eq("uuid", uuid));
		
		return (BillAudit) criteria.uniqueResult();
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<BillAudit> getBillAuditHistory(@Nonnull Bill bill, PagingInfo pagingInfo) {
		if (bill == null) {
			throw new NullPointerException("The bill must be defined.");
		}
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(BillAudit.class);
		criteria.add(Restrictions.eq("bill", bill));
		criteria.addOrder(Order.desc("auditDate"));
		
		loadPagingTotal(criteria, pagingInfo);
		
		if (pagingInfo != null && pagingInfo.getPageSize() > 0) {
			criteria.setFirstResult(pagingInfo.getPage() * pagingInfo.getPageSize());
			criteria.setMaxResults(pagingInfo.getPageSize());
		}
		
		return criteria.list();
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<BillAudit> getBillAuditsByAction(@Nonnull Bill bill, @Nonnull BillAuditAction action,
	        PagingInfo pagingInfo) {
		if (bill == null) {
			throw new NullPointerException("The bill must be defined.");
		}
		if (action == null) {
			throw new NullPointerException("The action must be defined.");
		}
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(BillAudit.class);
		criteria.add(Restrictions.eq("bill", bill));
		criteria.add(Restrictions.eq("action", action));
		criteria.addOrder(Order.desc("auditDate"));
		
		loadPagingTotal(criteria, pagingInfo);
		
		if (pagingInfo != null && pagingInfo.getPageSize() > 0) {
			criteria.setFirstResult(pagingInfo.getPage() * pagingInfo.getPageSize());
			criteria.setMaxResults(pagingInfo.getPageSize());
		}
		
		return criteria.list();
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<BillAudit> getBillAuditsByDateRange(@Nonnull Bill bill, Date startDate, Date endDate,
	        PagingInfo pagingInfo) {
		if (bill == null) {
			throw new NullPointerException("The bill must be defined.");
		}
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(BillAudit.class);
		criteria.add(Restrictions.eq("bill", bill));
		
		if (startDate != null) {
			criteria.add(Restrictions.ge("auditDate", startDate));
		}
		if (endDate != null) {
			criteria.add(Restrictions.le("auditDate", endDate));
		}
		
		criteria.addOrder(Order.desc("auditDate"));
		
		loadPagingTotal(criteria, pagingInfo);
		
		if (pagingInfo != null && pagingInfo.getPageSize() > 0) {
			criteria.setFirstResult(pagingInfo.getPage() * pagingInfo.getPageSize());
			criteria.setMaxResults(pagingInfo.getPageSize());
		}
		
		return criteria.list();
	}
	
	@Override
	@Transactional
	public void purgeBillAudit(@Nonnull BillAudit audit) {
		if (audit == null) {
			throw new NullPointerException("The audit entry must be defined.");
		}
		sessionFactory.getCurrentSession().delete(audit);
	}
	
	/**
	 * Loads the total record count into the paging info object if paging is enabled and record count is
	 * requested.
	 */
	private void loadPagingTotal(Criteria criteria, PagingInfo pagingInfo) {
		if (pagingInfo != null && pagingInfo.getLoadRecordCount()) {
			criteria.setProjection(Projections.rowCount());
			Long count = (Long) criteria.uniqueResult();
			pagingInfo.setTotalRecordCount(count != null ? count : 0L);
			pagingInfo.setLoadRecordCount(false);
			criteria.setProjection(null);
			criteria.setResultTransformer(Criteria.ROOT_ENTITY);
		}
	}
}
