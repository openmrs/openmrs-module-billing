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
package org.openmrs.module.billing.base.entity;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.OpenmrsData;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.base.entity.IEntityDataService;
import org.openmrs.module.billing.api.base.entity.IMetadataDataService;

/**
 * Class to hold common entity data service test functionality.
 */
public abstract class IEntityDataServiceTest<S extends IEntityDataService<E>, E extends OpenmrsData> extends IObjectDataServiceTest<S, E> {
	
	public static void assertOpenmrsData(OpenmrsData expected, OpenmrsData actual) {
		assertOpenmrsObject(expected, actual);
		
		Assert.assertEquals(expected.getChangedBy(), actual.getChangedBy());
		Assert.assertEquals(expected.getCreator(), actual.getCreator());
		Assert.assertEquals(expected.getDateChanged(), actual.getDateChanged());
		Assert.assertEquals(expected.getDateCreated(), actual.getDateCreated());
		Assert.assertEquals(expected.isVoided(), actual.isVoided());
		Assert.assertEquals(expected.getVoidedBy(), actual.getVoidedBy());
		Assert.assertEquals(expected.getVoidReason(), actual.getVoidReason());
		Assert.assertEquals(expected.getDateVoided(), actual.getDateVoided());
	}
	
	@Override
	protected void assertEntity(E expected, E actual) {
		assertOpenmrsData(expected, actual);
	}
	
	/**
	 * @verifies void the entity
	 * @see org.openmrs.module.openhmis.commons.api.entity.IEntityDataService#voidEntity(OpenmrsData,
	 *      String)
	 */
	@Test
	public void voidEntity_shouldVoidTheEntity() {
		String reason = "test void";
		E entity = service.getById(0);
		service.voidEntity(entity, reason);
		
		Context.flushSession();
		
		entity = service.getById(0);
		Assert.assertTrue(entity.isVoided());
		Assert.assertEquals(Context.getAuthenticatedUser(), entity.getVoidedBy());
		Assert.assertEquals(reason, entity.getVoidReason());
		Date now = new Date();
		Assert.assertTrue(entity.getDateVoided().before(now) || entity.getDateVoided().equals(now));
	}
	
	/**
	 * @verifies throw IllegalArgumentException with null reason parameter
	 * @see org.openmrs.module.openhmis.commons.api.entity.IEntityDataService#voidEntity(OpenmrsData,
	 *      String)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void voidEntity_shouldThrowIllegalArgumentExceptionWithNullReasonParameter() {
		E entity = service.getById(0);
		
		service.voidEntity(entity, null);
	}
	
	/**
	 * @verifies throw NullPointerException with null entity
	 * @see org.openmrs.module.openhmis.commons.api.entity.IEntityDataService#voidEntity(OpenmrsData,
	 *      String)
	 */
	@Test(expected = NullPointerException.class)
	public void voidEntity_shouldThrowNullPointerExceptionWithNullEntity() {
		service.voidEntity(null, "something");
	}
	
	/**
	 * @verifies unvoid the entity
	 * @see org.openmrs.module.openhmis.commons.api.entity.IEntityDataService#unvoidEntity(OpenmrsData)
	 */
	@Test
	public void unvoidEntity_shouldUnvoidTheEntity() {
		String reason = "test void";
		E entity = service.getById(0);
		service.voidEntity(entity, reason);
		
		Context.flushSession();
		
		entity = service.getById(0);
		Assert.assertTrue(entity.isVoided());
		
		service.unvoidEntity(entity);
		
		Context.flushSession();
		
		entity = service.getById(0);
		
		Assert.assertFalse(entity.isVoided());
		Assert.assertNull(entity.getVoidedBy());
		Assert.assertNull(entity.getVoidReason());
		Assert.assertNotNull(entity.getDateVoided());
	}
	
