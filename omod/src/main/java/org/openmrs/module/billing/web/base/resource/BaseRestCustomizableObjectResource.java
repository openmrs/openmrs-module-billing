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
package org.openmrs.module.billing.web.base.resource;

import java.util.HashSet;
import java.util.List;

import org.openmrs.OpenmrsObject;
import org.openmrs.module.billing.api.base.entity.model.IAttribute;
import org.openmrs.module.billing.api.base.entity.model.ICustomizable;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;

// @formatter:off
/**
 * REST resource for {@link org.openmrs.OpenmrsObject}
 * {@link org.openmrs.module.openhmis.commons.api.entity.model.ICustomizable}s.
 * @param <E> The customizable model class
 * @param <TAttribute> The model attribute class
 */
public abstract class BaseRestCustomizableObjectResource<
			E extends ICustomizable<TAttribute> & OpenmrsObject,
			TAttribute extends IAttribute<E, ?>>
		extends BaseRestObjectResource<E> {
// @formatter:on
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = super.getRepresentationDescription(rep);
		if (!(rep instanceof RefRepresentation)) {
			description.addProperty("attributes");
		}
		
		return description;
	}
	
	protected void baseSetAttributes(E instance, List<TAttribute> attributes) {
		if (instance.getAttributes() == null) {
			instance.setAttributes(new HashSet<TAttribute>());
		}
		
		BaseRestDataResource.syncCollection(instance.getAttributes(), attributes);
		for (TAttribute attribute : instance.getAttributes()) {
			attribute.setOwner(instance);
		}
	}
}
