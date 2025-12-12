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
package org.openmrs.module.billing.api.model;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.BaseChangeableOpenmrsMetadata;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Provider;

public class BillableService extends BaseChangeableOpenmrsMetadata {
	
	public static final long serialVersionUID = 0L;
	
	private int billableServiceId;
	
	private String name;
	
	private String shortName;
	
	private Concept concept;
	
	private Concept serviceType;
	
	private Concept serviceCategory;
	
	private List<CashierItemPrice> servicePrices;
	
	private BillableServiceStatus serviceStatus = BillableServiceStatus.ENABLED;
	
	private Provider provider;
	
	private Location location;
	
	public int getBillableServiceId() {
		return billableServiceId;
	}
	
	public void setBillableServiceId(int billableServiceId) {
		this.billableServiceId = billableServiceId;
	}
	
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
	
	public Concept getServiceType() {
		return serviceType;
	}
	
	public void setServiceType(Concept serviceType) {
		this.serviceType = serviceType;
	}
	
	public Concept getServiceCategory() {
		return serviceCategory;
	}
	
	public void setServiceCategory(Concept serviceCategory) {
		this.serviceCategory = serviceCategory;
	}
	
	public BillableServiceStatus getServiceStatus() {
		return serviceStatus;
	}
	
	public void setServiceStatus(BillableServiceStatus serviceStatus) {
		this.serviceStatus = serviceStatus;
	}
	
	@Override
	public Integer getId() {
		return getBillableServiceId();
	}
	
	@Override
	public void setId(Integer integer) {
		setBillableServiceId(integer);
	}
	
	public List<CashierItemPrice> getServicePrices() {
		return servicePrices;
	}
	
	public void setServicePrices(List<CashierItemPrice> servicePrices) {
		this.servicePrices = servicePrices;
	}
	
	public Concept getConcept() {
		return concept;
	}
	
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	public void addServicePrice(CashierItemPrice price) {
		if (price == null) {
			throw new NullPointerException("Service Price must be defined.");
		}
		
		if (this.servicePrices == null) {
			this.servicePrices = new ArrayList<CashierItemPrice>();
		}
		
		this.servicePrices.add(price);
		price.setBillableService(this);
	}
	
	public Provider getProvider() {
		return provider;
	}
	
	public void setProvider(Provider provider) {
		this.provider = provider;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
}
