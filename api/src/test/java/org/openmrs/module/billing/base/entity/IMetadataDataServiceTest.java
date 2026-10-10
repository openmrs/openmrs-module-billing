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

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmrs.OpenmrsMetadata;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.base.entity.IMetadataDataService;

public abstract class IMetadataDataServiceTest<S extends IMetadataDataService<E>, E extends OpenmrsMetadata> extends IObjectDataServiceTest<S, E> {
	
	public static void assertOpenmrsMetadata(OpenmrsMetadata expected, OpenmrsMetadata actual) {
		assertOpenmrsObject(expected, actual);
		
		Assertions.assertEquals(expected.getChangedBy(), actual.getChangedBy());
		Assertions.assertEquals(expected.getCreator(), actual.getCreator());
		Assertions.assertEquals(expected.getDateChanged(), actual.getDateChanged());
		Assertions.assertEquals(expected.getDateCreated(), actual.getDateCreated());
		Assertions.assertEquals(expected.getDateRetired(), actual.getDateRetired());
		Assertions.assertEquals(expected.getDescription(), actual.getDescription());
		Assertions.assertEquals(expected.getName(), actual.getName());
		Assertions.assertEquals(expected.getRetired(), actual.getRetired());
		Assertions.assertEquals(expected.getRetiredBy(), actual.getRetiredBy());
		Assertions.assertEquals(expected.getRetireReason(), actual.getRetireReason());
	}
	
	@Override
	protected void assertEntity(E expected, E actual) {
		assertOpenmrsMetadata(expected, actual);
	}
	
	/**
	 * @verifies retire the metadata successfully
	 * @see org.openmrs.module.openhmis.commons.api.entity.IMetadataDataService#retire(OpenmrsMetadata,
	 *      String)
	 */
	@Test
	public void retire_shouldRetireTheMetadataSuccessfully() {
		String reason = "test retire";
		E entity = service.getById(0);
		
		service.retire(entity, reason);
		
		Context.flushSession();
		
		entity = service.getById(entity.getId());
		
		Assertions.assertTrue(entity.getRetired());
		Assertions.assertEquals(Context.getAuthenticatedUser(), entity.getRetiredBy());
		Assertions.assertEquals(reason, entity.getRetireReason());
		Date now = new Date();
		Assertions.assertTrue(entity.getDateRetired().before(now) || entity.getDateRetired().equals(now));
	}
	
	/**
	 * @verifies throw NullPointerException when the metadata is null
	 * @see org.openmrs.module.openhmis.commons.api.entity.IMetadataDataService#retire(OpenmrsMetadata,
	 *      String)
	 */
	@Test
	public void retire_shouldThrowNullPointerExceptionWhenTheMetadataIsNull() {
		assertThrows(NullPointerException.class, () -> {
			service.retire(null, "something");
		});
	}
	
	/**
	 * @verifies throw IllegalArgumentException when no reason is given
	 * @see org.openmrs.module.openhmis.commons.api.entity.IMetadataDataService#retire(OpenmrsMetadata,
	 *      String)
	 */
	@Test
	public void retire_shouldThrowIllegalArgumentExceptionWhenNoReasonIsGiven() {
		assertThrows(IllegalArgumentException.class, () -> {
			E entity = service.getById(0);
			
			service.retire(entity, null);
		});
	}
	
	/**
	 * @verifies throw NullPointerException if the metadata is null
	 * @see org.openmrs.module.openhmis.commons.api.entity.IMetadataDataService#unretire(OpenmrsMetadata)
	 */
	@Test
	public void unretire_shouldThrowNullPointerExceptionIfTheMetadataIsNull() {
		assertThrows(NullPointerException.class, () -> {
			service.unretire(null);
		});
	}
	
	/**
	 * @verifies unretire the metadata
	 * @see org.openmrs.module.openhmis.commons.api.entity.IMetadataDataService#unretire(OpenmrsMetadata)
	 */
	@Test
	public void unretire_shouldUnretireTheMetadata() {
		String reason = "test retire";
		E entity = service.getById(0);
		service.retire(entity, reason);
		
		Context.flushSession();
		
		entity = service.getById(entity.getId());
		Date dateRetired = entity.getDateRetired();
		Assertions.assertTrue(entity.getRetired());
		service.unretire(entity);
		
		Context.flushSession();
		
		entity = service.getById(entity.getId());
		Assertions.assertFalse(entity.getRetired());
		Assertions.assertNull(entity.getRetiredBy());
		Assertions.assertNull(entity.getRetireReason());
		Assertions.assertEquals(dateRetired, entity.getDateRetired());
	}
	
