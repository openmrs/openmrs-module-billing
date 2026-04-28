/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.model;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.BaseChangeableOpenmrsMetadata;
import org.openmrs.Concept;

public class BillableService extends BaseChangeableOpenmrsMetadata {
	
	private static final long serialVersionUID = 0L;
	
	private int billableServiceId;
	
	private String name;
	
	private String shortName;
	
	private Concept concept;
	
	private Concept serviceType;
	
	private Concept serviceCategory;
	
	private List<CashierItemPrice> servicePrices;
	
	private BillableServiceStatus serviceStatus = BillableServiceStatus.ENABLED;
	
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
}