	/**
	 * @verifies throw NullPointerException with null entity
	 * @see org.openmrs.module.openhmis.commons.api.entity.IEntityDataService#unvoidEntity(OpenmrsData)
	 */
	@Test(expected = NullPointerException.class)
	public void unvoidEntity_shouldThrowNullPointerExceptionWithNullEntity() {
		service.unvoidEntity(null);
	}
	
	/**
	 * @verifies return all entities when include voided is set to true
	 * @see org.openmrs.module.openhmis.commons.api.entity.IMetadataDataService#getAll(boolean)
	 */
	@Test
	public void getAll_shouldReturnAllEntitiesWhenIncludeVoidedIsSetToTrue() {
		String reason = "test void";
		E entity = service.getById(0);
		service.voidEntity(entity, reason);
		
		Context.flushSession();
		
		List<E> entities = service.getAll(true);
		Assert.assertNotNull(entities);
		Assert.assertEquals(getTestEntityCount(), entities.size());
	}
	
	/**
	 * @verifies return all unvoided entities when include voided is set to false
	 * @see org.openmrs.module.openhmis.commons.api.entity.IMetadataDataService#getAll(boolean)
	 */
	@Test
	public void getAll_shouldReturnAllUnvoidedEntitiesWhenIncludeVoidedIsSetToFalse() {
		String reason = "test void";
		E entity = service.getById(0);
		service.voidEntity(entity, reason);
		
		Context.flushSession();
		
		List<E> entities = service.getAll(false);
		Assert.assertNotNull(entities);
		Assert.assertEquals(getTestEntityCount() - 1, entities.size());
	}
	
	/**
	 * @verifies return all unvoided entities when voided is not specified
	 * @see IMetadataDataService#getAll(boolean)
	 */
	@Test
	public void getAll_shouldReturnAllUnvoidedEntitiesWhenVoidedIsNotSpecified() {
		String reason = "test void";
		E entity = service.getById(0);
		service.voidEntity(entity, reason);
		
		Context.flushSession();
		
		List<E> entities = service.getAll();
		Assert.assertNotNull(entities);
		Assert.assertEquals(getTestEntityCount() - 1, entities.size());
	}
	
	/**
	 * @verifies return an empty list if no entities are found
	 * @see org.openmrs.module.openhmis.commons.api.entity.IEntityDataService#getAll(boolean,
	 *      PagingInfo)
	 */
	@Test
	public void getAll_shouldReturnAnEmptyListIfNoEntitiesAreFound() {
		// Delete all defined entities
		List<E> entities = service.getAll(true);
		for (E entity : entities) {
			service.purge(entity);
		}
		
		// Test that empty result is as expected
		entities = service.getAll();
		Assert.assertNotNull(entities);
		Assert.assertEquals(0, entities.size());
		
		entities = service.getAll(true);
		Assert.assertNotNull(entities);
		Assert.assertEquals(0, entities.size());
		
		entities = service.getAll(false);
		Assert.assertNotNull(entities);
		Assert.assertEquals(0, entities.size());
		
		entities = service.getAll(true, new PagingInfo(1, 1));
		Assert.assertNotNull(entities);
		Assert.assertEquals(0, entities.size());
	}
	
	/**
	 * @verifies not return voided entities unless specified
	 * @see org.openmrs.module.openhmis.commons.api.entity.IEntityDataService#getAll(boolean,
	 *      PagingInfo)
	 */
	@Test
	public void getAll_shouldNotReturnVoidedEntitiesUnlessSpecified() {
		E entity = service.getById(0);
		service.voidEntity(entity, "something");
		Context.flushSession();
		
		List<E> entities = service.getAll(false);
		Assert.assertNotNull(entities);
		Assert.assertEquals(getTestEntityCount() - 1, entities.size());
		
		entities = service.getAll(true);
		Assert.assertNotNull(entities);
		Assert.assertEquals(getTestEntityCount(), entities.size());
	}
	
