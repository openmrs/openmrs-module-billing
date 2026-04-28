/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
