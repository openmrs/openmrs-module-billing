/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.base.entity.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.openmrs.OpenmrsObject;
import org.openmrs.module.billing.api.base.entity.model.BaseCustomizableData;

/**
 * Base data service for {@link BaseCustomizableData} models.
 *
 * @param <E> The model class.
 */

public abstract class BaseCustomizableEntityDataServiceImpl<E extends BaseCustomizableData<?>> extends BaseEntityDataServiceImpl<E> {
	
	@Override
	@SuppressWarnings("unchecked")
	protected Collection<? extends OpenmrsObject> getRelatedObjects(E entity) {
		Collection<? extends OpenmrsObject> result = super.getRelatedObjects(entity);
		
		if (result == null) {
			result = new ArrayList<OpenmrsObject>();
		}
		
		Collection attributes = entity.getAttributes();
		if (attributes != null && !attributes.isEmpty()) {
			result.addAll(attributes);
		}
		
		return result;
	}
}
