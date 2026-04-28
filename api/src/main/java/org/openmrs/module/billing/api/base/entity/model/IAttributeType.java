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

import org.openmrs.OpenmrsMetadata;
import org.openmrs.customdatatype.CustomValueDescriptor;

/**
 * Represents classes that define attribute type information.
 */
public interface IAttributeType extends OpenmrsMetadata, CustomValueDescriptor {
	
	Integer getAttributeOrder();
	
	void setAttributeOrder(Integer attributeOrder);
	
	String getFormat();
	
	void setFormat(String format);
	
	Integer getForeignKey();
	
	void setForeignKey(Integer foreignKey);
	
	String getRegExp();
	
	void setRegExp(String regExp);
	
	Boolean getRequired();
	
	void setRequired(Boolean required);
}
