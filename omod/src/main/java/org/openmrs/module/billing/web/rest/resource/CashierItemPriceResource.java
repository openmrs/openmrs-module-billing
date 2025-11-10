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
import org.openmrs.module.billing.api.base.entity.IMetadataDataService;
import org.openmrs.module.billing.web.base.resource.BaseRestMetadataResource;
import org.openmrs.module.billing.web.rest.controller.base.CashierResourceController;
import org.openmrs.module.billing.api.ICashierItemPriceService;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.openmrs.module.stockmanagement.api.StockManagementService;
import org.openmrs.module.stockmanagement.api.model.StockItem;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;

import java.math.BigDecimal;

@Resource(name = RestConstants.VERSION_1 + CashierResourceController.BILLING_NAMESPACE + "/cashierItemPrice", supportedClass = CashierItemPrice.class,
        supportedOpenmrsVersions = {"2.0 - 2.*"})
public class CashierItemPriceResource extends BaseRestMetadataResource<CashierItemPrice> {
    @Override
    public CashierItemPrice newDelegate() {
        return new CashierItemPrice();
    }

    @Override
    public Class<? extends IMetadataDataService<CashierItemPrice>> getServiceClass() {
        return ICashierItemPriceService.class;
    }

    @Override
    public CashierItemPrice getByUniqueId(String uuid) {
        return getService().getByUuid(uuid);
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        DelegatingResourceDescription description = super.getRepresentationDescription(rep);
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            description.addProperty("name");
            description.addProperty("price");
            description.addProperty("paymentMode");
            description.addProperty("item");
            description.addProperty("billableService", Representation.REF);
        } else if (rep instanceof CustomRepresentation) {
            //For custom representation, must be null
            // - let the user decide which properties should be included in the response
            description = null;
        }
        return description;
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("name");
        description.addProperty("price");
        description.addProperty("paymentMode");
        description.addProperty("item");
        description.addProperty("billableService");
        return description;
    }

    @Override
    public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
        return getCreatableProperties();
    }

    @PropertySetter("price")
    public void setPrice(CashierItemPrice instance, Object price) {
        double amount;
        if (price instanceof Integer) {
            int rawAmount = (Integer) price;
            amount = Double.valueOf(rawAmount);
            instance.setPrice(BigDecimal.valueOf(amount));
        } else {
            instance.setPrice(BigDecimal.valueOf((Double) price));
        }
    }

    @PropertySetter(value = "item")
    public void setItem(CashierItemPrice instance, Object item) {
        StockManagementService service = Context.getService(StockManagementService.class);
        String itemUuid = (String) item;
        instance.setItem(service.getStockItemByUuid(itemUuid));
    }

    @PropertyGetter(value = "item")
    public String getItem(CashierItemPrice instance) {
        try {
            StockItem stockItem = instance.getItem();
            return stockItem.getDrug().getName();
        } catch (Exception e) {
            log.error(e);
            return "";
        }
    }
}
