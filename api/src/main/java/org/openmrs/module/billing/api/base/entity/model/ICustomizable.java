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

import java.util.Set;

import org.openmrs.customdatatype.CustomValueDescriptor;

/**
 * Represents a class that can be customized with attributes.
 *
 * @param <TAttribute> The {@link ISimpleAttribute} class.
 */
public interface ICustomizable<TAttribute extends IAttribute<?, ?>> {
	
	/**
	 * Gets the {@link TAttribute}'s added to this instance.
	 *
	 * @return The attributes for this instance.
	 */
	Set<TAttribute> getAttributes();
	
	/**
	 * Sets the {@link TAttribute}'s for this instance.
	 *
	 * @param attributes The attributes for this instance.
	 */
	void setAttributes(Set<TAttribute> attributes);
	
	/**
	 * Adds an {@link TAttribute} to the attributes for this instance.
	 *
	 * @param attribute The attribute to add.
	 */
	void addAttribute(TAttribute attribute);
	
	/**
	 * Removes an {@link TAttribute} from the attributes for this instance.
	 *
	 * @param attribute The attribute to remove.
	 */
	void removeAttribute(TAttribute attribute);
	
	/**
	 * Gets the active (that is, not retired) {@link TAttribute}'s for this instance.
	 *
	 * @return The active attributes.
	 */
	Set<TAttribute> getActiveAttributes();
	
	/**
	 * Gets the active (that is, not retired) {@link TAttribute}'s of the specified type for this
	 * instance.
	 *
	 * @param ofType The attribute type.
	 * @return The active attributes.
	 */
	Set<TAttribute> getActiveAttributes(CustomValueDescriptor ofType);
}