	/**
	 * @verifies return all retired metadata when retired is set to true
	 * @see org.openmrs.module.openhmis.commons.api.entity.IMetadataDataService#getAll(boolean)
	 */
	@Test
	public void getAll_shouldReturnAllMetadataWhenIncludeRetiredIsSetToTrue() {
		String reason = "test retire";
		E entity = service.getById(0);
		service.retire(entity, reason);
		
		Context.flushSession();
		
		List<E> entities = service.getAll(true);
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(getTestEntityCount(), entities.size());
	}
	
	/**
	 * @verifies return all unretired metadata when retired is set to false
	 * @see org.openmrs.module.openhmis.commons.api.entity.IMetadataDataService#getAll(boolean)
	 */
	@Test
	public void getAll_shouldReturnAllUnretiredMetadataWhenRetiredIsSetToFalse() {
		String reason = "test retire";
		E entity = service.getById(0);
		service.retire(entity, reason);
		
		Context.flushSession();
		
		List<E> entities = service.getAll(false);
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(getTestEntityCount() - 1, entities.size());
	}
	
	/**
	 * @verifies return all unretired metadata when retired is not specified
	 * @see org.openmrs.module.openhmis.commons.api.entity.IMetadataDataService#getAll(boolean)
	 */
	@Test
	public void getAll_shouldReturnAllUnretiredMetadataWhenRetiredIsNotSpecified() {
		String reason = "test retire";
		E entity = service.getById(0);
		service.retire(entity, reason);
		
		Context.flushSession();
		
		List<E> entities = service.getAll();
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(getTestEntityCount() - 1, entities.size());
	}
	
	@Test
	@Override
	public void getAll_shouldReturnAnEmptyListIfThereAreNoObjects() {
		List<E> entities = service.getAll();
		for (E entity : entities) {
			service.retire(entity, "test");
		}
		
		Context.flushSession();
		
		entities = service.getAll(false);
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(0, entities.size());
	}
	
	/**
	 * @verifies throw IllegalArgumentException if the name is null
	 * @see org.openmrs.module.openhmis.commons.api.entity.IMetadataDataService#getByNameFragment(String,
	 *      boolean)
	 */
	@Test
	public void getByNameFragment_shouldThrowIllegalArgumentExceptionIfTheNameIsNull() {
		assertThrows(IllegalArgumentException.class, () -> {
			service.getByNameFragment(null, true);
		});
	}
	
	/**
	 * @verifies throw IllegalArgumentException if the name is empty
	 * @see org.openmrs.module.openhmis.commons.api.entity.IMetadataDataService#getByNameFragment(String,
	 *      boolean)
	 */
	@Test
	public void getByNameFragment_shouldThrowIllegalArgumentExceptionIfTheNameIsEmpty() {
		assertThrows(IllegalArgumentException.class, () -> {
			service.getByNameFragment("", true);
		});
	}
	
	/**
	 * @verifies throw IllegalArgumentException if the name is longer than 255 characters
	 * @see org.openmrs.module.openhmis.commons.api.entity.IMetadataDataService#getByNameFragment(String,
	 *      boolean)
	 */
	@Test
	public void getByNameFragment_shouldThrowIllegalArgumentExceptionIfTheNameIsLongerThan255Characters() {
		assertThrows(IllegalArgumentException.class, () -> {
			service.getByNameFragment(StringUtils.repeat("A", 256), true);
		});
	}
	
	/**
	 * @verifies return an empty list if no metadata are found
	 * @see org.openmrs.module.openhmis.commons.api.entity.IMetadataDataService#getByNameFragment(String,
	 *      boolean)
	 */
	@Test
	public void getByNameFragment_shouldReturnAnEmptyListIfNoMetadataAreFound() {
		List<E> entities = service.getByNameFragment("NotAValidName", true);
		
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(0, entities.size());
	}
	
