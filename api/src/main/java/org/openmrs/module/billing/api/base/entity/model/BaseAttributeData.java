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

import org.openmrs.module.billing.api.base.util.AttributeUtil;

// @formatter:off
/**
 * Base class for {@link org.openmrs.OpenmrsData} attribute models.
 * @param <TOwner> The class of the attribute owner.
 * @param <TAttributeType> The class of the attribute type.
 */
public abstract class BaseAttributeData<
			TOwner extends ICustomizable<?>,
			TAttributeType extends IAttributeType>
		extends BaseSerializableOpenmrsData
		implements IAttribute<TOwner, TAttributeType> {
// @formatter:on
	private static final long serialVersionUID = 0L;
	
	private Integer attributeId;
	
	private TOwner owner;
	
	private TAttributeType attributeType;
	
	private String value;
	
	@Override
	public Integer getId() {
		return attributeId;
	}
	
	@Override
	public void setId(Integer id) {
		attributeId = id;
	}
	
	public TOwner getOwner() {
		return owner;
	}
	
	public void setOwner(TOwner owner) {
		this.owner = owner;
	}
	
	public TAttributeType getAttributeType() {
		return attributeType;
	}
	
	public void setAttributeType(TAttributeType attributeType) {
		this.attributeType = attributeType;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public Object getHydratedValue() {
		return AttributeUtil.tryToHydrateObject(getAttributeType().getFormat(), value);
	}
}
