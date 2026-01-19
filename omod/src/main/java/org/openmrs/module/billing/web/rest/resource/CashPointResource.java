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

import org.apache.commons.lang3.BooleanUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.web.rest.controller.base.CashierResourceController;
import org.openmrs.module.billing.api.CashPointService;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * REST resource representing a {@link CashPoint}.
 */
@Resource(name = RestConstants.VERSION_1 + CashierResourceController.BILLING_NAMESPACE + "/cashPoint", supportedClass = CashPoint.class,
        supportedOpenmrsVersions = {"2.7.8 - 9.*"})
public class CashPointResource extends MetadataDelegatingCrudResource<CashPoint> {

    private final CashPointService cashPointService = Context.getService(CashPointService.class);
    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        DelegatingResourceDescription description = super.getRepresentationDescription(rep);
        description.addProperty("location", Representation.REF);
        return description;
    }

    @Override
    public CashPoint getByUniqueId(String uuid) {
        return cashPointService.getCashPointByUuid(uuid);
    }

    @Override
    public SimpleObject getAll(RequestContext context) throws ResponseException {
        SimpleObject results = new SimpleObject();
        boolean includeRetired = BooleanUtils.toBoolean(context.getParameter("includeAll"));
        results.put("results", cashPointService.getAllCashPoints(includeRetired));
        return results;
    }

    @Override
    public void purge(CashPoint cashPoint, RequestContext requestContext) throws ResponseException {
        cashPointService.purgeCashPoint(cashPoint);
    }

    @Override
    public CashPoint save(CashPoint cashPoint) {
        return cashPointService.saveCashPoint(cashPoint);
    }

    @Override
    public void delete(CashPoint cashpoint, String reason, RequestContext context) throws ResponseException {
        cashPointService.retireCashPoint(cashpoint, reason);
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() {
        DelegatingResourceDescription description = super.getCreatableProperties();
        description.addProperty("location");
        return description;
    }

    @Override
    public CashPoint newDelegate() {
        return new CashPoint();
    }


}
