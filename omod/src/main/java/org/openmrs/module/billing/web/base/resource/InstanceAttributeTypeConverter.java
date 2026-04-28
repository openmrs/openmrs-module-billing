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

import org.apache.commons.lang.NotImplementedException;
import org.hibernate.proxy.HibernateProxy;
import org.openmrs.module.billing.api.base.entity.model.IInstanceAttributeType;
import org.openmrs.module.billing.web.rest.controller.base.CashierResourceController;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;

/**
 * Resolves type names for
 * {@link org.openmrs.module.openhmis.commons.api.entity.model.IInstanceAttributeType}s
 *
 * @param <T> The instance attribute type class
 */
@Resource(name = RestConstants.VERSION_1 + CashierResourceController.BILLING_NAMESPACE
        + "/attributetype", supportedClass = IInstanceAttributeType.class, supportedOpenmrsVersions = { "2.0 - 2.*" })
public class InstanceAttributeTypeConverter<T extends IInstanceAttributeType<?>> extends MetadataDelegatingCrudResource<T> {
	
	private static final String NEED_SUBCLASS_HANDLER = "This operation should be handled by a subclass handler.";
	
	@Override
	public boolean hasTypesDefined() {
		return true;
	}
	
	/*TODO: This is a workaround for a possible bug in the REST module */
	@SuppressWarnings("unchecked")
	@Override
	protected String getTypeName(T delegate) {
		Class<? extends T> unproxiedClass = (Class<? extends T>) delegate.getClass();
		if (HibernateProxy.class.isAssignableFrom(unproxiedClass)) {
			unproxiedClass = (Class<? extends T>) unproxiedClass.getSuperclass();
		}
		
		return getTypeName(unproxiedClass);
	}
	
	@Override
	public T newDelegate() {
		throw new NotImplementedException(NEED_SUBCLASS_HANDLER);
	}
	
	@Override
	public T save(T delegate) {
		throw new NotImplementedException(NEED_SUBCLASS_HANDLER);
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		throw new NotImplementedException(NEED_SUBCLASS_HANDLER);
	}
	
	@Override
	public T getByUniqueId(String uniqueId) {
		throw new NotImplementedException(NEED_SUBCLASS_HANDLER);
	}
	
	@Override
	public void purge(T delegate, RequestContext context) {
		throw new NotImplementedException(NEED_SUBCLASS_HANDLER);
	}
	
}
