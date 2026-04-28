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
