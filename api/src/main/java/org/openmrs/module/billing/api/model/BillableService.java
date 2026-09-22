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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.openmrs.BaseChangeableOpenmrsMetadata;
import org.openmrs.Concept;

@Getter
@Setter
public class BillableService extends BaseChangeableOpenmrsMetadata {
	
	private static final long serialVersionUID = 0L;
	
	@Setter(AccessLevel.NONE)
	@Getter(AccessLevel.NONE)
	private Integer billableServiceId;
	
	private String name;
	
	private String shortName;
	
	private Concept concept;
	
	private Concept serviceType;
	
	private Concept serviceCategory;
	
	private List<CashierItemPrice> servicePrices;
	
	private BillableServiceStatus serviceStatus = BillableServiceStatus.ENABLED;
	
	@Override
	public Integer getId() {
		return billableServiceId;
	}
	
	@Override
	public void setId(Integer billableServiceId) {
		this.billableServiceId = billableServiceId;
	}

}
