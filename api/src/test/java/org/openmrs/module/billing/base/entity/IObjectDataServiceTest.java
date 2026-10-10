/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.base.entity;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.base.entity.IObjectDataService;
import org.openmrs.module.billing.api.base.f.Action2;
import org.openmrs.module.billing.base.BaseModuleContextTest;

public abstract class IObjectDataServiceTest<S extends IObjectDataService<E>, E extends OpenmrsObject> extends BaseModuleContextTest {
	
	protected S service;
	
	/**
	 * Tests that the specified object are not null and that the {@link OpenmrsObject} properties are
	 * equal.
	 *
	 * @param expected The expected object properties
	 * @param actual The actual object properties
	 */
	public static void assertOpenmrsObject(OpenmrsObject expected, OpenmrsObject actual) {
		Assertions.assertNotNull(expected);
		Assertions.assertNotNull(actual);
		
		Assertions.assertEquals(expected.getId(), actual.getId());
		Assertions.assertEquals(expected.getUuid(), actual.getUuid());
	}
	
	public static <T> void assertCollection(Collection<T> expected, Collection<T> actual, Action2<T, T> test) {
		if (expected == null) {
			Assertions.assertNull(actual);
		} else {
			Assertions.assertEquals(expected.size(), actual.size());
			
			T[] expectedArray = (T[]) new Object[expected.size()];
			expected.toArray(expectedArray);
			T[] actualArray = (T[]) new Object[actual.size()];
			actual.toArray(actualArray);
			
			for (int i = 0; i < expected.size(); i++) {
				test.apply(expectedArray[i], actualArray[i]);
			}
		}
	}
	
	public abstract E createEntity(boolean valid);
	
	protected abstract int getTestEntityCount();
	
	protected abstract void updateEntityFields(E entity);
	
	protected void assertEntity(E expected, E actual) {
		assertOpenmrsObject(expected, actual);
	}
	
	protected S createService() {
		return Context.getService(getServiceClass());
	}
	
	@BeforeEach
	public void before() throws Exception {
		service = createService();
	}
	
	/**
	 * @verifies throw NullPointerException if the object is null
	 * @see org.openmrs.module.openhmis.commons.api.entity.IObjectDataService#save(OpenmrsObject)
	 */
	@Test
	public void save_shouldThrowNullPointerExceptionIfTheObjectIsNull() {
		assertThrows(NullPointerException.class, () -> {
			service.save(null);
		});
	}
	
	/**
	 * @verifies validate the object before saving
	 * @see org.openmrs.module.openhmis.commons.api.entity.IObjectDataService#save(OpenmrsObject)
	 */
	@Test
	public void save_shouldValidateTheObjectBeforeSaving() {
		assertThrows(APIException.class, () -> {
			E entity = createEntity(false);
			
			service.save(entity);
		});
	}
	
	/**
	 * @verifies return saved object
	 * @see org.openmrs.module.openhmis.commons.api.entity.IObjectDataService#save(OpenmrsObject)
	 */
	@Test
	public void save_shouldReturnSavedObject() {
		E entity = createEntity(true);
		
		E result = service.save(entity);
		Context.flushSession();
		
		Assertions.assertNotNull(result);
		Assertions.assertNotNull(result.getId());
	}
	
	/**
	 * @verifies update the object successfully
	 * @see org.openmrs.module.openhmis.commons.api.entity.IObjectDataService#save(OpenmrsObject)
	 */
	@Test
	public void save_shouldUpdateTheObjectSuccessfully() {
		E entity = service.getById(0);
		Assertions.assertNotNull(entity);
		
		updateEntityFields(entity);
		
		service.save(entity);
		Context.flushSession();
		
		E updatedEntity = service.getById(entity.getId());
		assertEntity(entity, updatedEntity);
	}
	
	/**
	 * @verifies create the object successfully
	 * @see org.openmrs.module.openhmis.commons.api.entity.IObjectDataService#save(OpenmrsObject)
	 */
	@Test
	public void save_shouldCreateTheObjectSuccessfully() {
		E entity = createEntity(true);
		
		entity = service.save(entity);
		Context.flushSession();
		
		E result = service.getById(entity.getId());
		assertEntity(entity, result);
	}
	
	/**
	 * @verifies throw NullPointerException if the object is null
	 * @see org.openmrs.module.openhmis.commons.api.entity.IObjectDataService#purge(OpenmrsObject)
	 */
	@Test
	public void purge_shouldThrowNullPointerExceptionIfTheObjectIsNull() {
		assertThrows(NullPointerException.class, () -> {
			service.purge(null);
		});
	}
	
	/**
	 * @verifies delete the specified object
	 * @see org.openmrs.module.openhmis.commons.api.entity.IObjectDataService#purge(OpenmrsObject)
	 */
	@Test
	public void purge_shouldDeleteTheSpecifiedObject() {
		E entity = createEntity(true);
		
		service.save(entity);
		Context.flushSession();
		
		E result = service.getById(entity.getId());
		Assertions.assertNotNull(result);
		
		service.purge(entity);
		Context.flushSession();
		
		result = service.getById(entity.getId());
		Assertions.assertNull(result);
	}
	
	/**
	 * @verifies return all object records
	 * @see org.openmrs.module.openhmis.commons.api.entity.IObjectDataService#getAll()
	 */
	@Test
	public void getAll_shouldReturnAllObjectRecords() {
		List<E> entities = service.getAll();
		Assertions.assertNotNull(entities);
		
		Assertions.assertEquals(getTestEntityCount(), entities.size());
	}
	
