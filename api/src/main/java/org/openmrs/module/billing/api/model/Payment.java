/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.billing.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openmrs.Provider;
import org.openmrs.module.billing.api.base.entity.model.BaseInstanceCustomizableData;

import java.math.BigDecimal;

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
