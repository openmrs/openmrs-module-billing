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

import org.apache.commons.lang3.BooleanUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.PaymentModeService;
import org.openmrs.module.billing.web.rest.controller.base.CashierResourceController;
import org.openmrs.module.billing.api.model.PaymentMode;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * REST resource representing a {@link PaymentMode}.
 */
@Resource(name = RestConstants.VERSION_1 + CashierResourceController.BILLING_NAMESPACE
        + "/paymentMode", supportedClass = PaymentMode.class, supportedOpenmrsVersions = { "2.7.8 - 9.*" })
public class PaymentModeResource extends MetadataDelegatingCrudResource<PaymentMode> {
	
	private final PaymentModeService paymentModeService = Context.getService(PaymentModeService.class);
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("uuid");
		description.addProperty("name");
		description.addProperty("description");
		description.addProperty("retired");
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			description.addProperty("retireReason");
			description.addProperty("sortOrder");
			description.addProperty("attributeTypes", Representation.REF);
		} else if (rep instanceof CustomRepresentation) {
			// For custom representation, must be null
			// - let the user decide which properties should be included in the response
			return null;
		}
		return description;
	}
	
	@Override
	public PageableResult doGetAll(RequestContext context) throws ResponseException {
		boolean includeRetired = BooleanUtils.toBoolean(context.getParameter("includeAll"));
		List<PaymentMode> paymentModes = paymentModeService.getPaymentModes(includeRetired);
		return new NeedsPaging<>(paymentModes, context);
	}
	
	@Override
	public PaymentMode getByUniqueId(String s) {
		return paymentModeService.getPaymentModeByUuid(s);
	}
	
	@Override
	public PaymentMode newDelegate() {
		return new PaymentMode();
	}
	
	@Override
	public PaymentMode save(PaymentMode paymentMode) {
		return paymentModeService.savePaymentMode(paymentMode);
	}
	
	@Override
	public void purge(PaymentMode paymentMode, RequestContext requestContext) throws ResponseException {
		paymentModeService.purgePaymentMode(paymentMode);
	}
}
