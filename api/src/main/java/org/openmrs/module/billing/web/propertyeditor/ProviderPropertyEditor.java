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
import org.openmrs.Provider;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;

/**
 * Property editor for {@link org.openmrs.Provider}s
 */
public class ProviderPropertyEditor extends PropertyEditorSupport {
	
	@Override
	public String getAsText() {
		Provider provider = (Provider) getValue();
		
		if (provider == null) {
			return "";
		} else {
			return provider.getId().toString();
		}
	}
	
	@Override
	public void setAsText(String text) {
		ProviderService service = Context.getProviderService();
		
		if (StringUtils.isEmpty(text)) {
			setValue(null);
		} else {
			Provider provider;
			if (NumberUtils.isNumber(text)) {
				provider = service.getProvider(Integer.valueOf(text));
			} else {
				provider = service.getProviderByUuid(text);
			}
			
			setValue(provider);
			if (provider == null) {
				throw new IllegalArgumentException("Provider not found: " + text);
			}
		}
	}
}
