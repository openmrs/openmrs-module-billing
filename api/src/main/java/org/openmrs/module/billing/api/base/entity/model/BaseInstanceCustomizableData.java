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

/**
 * Base class for {@link org.openmrs.OpenmrsData} models that can be customized based on an
 * {@link IInstanceType}
 * 
 * @param <TInstanceType> The model instance type class.
 * @param <TAttribute> The model attribute class.
 */
public abstract class BaseInstanceCustomizableData<TInstanceType extends IInstanceType<?>, TAttribute extends IInstanceAttribute<?, ?, ?>> extends BaseCustomizableData<TAttribute> implements IInstanceCustomizable<TInstanceType, TAttribute> {
	
	// @formatter:on
	private static final long serialVersionUID = 1L;
	
	private TInstanceType instanceType;
	
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
