/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.base.entity.db.hibernate;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import jakarta.persistence.TypedQuery;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.APIException;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides access to a data source through hibernate.
 */
public class BaseHibernateRepositoryImpl implements BaseHibernateRepository {
	
	private DbSessionFactory sessionFactory;
	
	public BaseHibernateRepositoryImpl(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public DbSessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public Query createQuery(String query) {
		return getHibernateSession().createQuery(query);
	}
	
	@Override
	public <E extends OpenmrsObject> EntityCriteria<E> createCriteria(Class<E> cls) {
		return new EntityCriteria<E>(cls);
	}
	
	@Override
	public <E extends OpenmrsObject> E save(E entity) {
		Session session = getHibernateSession();
		
		try {
			return HibernateUtil.saveOrUpdate(session, entity);
		}
		catch (Exception ex) {
			throw new APIException(
			        "An exception occurred while attempting to add a " + entity.getClass().getSimpleName() + " entity.", ex);
		}
	}
	
	@Override
	@Transactional
	public void saveAll(Collection<? extends OpenmrsObject> collection) {
		Session session = getHibernateSession();
		try {
			
			if (collection != null && !collection.isEmpty()) {
				for (OpenmrsObject obj : collection) {
					HibernateUtil.saveOrUpdate(session, obj);
				}
			}
		}
		catch (Exception ex) {
			throw new APIException("An exception occurred while attempting to add a entity.", ex);
		}
	}
	
	@Override
	public <E extends OpenmrsObject> void delete(E entity) {
		DbSession session = sessionFactory.getCurrentSession();
		try {
			session.delete(entity);
		}
		catch (Exception ex) {
			throw new APIException(
			        "An exception occurred while attempting to delete a " + entity.getClass().getSimpleName() + " entity.",
			        ex);
		}
	}
	
	@Override
	public long selectCount(EntityCriteria<?> criteria) {
		try {
			Session session = getHibernateSession();
			Long count = session.createQuery(criteria.toCountQuery(session.getCriteriaBuilder())).uniqueResult();
			return count == null ? 0 : count;
		}
		catch (Exception ex) {
			throw new APIException("An exception occurred while attempting to selecting a value.", ex);
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T selectValue(Query query) {
		try {
			return (T) query.uniqueResult();
		}
		catch (Exception ex) {
			throw new APIException("An exception occurred while attempting to selecting a value.", ex);
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <E extends OpenmrsObject> E selectSingle(Class<E> cls, Serializable id) {
		DbSession session = sessionFactory.getCurrentSession();
		
		try {
			return (E) session.get(cls, id);
		}
		catch (Exception ex) {
			throw new APIException("An exception occurred while attempting to select a single " + cls.getSimpleName()
			        + " entity with ID" + " " + id.toString() + ".", ex);
		}
	}
	
	@Override
	public <E extends OpenmrsObject> E selectSingle(Class<E> cls, EntityCriteria<E> criteria) {
		E result = null;
		try {
			List<E> results = list(criteria);
			
			if (!results.isEmpty()) {
				result = results.get(0);
			}
		}
		catch (Exception ex) {
			throw new APIException(
			        "An exception occurred while attempting to select a single " + cls.getSimpleName() + " entity.", ex);
		}
		return result;
	}
	
	@Override
	public <E extends OpenmrsObject> List<E> select(Class<E> cls) {
		try {
			return list(createCriteria(cls));
		}
		catch (Exception ex) {
			throw new APIException("An exception occurred while attempting to get " + cls.getSimpleName() + " entities.", //
			        ex);
		}
	}
	
	@Override
	public <E extends OpenmrsObject> List<E> select(Class<E> cls, EntityCriteria<E> criteria) {
		// If the criteria is not defined just use the default select method
		if (criteria == null) {
			return select(cls);
		}
		
		List<E> results;
		
		try {
			results = list(criteria);
		}
		catch (Exception ex) {
			throw new APIException("An exception occurred while attempting to select " + cls.getSimpleName() + " entities.",
			        ex);
		}
		
		return results;
	}
	
	private Session getHibernateSession() {
		return sessionFactory.getHibernateSessionFactory().getCurrentSession();
	}
	
	private <E> List<E> list(EntityCriteria<E> criteria) {
		return createTypedQuery(criteria).getResultList();
	}
	
	private <E> TypedQuery<E> createTypedQuery(EntityCriteria<E> criteria) {
		Session session = getHibernateSession();
		TypedQuery<E> query = session.createQuery(criteria.toQuery(session.getCriteriaBuilder()));
		if (criteria.getFirstResult() != null) {
			query.setFirstResult(criteria.getFirstResult());
		}
		if (criteria.getMaxResults() != null) {
			query.setMaxResults(criteria.getMaxResults());
		}
		if (criteria.getFetchSize() != null) {
			query.setHint("org.hibernate.fetchSize", criteria.getFetchSize());
		}
		if (criteria.getLockMode() != null) {
			query.setLockMode(criteria.getLockMode());
		}
		return query;
	}
}
