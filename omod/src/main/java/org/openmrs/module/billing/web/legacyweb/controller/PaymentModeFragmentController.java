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

import java.util.HashMap;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.PaymentModeService;
import org.openmrs.module.billing.api.model.PaymentMode;
import org.openmrs.module.billing.api.model.PaymentModeAttributeType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller to manage the payment mode fragments.
 */
@Controller
@RequestMapping("/module/billing/paymentModeFragment")
public class PaymentModeFragmentController {
	
	@RequestMapping(method = RequestMethod.GET)
	public void paymentModeFragment(@RequestParam("uuid") String uuid, ModelMap model) {
		PaymentModeService service = Context.getService(PaymentModeService.class);
		PaymentMode paymentMode = service.getPaymentModeByUuid(uuid);
		ConceptService conceptService = Context.getConceptService();
		
		Map<Integer, Concept> conceptMap = new HashMap<Integer, Concept>();
		for (PaymentModeAttributeType type : paymentMode.getAttributeTypes()) {
			if (type.getFormat().equals("org.openmrs.Concept") && type.getForeignKey() != null) {
				conceptMap.put(type.getForeignKey(), conceptService.getConcept(type.getForeignKey()));
			}
		}
		
		model.addAttribute("paymentMode", paymentMode);
		model.addAttribute("conceptMap", conceptMap);
	}
}
