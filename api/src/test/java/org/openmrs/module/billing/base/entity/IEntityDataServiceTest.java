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

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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
		
		Assertions.assertEquals(expected.getChangedBy(), actual.getChangedBy());
		Assertions.assertEquals(expected.getCreator(), actual.getCreator());
		Assertions.assertEquals(expected.getDateChanged(), actual.getDateChanged());
		Assertions.assertEquals(expected.getDateCreated(), actual.getDateCreated());
		Assertions.assertEquals(expected.getVoided(), actual.getVoided());
		Assertions.assertEquals(expected.getVoidedBy(), actual.getVoidedBy());
		Assertions.assertEquals(expected.getVoidReason(), actual.getVoidReason());
		Assertions.assertEquals(expected.getDateVoided(), actual.getDateVoided());
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
		Assertions.assertTrue(entity.getVoided());
		Assertions.assertEquals(Context.getAuthenticatedUser(), entity.getVoidedBy());
		Assertions.assertEquals(reason, entity.getVoidReason());
		Date now = new Date();
		Assertions.assertTrue(entity.getDateVoided().before(now) || entity.getDateVoided().equals(now));
	}
	
	/**
	 * @verifies throw IllegalArgumentException with null reason parameter
	 * @see org.openmrs.module.openhmis.commons.api.entity.IEntityDataService#voidEntity(OpenmrsData,
	 *      String)
	 */
	@Test
	public void voidEntity_shouldThrowIllegalArgumentExceptionWithNullReasonParameter() {
		assertThrows(IllegalArgumentException.class, () -> {
			E entity = service.getById(0);
			
			service.voidEntity(entity, null);
		});
	}
	
	/**
	 * @verifies throw NullPointerException with null entity
	 * @see org.openmrs.module.openhmis.commons.api.entity.IEntityDataService#voidEntity(OpenmrsData,
	 *      String)
	 */
	@Test
	public void voidEntity_shouldThrowNullPointerExceptionWithNullEntity() {
		assertThrows(NullPointerException.class, () -> {
			service.voidEntity(null, "something");
		});
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
		Assertions.assertTrue(entity.getVoided());
		
		service.unvoidEntity(entity);
		
		Context.flushSession();
		
		entity = service.getById(0);
		
		Assertions.assertFalse(entity.getVoided());
		Assertions.assertNull(entity.getVoidedBy());
		Assertions.assertNull(entity.getVoidReason());
		Assertions.assertNotNull(entity.getDateVoided());
	}
	
	/**
	 * @verifies throw NullPointerException with null entity
	 * @see org.openmrs.module.openhmis.commons.api.entity.IEntityDataService#unvoidEntity(OpenmrsData)
	 */
	@Test
	public void unvoidEntity_shouldThrowNullPointerExceptionWithNullEntity() {
		assertThrows(NullPointerException.class, () -> {
			service.unvoidEntity(null);
		});
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
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(getTestEntityCount(), entities.size());
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
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(getTestEntityCount() - 1, entities.size());
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
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(getTestEntityCount() - 1, entities.size());
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
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(0, entities.size());
		
		entities = service.getAll(true);
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(0, entities.size());
		
		entities = service.getAll(false);
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(0, entities.size());
		
		entities = service.getAll(true, new PagingInfo(1, 1));
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(0, entities.size());
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
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(getTestEntityCount() - 1, entities.size());
		
		entities = service.getAll(true);
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(getTestEntityCount(), entities.size());
	}
	
	/**
	 * @verifies return all specified metadata records if paging is null
	 * @see org.openmrs.module.openhmis.commons.api.entity.IEntityDataService#getAll(boolean,
	 *      PagingInfo)
	 */
	@Test
	public void getAll_shouldReturnAllSpecifiedMetadataRecordsIfPagingIsNull() {
		List<E> entities = service.getAll(true, null);
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(getTestEntityCount(), entities.size());
	}
	
	/**
	 * @verifies return all specified entity records if paging page or size is less than one
	 * @see org.openmrs.module.openhmis.commons.api.entity.IEntityDataService#getAll(boolean,
	 *      PagingInfo)
	 */
	@Test
	public void getAll_shouldReturnAllSpecifiedEntityRecordsIfPagingPageOrSizeIsLessThanOne() {
		List<E> entities = service.getAll(true, new PagingInfo(0, 1));
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(getTestEntityCount(), entities.size());
		
		entities = service.getAll(true, new PagingInfo(1, 0));
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(getTestEntityCount(), entities.size());
		
		entities = service.getAll(true, new PagingInfo(0, 0));
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(getTestEntityCount(), entities.size());
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
		
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(1, entities.size());
		Assertions.assertEquals(Long.valueOf(getTestEntityCount()), paging.getTotalRecordCount());
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
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(1, entities.size());
		Assertions.assertEquals(Long.valueOf(getTestEntityCount()), paging.getTotalRecordCount());
		
		// Now manually set the total and check that it is not reset
		paging = new PagingInfo(1, 1);
		paging.setTotalRecordCount(10L);
		
		entities = service.getAll(false, paging);
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(1, entities.size());
		Assertions.assertEquals((Long) 10L, paging.getTotalRecordCount());
		
		// Finally, explicitly set the paging to not load the total and make sure it is not counted
		paging = new PagingInfo(1, 1);
		paging.setLoadRecordCount(false);
		
		entities = service.getAll(false, paging);
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(1, entities.size());
		Assertions.assertNull(paging.getTotalRecordCount());
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
			
			Assertions.assertNotNull(entities);
			Assertions.assertEquals(1, entities.size());
			assertEntity(service.getById(i), entities.get(0));
		}
	}
}
