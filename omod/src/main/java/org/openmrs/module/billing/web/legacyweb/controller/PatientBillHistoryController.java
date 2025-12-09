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
