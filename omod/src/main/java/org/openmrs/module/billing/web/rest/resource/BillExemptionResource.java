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
package org.openmrs.module.billing.web.rest.resource;

import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillExemptionService;
import org.openmrs.module.billing.api.model.BillExemption;
import org.openmrs.module.billing.api.model.BillExemptionRule;
import org.openmrs.module.billing.api.model.ExemptionType;
import org.openmrs.module.billing.web.rest.controller.base.CashierResourceController;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.List;

/**
 * REST resource representing a {@link BillExemption}.
 */
@Resource(name = RestConstants.VERSION_1 + CashierResourceController.BILLING_NAMESPACE + "/billExemption",
        supportedClass = BillExemption.class, supportedOpenmrsVersions = {"2.0 - 2.*"})
public class BillExemptionResource extends MetadataDelegatingCrudResource<BillExemption> {

	@Override
	public BillExemption newDelegate() {
		return new BillExemption();
	}

	@Override
	public BillExemption save(BillExemption delegate) {
		return getService().save(delegate);
	}

	@Override
	public BillExemption getByUniqueId(String uniqueId) {
		return getService().getBillingExemptionByUuid(uniqueId);
	}

	@Override
	public void delete(BillExemption delegate, String reason, RequestContext context) throws ResponseException {
		if (delegate.getRetired()) {
			return;
		}
		delegate.setRetired(true);
		delegate.setRetireReason(reason);
		getService().save(delegate);
	}

	@Override
	public void purge(BillExemption delegate, RequestContext context) throws ResponseException {
		throw new UnsupportedOperationException("Purge is not supported for BillingExemption");
	}

	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();

		if (rep instanceof RefRepresentation) {
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("retired");
		} else if (rep instanceof DefaultRepresentation) {
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("retired");
			description.addProperty("retireReason");
			description.addProperty("concept", Representation.REF);
			description.addProperty("exemptionType");
			description.addProperty("rules", Representation.DEFAULT);
		} else if (rep instanceof FullRepresentation) {
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("retired");
			description.addProperty("retireReason");
			description.addProperty("concept", Representation.DEFAULT);
			description.addProperty("exemptionType");
			description.addProperty("rules", Representation.FULL);
			description.addProperty("auditInfo");
		}

		return description;
	}

	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("name");
		description.addProperty("description");
		description.addProperty("concept");
		description.addProperty("exemptionType");
		description.addProperty("rules");
		return description;
	}

	@Override
	public DelegatingResourceDescription getUpdatableProperties() {
		return getCreatableProperties();
	}

	@PropertySetter("rules")
	public void setRules(BillExemption instance, List<BillExemptionRule> rules) {
		if (rules != null) {
			for (BillExemptionRule rule : rules) {
				rule.setBillingExemption(instance);
			}
			instance.setRules(rules);
		}
	}

	@PropertySetter("exemptionType")
	public void setExemptionType(BillExemption instance, String exemptionType) {
		if (exemptionType != null) {
			instance.setExemptionType(ExemptionType.valueOf(exemptionType));
		}
	}

	private BillExemptionService getService() {
		return Context.getService(BillExemptionService.class);
	}
}