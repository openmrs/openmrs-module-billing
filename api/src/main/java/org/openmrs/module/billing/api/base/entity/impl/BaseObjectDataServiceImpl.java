/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.base.entity.impl;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.Query;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.base.Utility;
import org.openmrs.module.billing.api.base.entity.IObjectDataService;
import org.openmrs.module.billing.api.base.entity.db.hibernate.BaseHibernateRepository;
import org.openmrs.module.billing.api.base.entity.db.hibernate.EntityCriteria;
import org.openmrs.module.billing.api.base.entity.db.hibernate.EntityCriteria.SortOrder;
import org.openmrs.module.billing.api.base.entity.security.IObjectAuthorizationPrivileges;
import org.openmrs.module.billing.api.base.f.Action1;
import org.openmrs.module.billing.api.base.util.PrivilegeUtil;
import org.springframework.transaction.annotation.Transactional;

/**
 * The base type for object services. Provides the core implementation for the common
 * {@link org.openmrs.OpenmrsObject} operations.
 *
 * @param <E> The {@link org.openmrs.OpenmrsObject} model type.
 */
@Transactional
public abstract class BaseObjectDataServiceImpl<E extends OpenmrsObject, P extends IObjectAuthorizationPrivileges> extends BaseOpenmrsService implements IObjectDataService<E> {
	
	private BaseHibernateRepository repository;
	
	private Class entityClass = null;
	
	/**
	 * Gets the privileges to use for this service or {@code null} if none are needed.
	 *
	 * @return The service privileges.
	 */
	protected abstract P getPrivileges();
	
	/**
	 * Validates the specified object, throwing an exception in the validation fails.
	 *
	 * @param object The object to validate.
	 * @should not throw an exception for valid objects
	 * @should throw IllegalArgumentException with a null object
	 * @should throw an exception for invalid objects
	 */
	protected abstract void validate(E object);
	
	/**
	 * Gets a list of all related objects for the specified entity.
	 *
	 * @param entity The parent entity.
	 * @return The list of the related objects or {@code null} if none.
	 */
	protected Collection<? extends OpenmrsObject> getRelatedObjects(E entity) {
		return null;
	}
	
	protected SortOrder[] getDefaultSort() {
		return null;
	}
	
	/**
	 * Gets the {@link BaseHibernateRepository} for this data service.
	 *
	 * @return The repository.
	 */
	public BaseHibernateRepository getRepository() {
		return this.repository;
	}
	
	/**
	 * Sets the {@link BaseHibernateRepository} for this data service.
	 *
	 * @param repository The data repository object that the service will use.
	 */
	public void setRepository(BaseHibernateRepository repository) {
		this.repository = repository;
	}
	
	@Override
	@Transactional
	public E save(E object) {
		P privileges = getPrivileges();
		if (privileges != null && !StringUtils.isEmpty(privileges.getSavePrivilege())) {
			PrivilegeUtil.requirePrivileges(Context.getAuthenticatedUser(), privileges.getSavePrivilege());
		}
		
		if (object == null) {
			throw new NullPointerException("The object to save cannot be null.");
		}
		
		validate(object);
		
		return repository.save(object);
	}
	
	@Override
	@Transactional
	public E saveAll(E object, Collection<? extends OpenmrsObject> related) {
		P privileges = getPrivileges();
		if (privileges != null && !StringUtils.isEmpty(privileges.getSavePrivilege())) {
			PrivilegeUtil.requirePrivileges(Context.getAuthenticatedUser(), privileges.getSavePrivilege());
		}
		
		if (object == null) {
			throw new NullPointerException("The object to save cannot be null.");
		}
		
		validate(object);
		
		Collection<OpenmrsObject> saveAll = new ArrayList<OpenmrsObject>();
		saveAll.add(object);
		saveAll(related);
		
		repository.saveAll(saveAll);
		
		return object;
	}
	
	@Override
	@Transactional
	public void saveAll(Collection<? extends OpenmrsObject> collection) {
		P privileges = getPrivileges();
		if (privileges != null && !StringUtils.isEmpty(privileges.getSavePrivilege())) {
			PrivilegeUtil.requirePrivileges(Context.getAuthenticatedUser(), privileges.getSavePrivilege());
		}
		
		repository.saveAll(collection);
		
	}
	
