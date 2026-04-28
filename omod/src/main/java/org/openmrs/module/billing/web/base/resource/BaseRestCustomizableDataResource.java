/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.web.base.resource;

import java.util.HashSet;
import java.util.List;

import org.openmrs.OpenmrsData;
import org.openmrs.module.billing.api.base.entity.model.IAttribute;
import org.openmrs.module.billing.api.base.entity.model.ICustomizable;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;

// @formatter:off
/**
 * REST resource for {@link org.openmrs.OpenmrsData}
 * {@link org.openmrs.module.openhmis.commons.api.entity.model.ICustomizable}s.
 * @param <E> The customizable model class
 * @param <TAttribute> The model attribute class
 */
public abstract class BaseRestCustomizableDataResource<
			E extends ICustomizable<TAttribute> & OpenmrsData,
			TAttribute extends IAttribute<E, ?>>
		extends BaseRestDataResource<E> {
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
		
		syncCollection(instance.getAttributes(), attributes);
		for (TAttribute attribute : instance.getAttributes()) {
			attribute.setOwner(instance);
		}
	}
}
