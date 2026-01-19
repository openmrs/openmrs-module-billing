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
package org.openmrs.module.billing.web.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.CashPointService;
import org.openmrs.module.billing.api.model.CashPoint;

/**
 * Property editor for {@link CashPoint}s
 */
public class CashPointPropertyEditor extends PropertyEditorSupport {
	
	@Override
	public String getAsText() {
		CashPoint cashPoint = (CashPoint) getValue();
		
		if (cashPoint == null) {
			return "";
		} else {
			return cashPoint.getId().toString();
		}
	}
	
	@Override
	public void setAsText(String text) {
		CashPointService service = Context.getService(CashPointService.class);
		
		if (StringUtils.isEmpty(text)) {
			setValue(null);
		} else {
			CashPoint cashPoint;
			if (NumberUtils.isNumber(text)) {
				cashPoint = service.getCashPoint(Integer.valueOf(text));
			} else {
				cashPoint = service.getCashPointByUuid(text);
			}
			
			setValue(cashPoint);
			if (cashPoint == null) {
				throw new IllegalArgumentException("CashPoint not found: " + text);
			}
		}
	}
}
