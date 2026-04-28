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
 * Base class for {@link org.openmrs.OpenmrsObject} models that can be customized based on an
 * {@link IInstanceType}
 * @param <TInstanceType> The model instance type class.
 * @param <TAttribute> The model attribute class.
 */
public abstract class BaseInstanceCustomizableObject<
			TInstanceType extends IInstanceType<?>,
			TAttribute extends IInstanceAttribute<?, ?, ?>>
		extends BaseCustomizableObject<TAttribute>
		implements IInstanceCustomizable<TInstanceType, TAttribute> {
// @formatter:on
	private static final long serialVersionUID = 1L;
	
	private TInstanceType instanceType;
	
	@SuppressWarnings({ "unchecked" })
	static <TA extends IInstanceAttribute, I extends IInstanceCustomizable> void addAttribute(I instance, TA attribute) {
		if (attribute == null) {
			throw new NullPointerException("The attribute to add must be defined.");
		}
		
		if (instance.getAttributes() == null) {
			// Using LinkedHashSet because it is ordered by entry versus HashSet which is not.
			instance.setAttributes(new LinkedHashSet<TA>());
		}
		
		attribute.setOwner(instance);
		instance.getAttributes().add(attribute);
	}
	
	@SuppressWarnings({ "unchecked" })
	static <TA extends IInstanceAttribute<?, ?, ?>, I extends IInstanceCustomizable<?, ? extends TA>> void removeAttribute(
	        I instance, TA attribute) {
		if (instance.getAttributes() == null || attribute == null) {
			return;
		}
		attribute.setOwner(null);
		instance.getAttributes().remove(attribute);
	}
	
	// @formatter:off
	public static <TA extends IInstanceAttribute<?, ?, ?>, I extends IInstanceCustomizable<?, ? extends TA>>
			Set<TA>	getActiveAttributes(I instance) {
	// @formatter:on
		Set<TA> ret = new HashSet<TA>();
		if (instance.getAttributes() != null) {
			for (TA attr : instance.getAttributes()) {
				if (!attr.getAttributeType().getRetired()) {
					ret.add(attr);
				}
			}
		}
		return ret;
	}
	
	// @formatter:off
	public static <TA extends IInstanceAttribute<?, ?, ?>, I extends IInstanceCustomizable<?, ? extends TA>>
			Set<TA> getActiveAttributes(I instance, CustomValueDescriptor ofType) {
	// @formatter:on
		Set<TA> ret = new HashSet<TA>();
		if (instance.getAttributes() != null) {
			for (TA attr : instance.getAttributes()) {
				if (attr.getAttributeType().equals(ofType) && !attr.getAttributeType().getRetired()) {
					ret.add(attr);
				}
			}
		}
		return ret;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected void onAddAttribute(TAttribute attribute) {
		super.onAddAttribute(attribute);
		
		((IInstanceAttribute) attribute).setOwner(this);
	}
	
	@Override
	protected void onRemoveAttribute(TAttribute attribute) {
		super.onRemoveAttribute(attribute);
		
		attribute.setOwner(null);
	}
	
	@Override
	public TInstanceType getInstanceType() {
		return instanceType;
	}
	
	@Override
	public void setInstanceType(TInstanceType instanceType) {
		this.instanceType = instanceType;
	}
}
