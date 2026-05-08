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

import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.PaymentModeAttributeTypeService;
import org.openmrs.module.billing.api.model.PaymentModeAttributeType;
import org.openmrs.module.billing.web.rest.controller.base.CashierResourceController;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * REST resource representing a {@link PaymentModeAttributeType}.
 */
@Resource(name = RestConstants.VERSION_1 + CashierResourceController.BILLING_NAMESPACE
        + "/paymentModeAttributeType", supportedClass = PaymentModeAttributeType.class,
        supportedOpenmrsVersions = { "2.0 - 2.*" })
public class PaymentModeAttributeTypeResource extends MetadataDelegatingCrudResource<PaymentModeAttributeType> {

    private final PaymentModeAttributeTypeService service =
            Context.getService(PaymentModeAttributeTypeService.class);

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("uuid");
        description.addProperty("name");
        description.addProperty("description");
        description.addProperty("retired");
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            description.addProperty("retireReason");
            description.addProperty("format");
            description.addProperty("regExp");
        }
        return description;
    }

    @Override
    public PageableResult doGetAll(RequestContext context) throws ResponseException {
        List<PaymentModeAttributeType> attributeTypes =
                service.getAllPaymentModeAttributeTypes(context.getIncludeAll());
        return new NeedsPaging<>(attributeTypes, context);
    }

    @Override
    public PaymentModeAttributeType getByUniqueId(String uuid) {
        return service.getPaymentModeAttributeTypeByUuid(uuid);
    }

    @Override
    public PaymentModeAttributeType newDelegate() {
        return new PaymentModeAttributeType();
    }

    @Override
    public PaymentModeAttributeType save(PaymentModeAttributeType attributeType) {
        return service.savePaymentModeAttributeType(attributeType);
    }

    @Override
    public void purge(PaymentModeAttributeType attributeType, RequestContext context) throws ResponseException {
        service.purgePaymentModeAttributeType(attributeType);
    }
}
