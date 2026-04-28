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

import org.openmrs.module.billing.web.CashierWebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller to manage the Receipt Number Generation
 */
@Controller
@RequestMapping(value = ReceiptNumberGeneratorController.RECEIPT_NUMBER_GENERATOR_URL)
public class ReceiptNumberGeneratorController extends AbstractReceiptNumberGenerator {
	
	public static final String RECEIPT_NUMBER_GENERATOR_URL = CashierWebConstants.RECEIPT_NUMBER_GENERATOR_ROOT;
	
	@Override
	public String getReceiptNumberGeneratorUrl() {
		return RECEIPT_NUMBER_GENERATOR_URL;
	}
	
}
