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
