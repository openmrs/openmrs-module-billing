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
import java.lang.reflect.ParameterizedType;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.base.entity.IObjectDataService;

/**
 * Support class to build property editors for entities.
 *
 * @param <E> The model class
 */
public class EntityPropertyEditor<E extends OpenmrsObject> extends PropertyEditorSupport {
	
	private final IObjectDataService<E> service;
	
	public EntityPropertyEditor(Class<? extends IObjectDataService<E>> service) {
		this.service = Context.getService(service);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public String getAsText() {
		E entity = (E) getValue();
		
		if (entity == null) {
			return "";
		} else {
			return entity.getId().toString();
		}
	}
	
	@Override
	public void setAsText(String text) {
		if (StringUtils.isEmpty(text)) {
			setValue(null);
		} else {
			E entity;
			if (NumberUtils.isNumber(text)) {
				entity = service.getById(Integer.valueOf(text));
			} else {
				entity = service.getByUuid(text);
			}
			
			setValue(entity);
			if (entity == null) {
				throw new IllegalArgumentException("Entity ('" + getEntityClass().getName() + "') not found: " + text);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	protected Class<E> getEntityClass() {
		ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
		
		return (Class) parameterizedType.getActualTypeArguments()[0];
	}
}
