/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.web.legacyweb.controller;

import java.util.List;

import org.apache.log4j.Logger;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.model.Bill;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller to manage the page to display patient's bills history.
 */
@Controller
@RequestMapping(value = "/module/billing/portlets/patientBillHistory")
public class PatientBillHistoryController {
	
	private static final Logger LOG = Logger.getLogger(PatientBillHistoryController.class);
	
	@RequestMapping(method = RequestMethod.GET)
	public void billHistory(ModelMap model, @RequestParam(value = "patientUuid") String patientUuid) {
		LOG.warn("In bill history controller");
		List<Bill> bills = Context.getService(BillService.class).getBillsByPatientUuid(patientUuid, null);
		model.addAttribute("bills", bills);
	}
}
