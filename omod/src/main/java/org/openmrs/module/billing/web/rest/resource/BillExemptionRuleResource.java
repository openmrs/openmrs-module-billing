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

import org.apache.commons.lang.StringEscapeUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillExemptionService;
import org.openmrs.module.billing.api.evaluator.ScriptType;
import org.openmrs.module.billing.api.model.BillExemption;
import org.openmrs.module.billing.api.model.BillExemptionRule;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.ArrayList;
import java.util.List;

/**
 * REST sub-resource representing a {@link BillExemptionRule}.
 */
@SubResource(parent = BillExemptionResource.class, path = "rule", supportedClass = BillExemptionRule.class,
        supportedOpenmrsVersions = {"2.0 - 2.*"})
public class BillExemptionRuleResource extends DelegatingSubResource<BillExemptionRule, BillExemption, BillExemptionResource> {

	@Override
	public BillExemptionRule newDelegate() {
		return new BillExemptionRule();
	}

	@Override
	public BillExemptionRule save(BillExemptionRule delegate) {
		BillExemption exemption = delegate.getBillingExemption();
		if (exemption != null) {
			getService().save(exemption);
		}
		return delegate;
	}

	@Override
	public BillExemption getParent(BillExemptionRule instance) {
		return instance.getBillingExemption();
	}

	@Override
	public void setParent(BillExemptionRule instance, BillExemption parent) {
		instance.setBillingExemption(parent);
	}

	@Override
	public PageableResult doGetAll(BillExemption parent, RequestContext context) throws ResponseException {
		List<BillExemptionRule> rules = parent.getRules();
		if (rules == null) {
			rules = new ArrayList<>();
		}
		return new NeedsPaging<>(rules, context);
	}

	@Override
	public BillExemptionRule getByUniqueId(String uniqueId) {
		throw new ResourceDoesNotSupportOperationException("BillingExemptionRule does not support lookup by UUID");
	}

	@Override
	protected void delete(BillExemptionRule delegate, String reason, RequestContext context) throws ResponseException {
		if (delegate.getVoided()) {
			return;
		}
		delegate.setVoided(true);
		delegate.setVoidReason(reason);
		BillExemption exemption = delegate.getBillingExemption();
		if (exemption != null) {
			getService().save(exemption);
		}
	}

	@Override
	public void purge(BillExemptionRule delegate, RequestContext context) throws ResponseException {
		BillExemption exemption = delegate.getBillingExemption();
		if (exemption != null) {
			exemption.getRules().remove(delegate);
			getService().save(exemption);
		}
	}

	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();

		if (rep instanceof RefRepresentation) {
			description.addProperty("uuid");
			description.addProperty("scriptType");
			description.addProperty("script");
		} else if (rep instanceof DefaultRepresentation) {
			description.addProperty("uuid");
			description.addProperty("scriptType");
			description.addProperty("script");
			description.addProperty("voided");
		} else if (rep instanceof FullRepresentation) {
			description.addProperty("uuid");
			description.addProperty("scriptType");
			description.addProperty("script");
			description.addProperty("voided");
			description.addProperty("voidReason");
			description.addProperty("auditInfo");
		}

		return description;
	}

	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("scriptType");
		description.addProperty("script");
		return description;
	}

	@Override
	public DelegatingResourceDescription getUpdatableProperties() {
		return getCreatableProperties();
	}

	@PropertySetter("scriptType")
	public void setScriptType(BillExemptionRule instance, String scriptType) {
		if (scriptType != null) {
			instance.setScriptType(ScriptType.valueOf(scriptType));
		}
	}

	@PropertySetter("script")
	public void setScript(BillExemptionRule instance, String script) {
		if (script != null) {
			instance.setScript(StringEscapeUtils.unescapeHtml(script));
		}
	}

	private BillExemptionService getService() {
		return Context.getService(BillExemptionService.class);
	}
}