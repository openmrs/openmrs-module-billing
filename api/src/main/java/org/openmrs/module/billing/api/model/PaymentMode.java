/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.model;

import org.openmrs.module.billing.api.base.entity.model.BaseInstanceCustomizableType;

/**
 * Model class that represents a mode of payment (e.g., cash, check, credit card).
 */
public class PaymentMode extends BaseInstanceCustomizableType<PaymentModeAttributeType> {
	
	private static final long serialVersionUID = 0L;
	
	private Integer sortOrder;
	
	public Integer getSortOrder() {
		return sortOrder;
	}
	
	public void setSortOrder(Integer sortOrder) {
		this.sortOrder = sortOrder;
	}
	
	public PaymentModeAttributeType addAttributeType(String name, String format, String regExp, boolean required) {
		PaymentModeAttributeType attributeType = new PaymentModeAttributeType();
		
		attributeType.setOwner(this);
		
		attributeType.setName(name);
		attributeType.setFormat(format);
		attributeType.setRegExp(regExp);
		attributeType.setRequired(required);
		
		addAttributeType(attributeType);
		
		return attributeType;
	}
}
