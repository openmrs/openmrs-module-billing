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
package org.openmrs.module.billing;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.billing.web.CashierWebConstants;
import org.openmrs.module.web.WebModuleUtil;

/**
 * This class contains the logic that is run every time this module is either started or stopped.
 */
@Slf4j
public class BillingModuleActivator extends BaseModuleActivator {
	
	/**
	 * @see BaseModuleActivator#contextRefreshed()
	 */
	@Override
	public void contextRefreshed() {
		log.info("OpenMRS Billing Module refreshed");
	}
	
	/**
	 * @see BaseModuleActivator#started()
	 */
	@Override
	public void started() {
		log.info("OpenMRS Billing Module started");
	}
	
	/**
	 * @see BaseModuleActivator#stopped()
	 */
	@Override
	public void stopped() {
		Module module = ModuleFactory.getModuleById(CashierWebConstants.OPENHMIS_CASHIER_MODULE_ID);
		WebModuleUtil.unloadFilters(module);
		
		log.info("OpenMRS Billing Module stopped");
	}
}