	/**
	 * @verifies not return retired metadata unless specified
	 * @see org.openmrs.module.openhmis.commons.api.entity.IMetadataDataService#getByNameFragment(String,
	 *      boolean)
	 */
	@Test
	public void getByNameFragment_shouldNotReturnRetiredMetadataUnlessSpecified() {
		E entity = service.getById(0);
		service.retire(entity, "something");
		Context.flushSession();
		
		List<E> entities = service.getByNameFragment("t", false);
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(getTestEntityCount() - 1, entities.size());
		
		entities = service.getByNameFragment("t", true);
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(getTestEntityCount(), entities.size());
	}
	
	/**
	 * @verifies return metadata that start with the specified name
	 * @see org.openmrs.module.openhmis.commons.api.entity.IMetadataDataService#getByNameFragment(String,
	 *      boolean)
	 */
	@Test
	public void getByNameFragment_shouldReturnMetadataThatStartWithTheSpecifiedName() {
		E entity = service.getById(0);
		
		// Search using the first four characters in the name
		List<E> entities = service.getByNameFragment(entity.getName(), false);
		Assertions.assertFalse(entities.isEmpty());
		
		// Make sure the entity is in the results
		E found = null;
		for (E result : entities) {
			if (result.getId().equals(entity.getId())) {
				found = result;
				break;
			}
		}
		
		Assertions.assertNotNull(found, "Could not find entity in search results");
	}
	
	/**
	 * @verifies return all specified metadata records if paging is null
	 * @see IMetadataDataService#getByNameFragment(String, boolean, PagingInfo)
	 */
	@Test
	public void getByNameFragment_shouldReturnAllSpecifiedMetadataRecordsIfPagingIsNull() {
		E entity = service.getById(0);
		
		// This assumes that the entity name is unique
		List<E> entities = service.getByNameFragment(entity.getName(), false, null);
		
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(1, entities.size());
		assertEntity(entity, entities.get(0));
	}
	
	/**
	 * @verifies return all specified metadata records if paging page or size is less than one
	 * @see IMetadataDataService#getByNameFragment(String, boolean, PagingInfo)
	 */
	@Test
	public void getByNameFragment_shouldReturnAllSpecifiedMetadataRecordsIfPagingPageOrSizeIsLessThanOne() {
		E entity = service.getById(0);
		
		PagingInfo paging = new PagingInfo(0, 1);
		// This assumes that the entity name is unique
		List<E> entities = service.getByNameFragment(entity.getName(), false, paging);
		
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(1, entities.size());
		assertEntity(entity, entities.get(0));
		
		paging = new PagingInfo(1, 0);
		entities = service.getByNameFragment(entity.getName(), false, paging);
		
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(1, entities.size());
		assertEntity(entity, entities.get(0));
	}
	
	/**
	 * @verifies set the paging total records to the total number of metadata records
	 * @see org.openmrs.module.openhmis.commons.api.entity.IMetadataDataService#getByNameFragment(String,
	 *      boolean, PagingInfo)
	 */
	@Test
	public void getByNameFragment_shouldSetThePagingTotalRecordsToTheTotalNumberOfMetadataRecords() {
		PagingInfo paging = new PagingInfo(1, 1);
		List<E> entities = service.getByNameFragment("T", false, paging);
		
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(1, entities.size());
		Assertions.assertEquals(Long.valueOf(getTestEntityCount()), paging.getTotalRecordCount());
	}
	
	/**
	 * @verifies not get the total paging record count if it is more than zero
	 * @see org.openmrs.module.openhmis.commons.api.entity.IMetadataDataService#getByNameFragment(String,
	 *      boolean, PagingInfo)
	 */
	@Test
	public void getByNameFragment_shouldNotGetTheTotalPagingRecordCountIfItIsMoreThanZero() {
		E entity = service.getById(0);
		PagingInfo paging = new PagingInfo(1, 1);
		
		// First check that the full total is set
		List<E> entities = service.getByNameFragment(entity.getName(), false, paging);
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(1, entities.size());
		Assertions.assertEquals((Long) 1L, paging.getTotalRecordCount());
		
		// Now manually set the total and check that it is not reset
		paging = new PagingInfo(1, 1);
		paging.setTotalRecordCount(10L);
		
		entities = service.getByNameFragment(entity.getName(), false, paging);
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(1, entities.size());
		Assertions.assertEquals((Long) 10L, paging.getTotalRecordCount());
		
		// Finally, explicitly set the paging to not load the total and make sure it is not counted
		paging = new PagingInfo(1, 1);
		paging.setLoadRecordCount(false);
		
		entities = service.getByNameFragment(entity.getName(), false, paging);
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(1, entities.size());
		Assertions.assertNull(paging.getTotalRecordCount());
	}
	
