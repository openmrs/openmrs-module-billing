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

import org.apache.logging.log4j.util.Strings;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.web.base.resource.BaseRestDataResource;
import org.openmrs.module.billing.web.rest.controller.base.CashierResourceController;
import org.openmrs.module.billing.api.IBillableItemsService;
import org.openmrs.module.billing.api.base.entity.IEntityDataService;
import org.openmrs.module.billing.api.model.*;
import org.openmrs.module.billing.api.search.BillableServiceSearch;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;

import java.util.ArrayList;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + CashierResourceController.BILLING_NAMESPACE + "/billableService", supportedClass = BillableService.class,
        supportedOpenmrsVersions = {"2.0 - 2.*"})
public class BillableServiceResource extends BaseRestDataResource<BillableService> {

    @Override
    public BillableService newDelegate() {
        return new BillableService();
    }

    @Override
    public Class<? extends IEntityDataService<BillableService>> getServiceClass() {
        return IBillableItemsService.class;
    }

    @Override
    public BillableService getByUniqueId(String uuid) {
        return getService().getByUuid(uuid);
    }

    @Override
    public BillableService save(BillableService delegate) {
        return super.save(delegate);
    }

    @Override
    protected AlreadyPaged<BillableService> doSearch(RequestContext context) {
        Concept serviceType = context.getParameter("serviceType") != null ? Context.getConceptService().getConceptByUuid(
                context.getParameter("serviceType")) : null;
        Concept serviceCategory = context.getParameter("serviceCategory") != null ? Context.getConceptService().getConceptByUuid(
                context.getParameter("serviceCategory")) : null;
        String serviceStatus = context.getParameter("isDisabled");
        String serviceName = context.getParameter("serviceName");
        String locationUuid = context.getParameter("location");
        String providerUuid = context.getParameter("provider");
        BillableServiceStatus status = BillableServiceStatus.ENABLED;
        if (Strings.isNotEmpty(serviceStatus)) {
            if (serviceStatus.equalsIgnoreCase("yes") || serviceStatus.equalsIgnoreCase("1")) {
                status = BillableServiceStatus.DISABLED;
            }
        }
        BillableService searchTemplate = new BillableService();
        searchTemplate.setServiceType(serviceType);
        searchTemplate.setServiceCategory(serviceCategory);
        searchTemplate.setServiceStatus(status);
        searchTemplate.setName(serviceName);

        if (Strings.isNotEmpty(locationUuid)) {
            searchTemplate.setLocation(Context.getLocationService().getLocationByUuid(locationUuid));
        }

        if (Strings.isNotEmpty(providerUuid)) {
            searchTemplate.setProvider(Context.getProviderService().getProviderByUuid(providerUuid));
        }

        IBillableItemsService service = Context.getService(IBillableItemsService.class);
        return new AlreadyPaged<>(context, service.findServices(new BillableServiceSearch(searchTemplate, false)), false);
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        DelegatingResourceDescription description = super.getRepresentationDescription(rep);
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            description.addProperty("name");
            description.addProperty("shortName");
            description.addProperty("concept");
            description.addProperty("serviceType");
            description.addProperty("serviceCategory");
            description.addProperty("servicePrices");
            description.addProperty("serviceStatus");
            description.addProperty("provider");
            description.addProperty("location");
        } else if (rep instanceof CustomRepresentation) {
            //For custom representation, must be null
            // - let the user decide which properties should be included in the response
            description = null;
        }
        return description;
    }

    @PropertyGetter(value = "servicePrices")
    public List<CashierItemPrice> getServicePrices(BillableService instance) {
        return new ArrayList<>(instance.getServicePrices());
    }

    @PropertySetter("servicePrices")
    public void setServicePrices(BillableService instance, List<CashierItemPrice> itemPrices) {
        if (instance.getServicePrices() == null) {
            instance.setServicePrices(new ArrayList<CashierItemPrice>(itemPrices.size()));
        }
        BaseRestDataResource.syncCollection(instance.getServicePrices(), itemPrices);
        for (CashierItemPrice itemPrice : instance.getServicePrices()) {
            itemPrice.setBillableService(instance);
        }
    }

    @PropertySetter("provider")
    public void setProvider(BillableService instance, Object value) {
        if (value != null) {
            String uuid = value.toString();
            instance.setProvider(Context.getProviderService().getProviderByUuid(uuid));
        } else {
            instance.setProvider(null);
        }
    }

    @PropertySetter("location")
    public void setLocation(BillableService instance, Object value) {
        if (value != null) {
            String uuid = value.toString();
            instance.setLocation(Context.getLocationService().getLocationByUuid(uuid));
        } else {
            instance.setLocation(null);
        }
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() {
        return getRepresentationDescription(new DefaultRepresentation());
    }

    @Override
    public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
        return getCreatableProperties();
    }
}
