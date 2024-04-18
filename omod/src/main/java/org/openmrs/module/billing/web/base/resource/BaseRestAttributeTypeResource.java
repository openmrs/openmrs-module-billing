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

import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.base.entity.model.IAttributeType;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Converter;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.Resource;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubclassHandler;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;

// @formatter:off
/**
 * REST resource for {@link org.openmrs.module.openhmis.commons.api.entity.model.ISimpleAttributeType}s
 * @param <E> The simple attribute type class
 */
public abstract class BaseRestAttributeTypeResource<E extends IAttributeType>
        extends BaseRestMetadataResource<E>
        implements DelegatingSubclassHandler<IAttributeType, E>, Resource, Converter<E> {
// @formatter:on
@Override
public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
    DelegatingResourceDescription description = super.getRepresentationDescription(rep);
    description.addProperty("attributeOrder");
    description.addProperty("format");
    description.addProperty("foreignKey");
    description.addProperty("regExp");
    description.addProperty("required");
    description.addProperty("retired");

    return description;
}

    @Override
    public String getTypeName() {
        return getEntityClass().getSimpleName();
    }

    @Override
    public PageableResult getAllByType(RequestContext context) {
        PagingInfo info = PagingUtil.getPagingInfoFromContext(context);

        return new AlreadyPaged<E>(context, getService().getAll(info), info.hasMoreResults());
    }

    @Override
    protected PageableResult doSearch(RequestContext context) {
        if (context.getType().equals(getTypeName())) {
            return getAllByType(context);
        } else {
            throw new ResourceDoesNotSupportOperationException();
        }
    }

    @Override
    public Class<IAttributeType> getSuperclass() {
        return IAttributeType.class;
    }

    @Override
    public Class<E> getSubclassHandled() {
        return getEntityClass();
    }
}
