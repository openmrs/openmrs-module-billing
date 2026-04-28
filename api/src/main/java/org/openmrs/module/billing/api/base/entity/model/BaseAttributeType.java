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

import org.openmrs.BaseOpenmrsMetadata;

/**
 * Base class for attribute type models.
 */
public abstract class BaseAttributeType extends BaseOpenmrsMetadata implements IAttributeType {
	
	private Integer attributeTypeId;
	
	private Integer attributeOrder;
	
	private String format;
	
	private Integer foreignKey;
	
	private String regExp;
	
	private Boolean required = false;
	
	/**
	 * Gets the attribute type id.
	 *
	 * @return The attribute type id
	 */
	@Override
	public Integer getId() {
		return this.attributeTypeId;
	}
	
	/**
	 * Sets the attribute type id.
	 *
	 * @param id The attribute type id
	 */
	@Override
	public void setId(Integer id) {
		this.attributeTypeId = id;
	}
	
	/**
	 * Gets the attribute order.
	 *
	 * @return The attribute order
	 */
	public Integer getAttributeOrder() {
		return attributeOrder;
	}
	
	/**
	 * Sets the attribute order.
	 *
	 * @param attributeOrder The attribute order
	 */
	public void setAttributeOrder(Integer attributeOrder) {
		this.attributeOrder = attributeOrder;
	}
	
	/**
	 * Gets the attribute type format.
	 *
	 * @return The format
	 */
	public String getFormat() {
		return format;
	}
	
	/**
	 * Sets the attribute type format.
	 *
	 * @param format The format
	 */
	public void setFormat(String format) {
		this.format = format;
	}
	
	/**
	 * Gets the attribute type foreign key.
	 *
	 * @return The foreign key
	 */
	public Integer getForeignKey() {
		return foreignKey;
	}
	
	/**
	 * Sets the attribute type foreign key.
	 *
	 * @param foreignKey THe foreign key
	 */
	public void setForeignKey(Integer foreignKey) {
		this.foreignKey = foreignKey;
	}
	
	/**
	 * Gets the attribute type regular expression.
	 *
	 * @return The regular expression
	 */
	public String getRegExp() {
		return regExp;
	}
	
	/**
	 * Sets the attribute type regular expression
	 *
	 * @param regExp The regular expression
	 */
	public void setRegExp(String regExp) {
		this.regExp = regExp;
	}
	
	/**
	 * Gets whether this attribute type is required.
	 *
	 * @return {@code true} if the attribute type is required; otherwise, {@code false}
	 */
	public Boolean getRequired() {
		return required;
	}
	
	/**
	 * Sets whether this attribute type is required.
	 *
	 * @param required {@code true} if the attribute type is required; otherwise, {@code false}
	 */
	public void setRequired(Boolean required) {
		this.required = required;
	}
	
	@Override
	public String getDatatypeClassname() {
		return getFormat();
	}
	
	@Override
	public String getDatatypeConfig() {
		return getForeignKey().toString();
	}
	
	@Override
	public String getPreferredHandlerClassname() {
		// Default to null to simplify concrete classes
		return null;
	}
	
	@Override
	public String getHandlerConfig() {
		// Default to null to simplify concrete classes
		return null;
	}
}
