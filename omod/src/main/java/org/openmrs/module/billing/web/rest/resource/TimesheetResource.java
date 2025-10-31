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

import org.openmrs.module.billing.web.base.resource.BaseRestDataResource;
import org.openmrs.module.billing.web.rest.controller.base.CashierResourceController;
import org.openmrs.module.billing.api.ITimesheetService;
import org.openmrs.module.billing.api.base.entity.IEntityDataService;
import org.openmrs.module.billing.api.model.Timesheet;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.util.LocaleUtility;

import java.text.DateFormat;

/**
 * REST resource representing a {@link Timesheet}.
 */
@Resource(name = RestConstants.VERSION_2 + CashierResourceController.BILLING_NAMESPACE
        + "/timesheet", supportedClass = Timesheet.class, supportedOpenmrsVersions = { "2.0 - 2.*" })
public class TimesheetResource extends BaseRestDataResource<Timesheet> {
	
	@Override
	public Timesheet newDelegate() {
		return new Timesheet();
	}
	
	@Override
	public Class<? extends IEntityDataService<Timesheet>> getServiceClass() {
		return ITimesheetService.class;
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = super.getRepresentationDescription(rep);
		description.addProperty("cashier", Representation.REF);
		description.addProperty("cashPoint", Representation.REF);
		description.addProperty("clockIn");
		description.addProperty("clockOut");
		if (rep instanceof RefRepresentation) {
			description.addProperty("id");
			description.addProperty("uuid");
		}
		
		return description;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = super.getCreatableProperties();
		description.addProperty("cashier");
		description.addProperty("cashpoint");
		return description;
	}
	
	public String getDisplayString(Timesheet instance) {
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT,
		    LocaleUtility.getDefaultLocale());
		return dateFormat.format(instance.getClockIn()) + " to "
		        + (instance.getClockOut() != null ? dateFormat.format(instance.getClockOut()) : " open");
	}
}
