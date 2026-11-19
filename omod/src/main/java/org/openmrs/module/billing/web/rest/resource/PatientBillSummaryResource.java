/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.web.rest.resource;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.model.PatientBillSummary;
import org.openmrs.module.billing.web.base.resource.PagingUtil;
import org.openmrs.module.billing.web.rest.controller.base.CashierResourceController;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;

/**
 * Read-only REST resource that lists one {@link PatientBillSummary} row per patient with non-voided
 * bills. Paging counts patients. The optional {@code patientName} parameter filters patients by
 * name.
 */
@Resource(name = RestConstants.VERSION_1 + CashierResourceController.BILLING_NAMESPACE
        + "/patientBillSummary", supportedClass = PatientBillSummary.class, supportedOpenmrsVersions = { "2.0 - 2.*" })
public class PatientBillSummaryResource extends DelegatingCrudResource<PatientBillSummary> {
	
	/**
	 * Every named representation (ref, default, full, ...) returns the same summary properties. REF
	 * must be included, because the REST module uses it for list results when the request has no
	 * {@code v} parameter. A custom representation returns null, so the REST module builds it from the
	 * requested properties.
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof CustomRepresentation) {
			return null;
		}
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("patient", Representation.REF);
		description.addProperty("billCount");
		description.addProperty("totalUnpaid");
		description.addProperty("latestBillDate");
		return description;
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		return getSummaries(context);
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) {
		return getSummaries(context);
	}
	
	private AlreadyPaged<PatientBillSummary> getSummaries(RequestContext context) {
		String patientName = StringUtils.trimToNull(context.getRequest().getParameter("patientName"));
		PagingInfo pagingInfo = PagingUtil.getPagingInfoFromContext(context);
		
		List<PatientBillSummary> result = Context.getService(BillService.class).getPatientBillSummaries(patientName,
		    pagingInfo);
		
		return new AlreadyPaged<>(context, result, pagingInfo.hasMoreResults(), pagingInfo.getTotalRecordCount());
	}
	
	@Override
	protected String getUniqueId(PatientBillSummary delegate) {
		return delegate.getPatient().getUuid();
	}
	
	@Override
	public PatientBillSummary getByUniqueId(String uniqueId) {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	public PatientBillSummary newDelegate() {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	public PatientBillSummary save(PatientBillSummary delegate) {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	protected void delete(PatientBillSummary delegate, String reason, RequestContext context) {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	public void purge(PatientBillSummary delegate, RequestContext context) {
		throw new ResourceDoesNotSupportOperationException();
	}
}
