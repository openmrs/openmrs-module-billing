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

import java.util.List;

import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.api.Converter;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;

/**
 * Represents paged results that include a total length.
 *
 * @param <T> The model class.
 */
public class AlreadyPagedWithLength<T> extends AlreadyPaged<T> {
	
	private final long length;
	
	public AlreadyPagedWithLength(RequestContext context, List<T> results, boolean hasMoreResults, long length) {
		super(context, results, hasMoreResults);
		this.length = length;
	}
	
	@Override
	public SimpleObject toSimpleObject(Converter converter) {
		SimpleObject obj = super.toSimpleObject(converter);
		obj.add("length", this.length);
		return obj;
	}
}
