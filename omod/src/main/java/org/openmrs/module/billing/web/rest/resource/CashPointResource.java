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

import org.openmrs.module.billing.web.base.resource.BaseRestMetadataResource;
import org.openmrs.module.billing.web.rest.controller.base.CashierResourceController;
import org.openmrs.module.billing.api.ICashPointService;
import org.openmrs.module.billing.api.base.entity.IMetadataDataService;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;

/**
 * REST resource representing a {@link CashPoint}.
 */
@Resource(name = RestConstants.VERSION_1 + CashierResourceController.BILLING_NAMESPACE + "/cashPoint", supportedClass = CashPoint.class,
        supportedOpenmrsVersions = {"2.0 - 2.*"})
public class CashPointResource extends BaseRestMetadataResource<CashPoint> {
    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        DelegatingResourceDescription description = super.getRepresentationDescription(rep);
        description.addProperty("location", Representation.REF);
        description.addProperty("isDefault");
        return description;
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() {
        DelegatingResourceDescription description = super.getCreatableProperties();
        description.addProperty("location");
        description.addProperty("isDefault");
        return description;
    }

    @Override
    public CashPoint newDelegate() {
        return new CashPoint();
    }

    @Override
    public Class<? extends IMetadataDataService<CashPoint>> getServiceClass() {
        return ICashPointService.class;
    }

    @PropertySetter("isDefault")
    public void setIsDefault(CashPoint instance, Boolean isDefault) {
        if (isDefault != null) {
            instance.setIsDefault(isDefault);
        }
    }
}