	/**
	 * @verifies return an empty list if there are no objects
	 * @see org.openmrs.module.openhmis.commons.api.entity.IObjectDataService#getAll()
	 */
	@Test
	public void getAll_shouldReturnAnEmptyListIfThereAreNoObjects() {
		List<E> entities = service.getAll();
		for (E entity : entities) {
			service.purge(entity);
		}
		
		Context.flushSession();
		
		entities = service.getAll();
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(0, entities.size());
	}
	
	/**
	 * @verifies return the object with the specified id
	 * @see org.openmrs.module.openhmis.commons.api.entity.IObjectDataService#getById(int)
	 */
	@Test
	public void getById_shouldReturnTheObjectWithTheSpecifiedId() {
		E entity = service.getById(0);
		
		Assertions.assertEquals((Integer) 0, entity.getId());
	}
	
	/**
	 * @verifies return null if no object can be found.
	 * @see org.openmrs.module.openhmis.commons.api.entity.IObjectDataService#getById(int)
	 */
	@Test
	public void getById_shouldReturnNullIfNoObjectCanBeFound() {
		E entity = service.getById(-100);
		
		Assertions.assertNull(entity);
	}
	
	/**
	 * @verifies find the object with the specified uuid
	 * @see org.openmrs.module.openhmis.commons.api.entity.IObjectDataService#getByUuid(String)
	 */
	@Test
	public void getByUuid_shouldFindTheObjectWithTheSpecifiedUuid() {
		E entity = service.getById(0);
		E uuidEntity = service.getByUuid(entity.getUuid());
		
		assertEntity(entity, uuidEntity);
	}
	
	/**
	 * @verifies return null if no object is found
	 * @see org.openmrs.module.openhmis.commons.api.entity.IObjectDataService#getByUuid(String)
	 */
	@Test
	public void getByUuid_shouldReturnNullIfNoObjectIsFound() {
		E entity = service.getByUuid("Invalid");
		
		Assertions.assertNull(entity);
	}
	
	/**
	 * @verifies throw IllegalArgumentException if uuid is null
	 * @see org.openmrs.module.openhmis.commons.api.entity.IObjectDataService#getByUuid(String)
	 */
	@Test
	public void getByUuid_shouldThrowIllegalArgumentExceptionIfUuidIsNull() {
		assertThrows(IllegalArgumentException.class, () -> {
			service.getByUuid(null);
		});
	}
	
	/**
	 * @verifies throw IllegalArgumentException if uuid is empty
	 * @see org.openmrs.module.openhmis.commons.api.entity.IObjectDataService#getByUuid(String)
	 */
	@Test
	public void getByUuid_shouldThrowIllegalArgumentExceptionIfUuidIsEmpty() {
		assertThrows(IllegalArgumentException.class, () -> {
			service.getByUuid("");
		});
	}
	
	/**
	 * @verifies return all object records if paging is null
	 * @see org.openmrs.module.openhmis.commons.api.entity.IObjectDataService#getAll(PagingInfo)
	 */
	@Test
	public void getAll_shouldReturnAllObjectRecordsIfPagingIsNull() {
		List<E> entities = service.getAll(null);
		
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(getTestEntityCount(), entities.size());
	}
	
	/**
	 * @verifies return all object records if paging page or size is less than one
	 * @see org.openmrs.module.openhmis.commons.api.entity.IObjectDataService#getAll(PagingInfo)
	 */
	@Test
	public void getAll_shouldReturnAllObjectRecordsIfPagingPageOrSizeIsLessThanOne() {
		PagingInfo paging = new PagingInfo(0, 1);
		List<E> entities = service.getAll(paging);
		
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(getTestEntityCount(), entities.size());
		
		paging = new PagingInfo(1, 0);
		entities = service.getAll(paging);
		
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(getTestEntityCount(), entities.size());
	}
	
	/**
	 * @verifies set the paging total records to the total number of object records
	 * @see org.openmrs.module.openhmis.commons.api.entity.IObjectDataService#getAll(PagingInfo)
	 */
	@Test
	public void getAll_shouldSetThePagingTotalRecordsToTheTotalNumberOfObjectRecords() {
		PagingInfo paging = new PagingInfo(1, 1);
		List<E> entities = service.getAll(paging);
		
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(1, entities.size());
		Assertions.assertEquals(Long.valueOf(getTestEntityCount()), paging.getTotalRecordCount());
	}
	
	/**
	 * @verifies not get the total paging record count if it is more than zero
	 * @see org.openmrs.module.openhmis.commons.api.entity.IObjectDataService#getAll(PagingInfo)
	 */
	@Test
	public void getAll_shouldNotGetTheTotalPagingRecordCountIfItIsMoreThanZero() {
		PagingInfo paging = new PagingInfo(1, 1);
		paging.setLoadRecordCount(false);
		List<E> entities = service.getAll(paging);
		
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(1, entities.size());
		Assertions.assertNull(paging.getTotalRecordCount());
	}
	
	/**
	 * @verifies return paged object records if paging is specified
	 * @see IObjectDataService#getAll(PagingInfo)
	 */
	@Test
	public void getAll_shouldReturnPagedObjectRecordsIfPagingIsSpecified() {
		List<E> allEntities = service.getAll();
		
		PagingInfo paging = new PagingInfo(1, 1);
		List<E> entities;
		for (int i = 0; i < getTestEntityCount(); i++) {
			paging.setPage(i + 1);
			entities = service.getAll(paging);
			
			Assertions.assertNotNull(entities);
			Assertions.assertEquals(1, entities.size());
			Assertions.assertEquals(allEntities.get(i), entities.get(0));
		}
	}
	
	@SuppressWarnings("unchecked")
	protected Class<S> getServiceClass() {
		ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
		
		return (Class<S>) parameterizedType.getActualTypeArguments()[0];
	}
}
