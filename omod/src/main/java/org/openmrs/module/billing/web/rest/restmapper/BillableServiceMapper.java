/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.web.rest.restmapper;

import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.PaymentModeService;
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
			price.setPaymentMode(
			    Context.getService(PaymentModeService.class).getPaymentModeByUuid(itemPrice.getPaymentMode()));
			price.setBillableService(service);
			servicePrices.add(price);
		}
		service.setServicePrices(servicePrices);
		
		return service;
	}
}
