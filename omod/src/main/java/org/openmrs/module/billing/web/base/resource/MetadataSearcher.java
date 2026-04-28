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

import org.openmrs.OpenmrsMetadata;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.base.entity.IMetadataDataService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;

/**
 * REST search helper for {@link org.openmrs.OpenmrsMetadata}
 *
 * @param <E> The model class
 */
public class MetadataSearcher<E extends OpenmrsMetadata> {
	
	private IMetadataDataService<E> service;
	
	public MetadataSearcher(Class<? extends IMetadataDataService<E>> serviceClass) {
		this.service = Context.getService(serviceClass);
	}
	
	public IMetadataDataService<E> getService() {
		return this.service;
	}
	
	public void setService(IMetadataDataService<E> service) {
		this.service = service;
	}
	
	/**
	 * Searches for entities using the specified name fragment.
	 *
	 * @param nameFragment The name search fragment
	 * @param context The request context
	 * @return The paged search results
	 */
	public AlreadyPaged<E> searchByName(String nameFragment, RequestContext context) {
		PagingInfo pagingInfo = PagingUtil.getPagingInfoFromContext(context);
		
		List<E> results = service.getByNameFragment(nameFragment, context.getIncludeAll(), pagingInfo);
		
		boolean hasMoreResults = ((long) pagingInfo.getPage() * pagingInfo.getPageSize()) < pagingInfo.getTotalRecordCount();
		return new AlreadyPagedWithLength<E>(context, results, hasMoreResults, pagingInfo.getTotalRecordCount());
	}
	
}