	@Override
	@Transactional
	public void purge(E object) {
		P privileges = getPrivileges();
		if (privileges != null && !StringUtils.isEmpty(privileges.getPurgePrivilege())) {
			PrivilegeUtil.requirePrivileges(Context.getAuthenticatedUser(), privileges.getPurgePrivilege());
		}
		
		if (object == null) {
			throw new NullPointerException("The object to purge cannot be null.");
		}
		
		repository.delete(object);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<E> getAll() {
		return getAll(null);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<E> getAll(PagingInfo pagingInfo) {
		P privileges = getPrivileges();
		if (privileges != null && !StringUtils.isEmpty(privileges.getGetPrivilege())) {
			PrivilegeUtil.requirePrivileges(Context.getAuthenticatedUser(), privileges.getGetPrivilege());
		}
		
		return executeCriteria(getEntityClass(), pagingInfo, null, getDefaultSort());
	}
	
	@Override
	@Transactional(readOnly = true)
	public E getById(int entityId) {
		P privileges = getPrivileges();
		if (privileges != null && !StringUtils.isEmpty(privileges.getGetPrivilege())) {
			PrivilegeUtil.requirePrivileges(Context.getAuthenticatedUser(), privileges.getGetPrivilege());
		}
		
		return repository.selectSingle(getEntityClass(), entityId);
	}
	
	@Override
	@Transactional(readOnly = true)
	public E getByUuid(String uuid) {
		P privileges = getPrivileges();
		if (privileges != null && !StringUtils.isEmpty(privileges.getGetPrivilege())) {
			PrivilegeUtil.requirePrivileges(Context.getAuthenticatedUser(), privileges.getGetPrivilege());
		}
		
		if (StringUtils.isEmpty(uuid)) {
			throw new IllegalArgumentException("The UUID must be defined.");
		}
		
		EntityCriteria<E> criteria = repository.createCriteria(getEntityClass());
		criteria.add(EntityCriteria.eq("uuid", uuid));
		
		return repository.selectSingle(getEntityClass(), criteria);
	}
	
	/**
	 * Gets a usable instance of the actual class of the generic type E defined by the implementing
	 * sub-class.
	 *
	 * @return The class object for the entity.
	 */
	@SuppressWarnings("unchecked")
	protected Class<E> getEntityClass() {
		if (entityClass == null) {
			ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
			
			entityClass = (Class<E>) parameterizedType.getActualTypeArguments()[0];
		}
		
		return entityClass;
	}
	
	/**
	 * Functional method to create, prepare, and execute a query with {@link EntityCriteria}.
	 *
	 * @param action The {@link Action1} to prepare the {@link EntityCriteria} predicates.
	 * @return The result of the query.
	 */
	protected <T extends OpenmrsObject> List<T> executeCriteria(Class<T> clazz, Action1<EntityCriteria<T>> action) {
		return executeCriteria(clazz, null, action, (SortOrder[]) null);
	}
	
	/**
	 * Functional method to create, prepare, and execute a paged query with {@link EntityCriteria}.
	 *
	 * @param pagingInfo The paging information.
	 * @param action The {@link Action1} to prepare the {@link EntityCriteria} predicates.
	 * @return
	 */
	protected <T extends OpenmrsObject> List<T> executeCriteria(Class<T> clazz, PagingInfo pagingInfo,
	        Action1<EntityCriteria<T>> action) {
		return executeCriteria(clazz, pagingInfo, action, (SortOrder[]) null);
	}
	
	protected <T extends OpenmrsObject> List<T> executeCriteria(Class<T> clazz, PagingInfo pagingInfo,
	        Action1<EntityCriteria<T>> action, SortOrder... orderBy) {
		EntityCriteria<T> criteria = repository.createCriteria(clazz);
		
		if (action != null) {
			action.apply(criteria);
		}
		
		loadPagingTotal(pagingInfo, criteria);
		
		if (orderBy != null) {
			for (SortOrder order : orderBy) {
				criteria.addOrder(order);
			}
		}
		
		return repository.select(clazz, createPagingCriteria(pagingInfo, criteria));
	}
	
	/**
	 * Functional method to apply an action to the related objects, returning the updated objects.
	 *
	 * @param clazz The class of related objects to perform the action on.
	 * @param entity The parent entity.
	 * @param action The {@link Action1} to apply.
	 * @param <T> The class of the related objects.
	 * @return A list containing the updated (applied) related objects.
	 */
	protected <T extends OpenmrsObject> List<T> executeOnRelatedObjects(Class<T> clazz, E entity, Action1<T> action) {
		List<T> updatedObjects = new ArrayList<T>();
		
		Collection<? extends OpenmrsObject> relatedObjects = getRelatedObjects(entity);
		if (relatedObjects != null && !relatedObjects.isEmpty()) {
			for (OpenmrsObject object : relatedObjects) {
				T data = Utility.as(clazz, object);
				if (data != null) {
					action.apply(data);
					
					updatedObjects.add(data);
				}
			}
		}
		
		return updatedObjects;
	}
	
	/**
	 * Loads the total number of records for the specified object type into the specified paging object.
	 *
	 * @param pagingInfo The {@link PagingInfo} object to load with the record count.
	 */
	protected void loadPagingTotal(PagingInfo pagingInfo) {
		loadPagingTotal(pagingInfo, null);
	}
	
	/**
	 * Loads the record count for the specified criteria into the specified paging object.
	 *
	 * @param pagingInfo The {@link PagingInfo} object to load with the record count.
	 * @param criteria The {@link EntityCriteria} to execute against the hibernate data source or
	 *            {@code null} to create a new one.
	 */
	protected void loadPagingTotal(PagingInfo pagingInfo, EntityCriteria<?> criteria) {
		if (pagingInfo != null && pagingInfo.getPage() > 0 && pagingInfo.getPageSize() > 0) {
			if (criteria == null) {
				criteria = repository.createCriteria(getEntityClass());
			}
			
			if (pagingInfo.getLoadRecordCount()) {
				// The row count is a separate query built from the same restrictions, so the criteria is left untouched
				pagingInfo.setTotalRecordCount(repository.selectCount(criteria));
				pagingInfo.setLoadRecordCount(false);
			}
		}
	}
	
	/**
	 * Creates a new {@link EntityCriteria} to retrieve the data specified by the {@link PagingInfo}
	 * object.
	 *
	 * @param pagingInfo The {@link PagingInfo} object that specifies which data should be retrieved.
	 * @return A new {@link EntityCriteria} with the paging settings.
	 */
	protected EntityCriteria<E> createPagingCriteria(PagingInfo pagingInfo) {
		return createPagingCriteria(pagingInfo, null);
	}
	
	/**
	 * Updates the specified {@link EntityCriteria} object to retrieve the data specified by the
	 * {@link PagingInfo} object.
	 *
	 * @param pagingInfo The {@link PagingInfo} object that specifies which data should be retrieved.
	 * @param criteria The {@link EntityCriteria} to add the paging settings to, or {@code null} to
	 *            create a new one for the service entity class.
	 * @return The {@link EntityCriteria} object with the paging settings applied.
	 */
	@SuppressWarnings("unchecked")
	protected <T> EntityCriteria<T> createPagingCriteria(PagingInfo pagingInfo, EntityCriteria<T> criteria) {
		if (pagingInfo != null && pagingInfo.getPage() > 0 && pagingInfo.getPageSize() > 0) {
			if (criteria == null) {
				criteria = (EntityCriteria<T>) repository.createCriteria(getEntityClass());
			}
			
			criteria.setFirstResult((pagingInfo.getPage() - 1) * pagingInfo.getPageSize());
			criteria.setMaxResults(pagingInfo.getPageSize());
			criteria.setFetchSize(pagingInfo.getPageSize());
		}
		
		return criteria;
	}
	
	protected Query createPagingQuery(PagingInfo pagingInfo, Query query) {
		if (query == null) {
			throw new IllegalArgumentException("The query must be defined.");
		}
		
		if (pagingInfo != null && pagingInfo.getPage() > 0 && pagingInfo.getPageSize() > 0) {
			query.setFirstResult((pagingInfo.getPage() - 1) * pagingInfo.getPageSize());
			query.setMaxResults(pagingInfo.getPageSize());
			query.setFetchSize(pagingInfo.getPageSize());
		}
		
		return query;
	}
}