	/**
	 * @verifies return paged metadata records if paging is specified
	 * @see org.openmrs.module.openhmis.commons.api.entity.IMetadataDataService#getByNameFragment(String,
	 *      boolean, PagingInfo)
	 */
	@Test
	public void getByNameFragment_shouldReturnPagedMetadataRecordsIfPagingIsSpecified() {
		List<E> allEntities = service.getByNameFragment("T", false);
		
		PagingInfo paging = new PagingInfo(1, 1);
		List<E> entities;
		
		for (int i = 0; i < getTestEntityCount(); i++) {
			paging.setPage(i + 1);
			entities = service.getByNameFragment("T", false, paging);
			
			Assertions.assertNotNull(entities);
			Assertions.assertEquals(1, entities.size());
			Assertions.assertEquals(Long.valueOf(getTestEntityCount()), paging.getTotalRecordCount());
			
			assertEntity(allEntities.get(i), entities.get(0));
		}
	}
	
	/**
	 * @verifies return all specified metadata records if paging is null
	 * @see org.openmrs.module.openhmis.commons.api.entity.IMetadataDataService#getAll(boolean,
	 *      PagingInfo)
	 */
	@Test
	public void getAll_shouldReturnAllSpecifiedMetadataRecordsIfPagingIsNull() {
		String reason = "test retire";
		E entity = service.getById(0);
		service.retire(entity, reason);
		
		Context.flushSession();
		
		List<E> entities = service.getAll(true, null);
		
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(getTestEntityCount(), entities.size());
	}
	
	/**
	 * @verifies return all specified metadata records if paging page or size is less than one
	 * @see org.openmrs.module.openhmis.commons.api.entity.IMetadataDataService#getAll(boolean,
	 *      PagingInfo)
	 */
	@Test
	public void getAll_shouldReturnAllSpecifiedMetadataRecordsIfPagingPageOrSizeIsLessThanOne() {
		String reason = "test retire";
		E entity = service.getById(0);
		service.retire(entity, reason);
		
		Context.flushSession();
		
		PagingInfo paging = new PagingInfo(0, 1);
		List<E> entities = service.getAll(true, paging);
		
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(getTestEntityCount(), entities.size());
		
		paging = new PagingInfo(1, 0);
		entities = service.getAll(true, paging);
		
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(getTestEntityCount(), entities.size());
	}
	
	/**
	 * @verifies set the paging total records to the total number of metadata records
	 * @see org.openmrs.module.openhmis.commons.api.entity.IMetadataDataService#getAll(boolean,
	 *      PagingInfo)
	 */
	@Test
	public void getAll_shouldSetThePagingTotalRecordsToTheTotalNumberOfMetadataRecords() {
		PagingInfo paging = new PagingInfo(1, 1);
		List<E> entities = service.getAll(false, paging);
		
		Assertions.assertNotNull(entities);
		Assertions.assertEquals(1, entities.size());
		Assertions.assertEquals(Long.valueOf(getTestEntityCount()), paging.getTotalRecordCount());
	}
	
	/**
	 * @verifies not get the total paging record count if it is more than zero
	 * @see org.openmrs.module.openhmis.commons.api.entity.IMetadataDataService#getAll(boolean,
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
	 * @verifies return paged metadata records if paging is specified
	 * @see IMetadataDataService#getAll(boolean, PagingInfo)
	 */
	@Test
	public void getAll_shouldReturnPagedMetadataRecordsIfPagingIsSpecified() {
		List<E> allEntities = service.getAll();
		
		PagingInfo paging = new PagingInfo(1, 1);
		List<E> entities;
		
		for (int i = 0; i < getTestEntityCount(); i++) {
			paging.setPage(i + 1);
			entities = service.getAll(paging);
			
			Assertions.assertNotNull(entities);
			Assertions.assertEquals(1, entities.size());
			assertEntity(allEntities.get(i), entities.get(0));
		}
	}
}
