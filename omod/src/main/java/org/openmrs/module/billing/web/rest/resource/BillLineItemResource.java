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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillableServiceService;
import org.openmrs.module.billing.api.BillLineItemService;
import org.openmrs.module.billing.web.base.resource.BaseRestDataResource;
import org.openmrs.module.billing.web.rest.controller.base.CashierResourceController;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.openmrs.module.billing.api.base.entity.IEntityDataService;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.stockmanagement.api.StockManagementService;
import org.openmrs.module.stockmanagement.api.model.StockItem;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;

import java.math.BigDecimal;

/**
 * REST resource representing a {@link BillLineItem}.
 */
@Resource(name = RestConstants.VERSION_1 + CashierResourceController.BILLING_NAMESPACE
        + "/billLineItem", supportedClass = BillLineItem.class, supportedOpenmrsVersions = { "2.0 - 2.*" })
@Slf4j
public class BillLineItemResource extends BaseRestDataResource<BillLineItem> {
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = super.getRepresentationDescription(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			description.addProperty("item");
			description.addProperty("billableService", Representation.REF);
			description.addProperty("quantity");
			description.addProperty("price");
			description.addProperty("priceName");
			description.addProperty("priceUuid");
			description.addProperty("lineItemOrder");
			description.addProperty("paymentStatus");
			return description;
		}
		return null;
	}
	
	@PropertySetter(value = "item")
	public void setItem(BillLineItem instance, Object item) {
		StockManagementService service = Context.getService(StockManagementService.class);
		String itemUuid = (String) item;
		instance.setItem(service.getStockItemByUuid(itemUuid));
	}
	
	@PropertySetter(value = "billableService")
	public void setBillableService(BillLineItem instance, Object item) {
		BillableServiceService service = Context.getService(BillableServiceService.class);
		String serviceUuid = (String) item;
		instance.setBillableService(service.getBillableServiceByUuid(serviceUuid));
	}
	
	@PropertyGetter(value = "item")
	public String getItem(BillLineItem instance) {
		try {
			StockItem stockItem = instance.getItem();
			return stockItem.getDrug().getName();
		}
		catch (Exception e) {
			return "";
		}
	}
	
	@PropertyGetter(value = "billableService")
	public String getBillableService(BillLineItem instance) {
		try {
			BillableService service = instance.getBillableService();
			return service.getName();
		}
		catch (Exception e) {
			return "";
		}
	}
	
	@PropertySetter(value = "price")
	public void setPriceValue(BillLineItem instance, Object price) {
		if (price instanceof Double || price instanceof Integer) {
			double priceValue = ((Number) price).doubleValue();
			instance.setPrice(BigDecimal.valueOf(priceValue));
		} else {
			throw new IllegalArgumentException("Unsupported price type: " + price.getClass().getName());
		}
	}
	
	@PropertySetter(value = "priceName")
	public void setPriceName(BillLineItem instance, String name) {
		instance.setPriceName(name);
	}
	
	@PropertyGetter(value = "priceName")
	public String getPriceName(BillLineItem instance) {
		String itemName = instance.getPriceName();
		return StringUtils.isNotBlank(itemName) ? itemName : "";
	}
	
	@PropertySetter(value = "priceUuid")
	public void setItemPrice(BillLineItem instance, String uuid) {
		StockManagementService itemDataService = Context.getService(StockManagementService.class);
		CashierItemPrice itemPrice = null;
		if (itemPrice != null) {
			instance.setItemPrice(itemPrice);
			instance.setPriceName("");
		}
	}
	
	@PropertyGetter(value = "priceUuid")
	public String getItemPriceUuid(BillLineItem instance) {
		try {
			CashierItemPrice itemPrice = instance.getItemPrice();
			return "";
		}
		catch (Exception e) {
			log.warn("Price probably was deleted", e);
			return "";
		}
	}
	
	@Override
	public BillLineItem getByUniqueId(String uuid) {
		if (StringUtils.isEmpty(uuid)) {
			return null;
		}
		
		return Context.getService(BillLineItemService.class).getBillLineItemByUuid(uuid);
	}
	
	@Override
	public BillLineItem newDelegate() {
		return new BillLineItem();
	}
	
	@Override
	public Class<IEntityDataService<BillLineItem>> getServiceClass() {
		// BillLineItemService doesn't implement IEntityDataService, so return null
		// Line items are managed through BillService, not directly
		return null;
	}
	
	@Override
	protected void delete(BillLineItem delegate, String reason, RequestContext context) {
		if (delegate == null) {
			throw new IllegalArgumentException("The line item to void cannot be null.");
		}
		if (StringUtils.isEmpty(reason)) {
			throw new IllegalArgumentException("The reason to void must be defined.");
		}
		
		BillLineItemService billLineItemService = Context.getService(BillLineItemService.class);
		billLineItemService.voidBillLineItem(delegate.getUuid(), reason);
	}
}
