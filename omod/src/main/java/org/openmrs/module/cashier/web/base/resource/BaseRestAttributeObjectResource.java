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
package org.openmrs.module.cashier.web.base.resource;

import org.openmrs.Concept;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.cashier.api.base.entity.model.IAttribute;
import org.openmrs.module.cashier.api.base.entity.model.IAttributeType;
import org.openmrs.module.webservices.rest.util.ReflectionUtil;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ConversionException;

// @formatter:off
/**
 * REST resource for {@link org.openmrs.OpenmrsObject}
 * {@link org.openmrs.module.openhmis.commons.api.entity.model.IAttribute}s.
 * @param <E> The customizable instance attribute class
 */
public abstract class BaseRestAttributeObjectResource<
			E extends IAttribute<?, TAttributeType> & OpenmrsObject,
			TAttributeType extends IAttributeType>
        extends BaseRestObjectResource<E> {
// @formatter:on
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = super.getRepresentationDescription(rep);
		if (!(rep instanceof RefRepresentation)) {
			description.addProperty("value");
			description.addProperty("attributeType", Representation.REF);
			description.addProperty("order", ReflectionUtil.findMethod(getClass(), "getAttributeOrder"));
		}

		return description;
	}

	@Override
	public void setProperty(Object instance, String propertyName, Object value) throws ConversionException {
		// Since the order property is from the attribute type and isn't modifyable, don't do anything if it's being set
		if (propertyName.equals("order")) {
			return;
		}
		super.setProperty(instance, propertyName, value);
	}

	protected Object baseGetValue(E instance) {
		if (instance.getAttributeType().getFormat().contains("Concept")) {
			ConceptService service = Context.getService(ConceptService.class);
			Concept concept = service.getConcept(instance.getValue());

			return concept == null ? "" : concept.getDisplayString();
		} else {
			return instance.getHydratedValue();
		}
	}

	protected void baseSetAttributeType(E instance, TAttributeType attributeType) {
		instance.setAttributeType(attributeType);
	}

	public Integer getAttributeOrder(E instance) {
		return instance.getAttributeType().getAttributeOrder();
	}
}
