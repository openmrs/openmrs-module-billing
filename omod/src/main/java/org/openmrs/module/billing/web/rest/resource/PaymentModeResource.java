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

import org.openmrs.module.billing.web.base.resource.BaseRestInstanceTypeResource;
import org.openmrs.module.billing.web.rest.controller.base.CashierResourceController;
import org.openmrs.module.billing.api.IPaymentModeService;
import org.openmrs.module.billing.api.base.entity.IMetadataDataService;
import org.openmrs.module.billing.api.model.PaymentMode;
import org.openmrs.module.billing.api.model.PaymentModeAttributeType;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;

import java.util.List;

/**
 * REST resource representing a {@link PaymentMode}.
 */
@Resource(name = RestConstants.VERSION_1 + CashierResourceController.BILLING_NAMESPACE + "/paymentMode", supportedClass = PaymentMode.class,
        supportedOpenmrsVersions = {"2.0 - 2.*"})
public class PaymentModeResource extends BaseRestInstanceTypeResource<PaymentMode, PaymentModeAttributeType> {
    @Override
    public PaymentMode newDelegate() {
        return new PaymentMode();
    }

    @Override
    protected PageableResult doGetAll(RequestContext context) {
        return super.doGetAll(context);
    }

    @Override
    public Class<? extends IMetadataDataService<PaymentMode>> getServiceClass() {
        return IPaymentModeService.class;
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        DelegatingResourceDescription description = super.getRepresentationDescription(rep);
        if (!(rep instanceof RefRepresentation)) {
            description.addProperty("sortOrder");
            description.addProperty("isDefault");
        } else if (rep instanceof CustomRepresentation) {
            //For custom representation, must be null
            // - let the user decide which properties should be included in the response
            description = null;
        }

        return description;
    }

    @PropertySetter("attributeTypes")
    public void setAttributeTypes(PaymentMode instance, List<PaymentModeAttributeType> attributeTypes) {
        super.baseSetAttributeTypes(instance, attributeTypes);
    }

    @PropertySetter("isDefault")
    public void setIsDefault(PaymentMode instance, Boolean isDefault) {
        if (isDefault != null) {
            instance.setIsDefault(isDefault);
        }
    }
}