	/**
	 * @verifies return all specified metadata records if paging is null
	 * @see org.openmrs.module.openhmis.commons.api.entity.IEntityDataService#getAll(boolean,
	 *      PagingInfo)
	 */
	@Test
	public void getAll_shouldReturnAllSpecifiedMetadataRecordsIfPagingIsNull() {
		List<E> entities = service.getAll(true, null);
		Assert.assertNotNull(entities);
		Assert.assertEquals(getTestEntityCount(), entities.size());
	}
	
	/**
	 * @verifies return all specified entity records if paging page or size is less than one
	 * @see org.openmrs.module.openhmis.commons.api.entity.IEntityDataService#getAll(boolean,
	 *      PagingInfo)
	 */
	@Test
	public void getAll_shouldReturnAllSpecifiedEntityRecordsIfPagingPageOrSizeIsLessThanOne() {
		List<E> entities = service.getAll(true, new PagingInfo(0, 1));
		Assert.assertNotNull(entities);
		Assert.assertEquals(getTestEntityCount(), entities.size());
		
		entities = service.getAll(true, new PagingInfo(1, 0));
		Assert.assertNotNull(entities);
		Assert.assertEquals(getTestEntityCount(), entities.size());
		
		entities = service.getAll(true, new PagingInfo(0, 0));
		Assert.assertNotNull(entities);
		Assert.assertEquals(getTestEntityCount(), entities.size());
	}
	
	/**
	 * @verifies set the paging total records to the total number of entity records
	 * @see org.openmrs.module.openhmis.commons.api.entity.IEntityDataService#getAll(boolean,
	 *      PagingInfo)
	 */
	@Test
	public void getAll_shouldSetThePagingTotalRecordsToTheTotalNumberOfEntityRecords() {
		PagingInfo paging = new PagingInfo(1, 1);
		List<E> entities = service.getAll(false, paging);
		
		Assert.assertNotNull(entities);
		Assert.assertEquals(1, entities.size());
		Assert.assertEquals(Long.valueOf(getTestEntityCount()), paging.getTotalRecordCount());
	}
	
	/**
	 * @verifies not get the total paging record count if it is more than zero
	 * @see org.openmrs.module.openhmis.commons.api.entity.IEntityDataService#getAll(boolean,
	 *      PagingInfo)
	 */
	@Test
	public void getAll_shouldNotGetTheTotalPagingRecordCountIfItIsMoreThanZero() {
		PagingInfo paging = new PagingInfo(1, 1);
		
		// First check that the full total is set
		List<E> entities = service.getAll(false, paging);
		Assert.assertNotNull(entities);
		Assert.assertEquals(1, entities.size());
		Assert.assertEquals(Long.valueOf(getTestEntityCount()), paging.getTotalRecordCount());
		
		// Now manually set the total and check that it is not reset
		paging = new PagingInfo(1, 1);
		paging.setTotalRecordCount(10L);
		
		entities = service.getAll(false, paging);
		Assert.assertNotNull(entities);
		Assert.assertEquals(1, entities.size());
		Assert.assertEquals((Long) 10L, paging.getTotalRecordCount());
		
		// Finally, explicitly set the paging to not load the total and make sure it is not counted
		paging = new PagingInfo(1, 1);
		paging.setLoadRecordCount(false);
		
		entities = service.getAll(false, paging);
		Assert.assertNotNull(entities);
		Assert.assertEquals(1, entities.size());
		Assert.assertNull(paging.getTotalRecordCount());
	}
	
	/**
	 * @verifies return paged entity records if paging is specified
	 * @see IEntityDataService#getAll(boolean, PagingInfo)
	 */
	@Test
	public void getAll_shouldReturnPagedEntityRecordsIfPagingIsSpecified() {
		PagingInfo paging = new PagingInfo(1, 1);
		List<E> entities;
		
		for (int i = 0; i < getTestEntityCount(); i++) {
			paging.setPage(i + 1);
			entities = service.getAll(paging);
			
			Assert.assertNotNull(entities);
			Assert.assertEquals(1, entities.size());
			assertEntity(service.getById(i), entities.get(0));
		}
	}
}
