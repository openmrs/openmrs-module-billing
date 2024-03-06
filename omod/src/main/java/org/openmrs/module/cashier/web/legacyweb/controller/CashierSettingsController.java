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
package org.openmrs.module.cashier.web.legacyweb.controller;

import org.openmrs.module.cashier.web.CashierWebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller to manage the Cashier Settings page.
 */
@Controller
@RequestMapping(value = CashierWebConstants.CASHIER_SETTINGS_ROOT)
public class CashierSettingsController extends CashierSettingsControllerBase {}