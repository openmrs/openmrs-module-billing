/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.base.entity.model;

import java.util.List;

import org.openmrs.OpenmrsMetadata;

/**
 * Represents a class that defines the type of an {@link IInstanceCustomizable}. Each
 * {@link IInstanceType} has attributes that are only used by instances of that type and not shared
 * with other instance types.
 *
 * @param <TAttributeType> The instance attribute type class.
 */
public interface IInstanceType<TAttributeType extends IInstanceAttributeType<?>> extends OpenmrsMetadata {
	
	/**
	 * Gets the {@link TAttributeType}'s for this {@link IInstanceType}.
	 *
	 * @return The attribute types.
	 */
	List<TAttributeType> getAttributeTypes();
	
	/**
	 * Sets the {@link TAttributeType}'s for this {@link IInstanceType}.
	 *
	 * @param attributeTypes The attribute types.
	 */
	void setAttributeTypes(List<TAttributeType> attributeTypes);
	
	/**
	 * Adds the specified {@link TAttributeType}.
	 *
	 * @param attributeType The attribute type to add.
	 */
	void addAttributeType(TAttributeType attributeType);
	
	/**
	 * Adds the specified {@link TAttributeType} at the specified index.
	 *
	 * @param index The index where the attribute type will be inserted or {@code null} to insert at the
	 *            end.
	 * @param attributeType The attribute type to add.
	 */
	void addAttributeType(Integer index, TAttributeType attributeType);
	
	/**
	 * Removes the specified {@link TAttributeType}.
	 *
	 * @param attributeType The attribute type to remove.
	 */
	void removeAttributeType(TAttributeType attributeType);
}
