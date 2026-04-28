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

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.openmrs.Provider;
import org.openmrs.module.billing.api.base.entity.model.BaseInstanceCustomizableData;

/**
 * Model class that represents the {@link Bill} payment information.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends BaseInstanceCustomizableData<PaymentMode, PaymentAttribute> {
	
	private static final long serialVersionUID = 0L;
	
	private Integer paymentId;
	
	@Getter
	@Setter
	private Bill bill;
	
	@Getter
	@Setter
	private BigDecimal amount;
	
	@Getter
	@Setter
	private BigDecimal amountTendered;
	
	@Getter
	@Setter
	private Provider cashier;
	
	public Integer getId() {
		return paymentId;
	}
	
	public void setId(Integer id) {
		paymentId = id;
	}
	
	public PaymentAttribute addAttribute(PaymentModeAttributeType type, String value) {
		if (type == null) {
			throw new NullPointerException("The payment mode attribute type must be defined.");
		}
		if (value == null) {
			throw new NullPointerException(("The payment attribute value must be defined."));
		}
		
		PaymentAttribute attribute = new PaymentAttribute();
		attribute.setAttributeType(type);
		attribute.setValue(value);
		
		addAttribute(attribute);
		
		return attribute;
	}
}
