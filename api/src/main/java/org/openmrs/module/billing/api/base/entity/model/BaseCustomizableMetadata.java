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

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.openmrs.customdatatype.CustomValueDescriptor;

// @formatter:off
/**
 * Base class for {@link org.openmrs.OpenmrsMetadata} models that can be customized.
 * @param <TAttribute> The model attribute class.
 */
public abstract class BaseCustomizableMetadata<TAttribute extends IAttribute<?, ?>>
		extends BaseSerializableOpenmrsMetadata
		implements ICustomizable<TAttribute> {
// @formatter:on
	private static final long serialVersionUID = 0L;
	
	private Set<TAttribute> attributes;
	
	protected void onAddAttribute(TAttribute attribute) {
		// Just here to allow subclass to add custom logic
	}
	
	protected void onRemoveAttribute(TAttribute attribute) {
		// Just here to allow subclass to add custom logic
	}
	
	@Override
	public Set<TAttribute> getAttributes() {
		return attributes;
	}
	
	@Override
	public void setAttributes(Set<TAttribute> attributes) {
		this.attributes = attributes;
	}
	
	@Override
	public Set<TAttribute> getActiveAttributes() {
		return getActiveAttributes(null);
	}
	
	@Override
	public Set<TAttribute> getActiveAttributes(CustomValueDescriptor ofType) {
		Set<TAttribute> result = new HashSet<TAttribute>();
		if (getAttributes() != null) {
			for (TAttribute attribute : getAttributes()) {
				if ((ofType == null || attribute.getAttributeType().equals(ofType))
				        && !attribute.getAttributeType().getRetired()) {
					result.add(attribute);
				}
			}
		}
		
		return result;
	}
	
	@Override
	public void addAttribute(TAttribute attribute) {
		if (attribute == null) {
			throw new NullPointerException("The attribute to add must be defined.");
		}
		
		Set<TAttribute> attributes = this.getAttributes();
		if (attributes == null) {
			// Using LinkedHashSet because it is ordered by entry versus HashSet which is not.
			attributes = new LinkedHashSet<TAttribute>();
			
			setAttributes(attributes);
		}
		
		onAddAttribute(attribute);
		attributes.add(attribute);
	}
	
	@Override
	public void removeAttribute(TAttribute attribute) {
		if (getAttributes() == null || attribute == null) {
			return;
		}
		
		onRemoveAttribute(attribute);
		getAttributes().remove(attribute);
	}
}
