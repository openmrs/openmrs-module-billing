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
