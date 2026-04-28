/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.base.entity;

import java.util.List;

import org.openmrs.OpenmrsData;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.springframework.transaction.annotation.Transactional;

/**
 * Represents classes that provide data access services to model types that implement
 * {@link org.openmrs.OpenmrsData}.
 *
 * @param <E> The {@link org.openmrs.OpenmrsData} model class.
 */
@Transactional
public interface IEntityDataService<E extends OpenmrsData> extends IObjectDataService<E> {
	
	/**
	 * Voiding an entity essentially removes it from circulation.
	 *
	 * @param entity The entity object to void.
	 * @param reason The reason for voiding.
	 * @should void the entity
	 * @should throw IllegalArgumentException with null reason parameter
	 * @should throw NullPointerException with null entity
	 */
	E voidEntity(E entity, String reason);
	
	/**
	 * Unvoid the entity record.
	 *
	 * @param entity The entity to be revived.
	 * @should unvoid the entity
	 * @should throw NullPointerException with null entity
	 */
	E unvoidEntity(E entity);
	
	/**
	 * Returns all entity records that have the specified voided status.
	 *
	 * @param includeVoided {@code true} to include voided entities.
	 * @return All the entity records that have the specified voided status.
	 * @should return all entities when includeVoided is set to true
	 * @should return all unvoided entities when includeVoided is set to false
	 */
	List<E> getAll(boolean includeVoided);
	
	/**
	 * Returns all entity records that have the specified voided status and paging.
	 *
	 * @param includeVoided {@code true} to include voided entities.
	 * @param paging The paging information.
	 * @return All the entity records that have the specified voided status.
	 * @should return an empty list if no entities are found
	 * @should not return voided entities unless specified
	 * @should return all specified metadata records if paging is null
	 * @should return all specified entity records if paging page or size is less than one
	 * @should set the paging total records to the total number of entity records
	 * @should not get the total paging record count if it is more than zero
	 * @should return paged entity records if paging is specified
	 */
	List<E> getAll(boolean includeVoided, PagingInfo paging);
}
