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

import org.openmrs.OpenmrsMetadata;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.springframework.transaction.annotation.Transactional;

/**
 * Represents classes that provide data access services to model types that inherit from
 * {@link org.openmrs.OpenmrsMetadata}.
 *
 * @param <E> The {@link org.openmrs.OpenmrsMetadata} model class.
 */
@Transactional
public interface IMetadataDataService<E extends OpenmrsMetadata> extends IObjectDataService<E> {
	
	/**
	 * Retires the specified metadata. This effectively removes the entity from circulation or use.
	 *
	 * @param metadata metadata to be retired.
	 * @param reason the reason why the metadata is being retired.
	 * @return the newly retired metadata.
	 * @should retire the metadata successfully
	 * @should throw NullPointerException when the metadata is null
	 * @should throw IllegalArgumentException when no reason is given
	 */
	E retire(E metadata, String reason);
	
	/**
	 * Unretire the specified metadata. This restores a previously retired metadata back into
	 * circulation and use.
	 *
	 * @param metadata The metadata to unretire.
	 * @return the newly unretired metadata.
	 * @should throw NullPointerException if the metadata is null
	 * @should unretire the metadata
	 */
	E unretire(E metadata);
	
	/**
	 * Returns all metadata records that have the specified retirement status.
	 *
	 * @param includeRetired {@code true} to include retired metadata.
	 * @return All the metadata records that have the specified retirement status.
	 * @should return all metadata when includeRetired is set to true
	 * @should return all unretired metadata when retired is set to false
	 */
	List<E> getAll(boolean includeRetired);
	
	/**
	 * Returns all metadata records that have the specified retirement status and paging.
	 *
	 * @param includeRetired {@code true} to include retired metadata.
	 * @param paging The paging information.
	 * @return All the metadata records that have the specified retirement status.
	 * @should return all metadata when include retired is set to true
	 * @should return all unretired metadata when retired is set to false
	 * @should return all specified metadata records if paging is null
	 * @should return all specified metadata records if paging page or size is less than one
	 * @should set the paging total records to the total number of metadata records
	 * @should not get the total paging record count if it is more than zero
	 * @should return paged metadata records if paging is specified
	 */
	List<E> getAll(boolean includeRetired, PagingInfo paging);
	
	/**
	 * Gets all the metadata that start with the specified name.
	 *
	 * @param nameFragment The name fragment.
	 * @param includeRetired Whether retired items should be included in the results.
	 * @return All metadata that starts with the specified name.
	 * @should throw IllegalArgumentException if the name is null
	 * @should throw IllegalArgumentException if the name is empty
	 * @should throw IllegalArgumentException if the name is longer than 255 characters
	 * @should return an empty list if no metadata are found
	 * @should not return retired metadata unless specified
	 * @should return metadata that start with the specified name
	 */
	List<E> getByNameFragment(String nameFragment, boolean includeRetired);
	
	/**
	 * Gets all the metadata that start with the specified name and paging.
	 *
	 * @param nameFragment The name fragment.
	 * @param includeRetired Whether retired metadata should be included in the results.
	 * @param paging The paging information.
	 * @return All metadata that starts with the specified name.
	 * @should throw IllegalArgumentException if the name is null
	 * @should throw IllegalArgumentException if the name is empty
	 * @should throw IllegalArgumentException if the name is longer than 255 characters
	 * @should return an empty list if no metadata are found
	 * @should not return retired metadata unless specified
	 * @should return metadata that start with the specified name
	 * @should return all specified metadata records if paging is null
	 * @should return all specified metadata records if paging page or size is less than one
	 * @should set the paging total records to the total number of metadata records
	 * @should not get the total paging record count if it is more than zero
	 * @should return paged metadata records if paging is specified
	 */
	List<E> getByNameFragment(String nameFragment, boolean includeRetired, PagingInfo paging);
}
