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
package org.openmrs.module.billing.web.rest.restmapper;

import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.IPaymentModeService;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.BillableServiceStatus;
import org.openmrs.module.billing.api.model.CashierItemPrice;

import java.util.ArrayList;
import java.util.List;

public class BillableServiceMapper {
	
	private String name;
	
	private String shortName;
	
	private String concept;
	
	private String serviceType;
	
	private String serviceCategory;
	
	private List<CashierItemPriceMapper> servicePrices;
	
	private BillableServiceStatus serviceStatus = BillableServiceStatus.ENABLED;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getShortName() {
		return shortName;
	}
	
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	
	public String getServiceType() {
		return serviceType;
	}
	
	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}
	
	public String getServiceCategory() {
		return serviceCategory;
	}
	
	public void setServiceCategory(String serviceCategory) {
		this.serviceCategory = serviceCategory;
	}
	
	public List<CashierItemPriceMapper> getServicePrices() {
		return servicePrices;
	}
	
	public void setServicePrices(List<CashierItemPriceMapper> servicePrices) {
		this.servicePrices = servicePrices;
	}
	
	public BillableServiceStatus getServiceStatus() {
		return serviceStatus;
	}
	
	public void setServiceStatus(BillableServiceStatus serviceStatus) {
		this.serviceStatus = serviceStatus;
	}
	
	public String getConcept() {
		return concept;
	}
	
	public void setConcept(String concept) {
		this.concept = concept;
	}
	
	public BillableService billableServiceMapper(BillableServiceMapper mapper) {
		BillableService service = new BillableService();
		List<CashierItemPrice> servicePrices = new ArrayList<>();
		service.setName(mapper.getName());
		service.setShortName(mapper.getShortName());
		service.setConcept(Context.getConceptService().getConceptByUuid(mapper.getConcept()));
		service.setServiceType(Context.getConceptService().getConceptByUuid(mapper.getServiceType()));
		service.setServiceCategory(Context.getConceptService().getConceptByUuid(mapper.getServiceCategory()));
		service.setServiceStatus(mapper.getServiceStatus());
		for (CashierItemPriceMapper itemPrice : mapper.getServicePrices()) {
			CashierItemPrice price = new CashierItemPrice();
			price.setName(itemPrice.getName());
			price.setPrice(itemPrice.getPrice());
			price.setPaymentMode(Context.getService(IPaymentModeService.class).getByUuid(itemPrice.getPaymentMode()));
			price.setBillableService(service);
			servicePrices.add(price);
		}
		service.setServicePrices(servicePrices);
		
		return service;
	}
}
