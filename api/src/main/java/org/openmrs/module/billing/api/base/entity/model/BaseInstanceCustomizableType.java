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

import java.util.ArrayList;
import java.util.List;

import org.openmrs.api.APIException;

// @formatter:off
/**
 * Base class for instance type models.
 * @param <TAttributeType> The attribute type class.
 */
public abstract class BaseInstanceCustomizableType<TAttributeType extends IInstanceAttributeType<?>>
        extends BaseSerializableOpenmrsMetadata implements IInstanceType<TAttributeType> {
// @formatter:on
	private static final long serialVersionUID = 0L;
	
	private Integer customizableInstanceTypeId;
	
	private List<TAttributeType> attributeTypes;
	
	@Override
	public void addAttributeType(TAttributeType attributeType) {
		addAttributeType(null, attributeType);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void addAttributeType(Integer index, TAttributeType attributeType) {
		if (attributeType == null) {
			throw new NullPointerException("The payment mode attribute type to add must be defined.");
		}
		
		if (attributeType.getOwner() != this) {
			// Note that this may cause issues if the attribute type class does not have this class as the owner class.
			//  I'm not sure how to make generics check this at compile-time, I tried with self-bounded generic parameters
			//  but could never get it working.  Such is life.
			((IInstanceAttributeType) attributeType).setOwner(this);
		}
		
		if (this.attributeTypes == null) {
			this.attributeTypes = new ArrayList<TAttributeType>();
		}
		
		if (index == null) {
			attributeType.setAttributeOrder(getAttributeTypes().size());
			getAttributeTypes().add(attributeType);
		} else {
			if (index > getAttributeTypes().size()) {
				throw new APIException("Invalid attribute order. Should not leave space in the list (list length: "
				        + getAttributeTypes().size() + ", index given: " + index + ").");
			}
			
			attributeType.setAttributeOrder(index);
			getAttributeTypes().add(index, attributeType);
		}
		
		this.attributeTypes.add(attributeType);
	}
	
	@Override
	public void removeAttributeType(TAttributeType attributeType) {
		if (attributeType != null && this.attributeTypes != null) {
			this.attributeTypes.remove(attributeType);
		}
	}
	
	@Override
	public Integer getId() {
		return customizableInstanceTypeId;
	}
	
	@Override
	public void setId(Integer id) {
		customizableInstanceTypeId = id;
	}
	
	@Override
	public List<TAttributeType> getAttributeTypes() {
		return attributeTypes;
	}
	
	@Override
	public void setAttributeTypes(List<TAttributeType> attributeTypes) {
		this.attributeTypes = attributeTypes;
	}
}
