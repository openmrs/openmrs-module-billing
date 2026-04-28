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

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.billing.api.base.entity.model.IInstanceAttributeType;
import org.openmrs.module.billing.api.base.entity.model.IInstanceType;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;

// @formatter:off
/**
 * REST resource for {@link org.openmrs.module.openhmis.commons.api.entity.model.IInstanceAttributeType}s.
 * @param <E> The customizable instance attribute class
 * @param <TAttributeType> The attribute type class
 */
public abstract class BaseRestInstanceTypeResource<
			E extends IInstanceType<TAttributeType>,
			TAttributeType extends IInstanceAttributeType<E>>
        extends BaseRestMetadataResource<E> {
// @formatter:on
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = super.getRepresentationDescription(rep);
		if (!(rep instanceof RefRepresentation)) {
			description.addProperty("attributeTypes");
		}
		
		return description;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = super.getCreatableProperties();
		description.addProperty("attributeTypes");
		
		return description;
	}
	
	protected void baseSetAttributeTypes(E instance, List<TAttributeType> attributeTypes) {
		if (instance.getAttributeTypes() == null) {
			instance.setAttributeTypes(new ArrayList<TAttributeType>());
		}
		
		BaseRestDataResource.syncCollection(instance.getAttributeTypes(), attributeTypes);
		for (TAttributeType type : instance.getAttributeTypes()) {
			type.setOwner(instance);
		}
	}
}
