/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.Privilege;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;

/**
 * Constants class for module privilege constants.
 */
@Slf4j
public class PrivilegeConstants {
	
	public static final String MANAGE_BILLS = "Manage Cashier Bills";
	
	public static final String ADJUST_BILLS = "Adjust Cashier Bills";
	
	public static final String VIEW_BILLS = "View Cashier Bills";
	
	public static final String PURGE_BILLS = "Purge Cashier Bills";
	
	public static final String DELETE_BILLS = "Delete Cashier Bills";
	
	public static final String REFUND_MONEY = "Refund Money";
	
	public static final String REPRINT_RECEIPT = "Reprint Receipt";
	
	public static final String MANAGE_BILL_DISCOUNTS = "Manage Bill Discounts";

	public static final String APPROVE_BILL_DISCOUNTS = "Approve Bill Discounts";

	public static final String VIEW_BILL_DISCOUNTS = "View Bill Discounts";
	
	public static final String MANAGE_METADATA = "Manage Cashier Metadata";
	
	public static final String VIEW_METADATA = "View Cashier Metadata";
	
	public static final String PURGE_METADATA = "Purge Cashier Metadata";
	
	public static final String MANAGE_TIMESHEETS = "Manage Cashier Timesheets";
	
	public static final String VIEW_TIMESHEETS = "View Cashier Timesheets";
	
	public static final String PURGE_TIMESHEETS = "Purge Cashier Timesheets";
	
	public static final String APP_VIEW_CASHIER_APP = "App: View Cashier App";
	
	public static final String APP_ACCESS_CASHIER_TASKS_PAGE = "App: Access Cashier Tasks";
	
	public static final String TASK_CREATE_NEW_BILL_PAGE = "Task: Create new bill";
	
	public static final String TASK_ADJUST_CASHIER_BILL = "Task: Adjust Cashier Bills";
	
	public static final String TASK_CASHIER_TIMESHEETS_PAGE = "Task: Cashier Timesheets";
	
	public static final String TASK_MANAGE_CASHIER_MODULE_PAGE = "Task: Manage Cashier Module";
	
	public static final String TASK_MANAGE_CASHIER_METADATA = "Task: Manage Cashier Metadata";
	
	public static final String TASK_VIEW_CASHIER_REPORTS = "Task: View Cashier Reports";
	
	public static final String[] PRIVILEGE_NAMES = new String[] { MANAGE_BILLS, ADJUST_BILLS, VIEW_BILLS, PURGE_BILLS,
	        REFUND_MONEY, REPRINT_RECEIPT, MANAGE_BILL_DISCOUNTS, APPROVE_BILL_DISCOUNTS, VIEW_BILL_DISCOUNTS,
	        MANAGE_TIMESHEETS, VIEW_TIMESHEETS, PURGE_TIMESHEETS, MANAGE_METADATA, VIEW_METADATA, PURGE_METADATA,
	        APP_VIEW_CASHIER_APP, TASK_CREATE_NEW_BILL_PAGE, TASK_ADJUST_CASHIER_BILL, TASK_CASHIER_TIMESHEETS_PAGE,
	        TASK_MANAGE_CASHIER_MODULE_PAGE, TASK_MANAGE_CASHIER_METADATA, TASK_CASHIER_TIMESHEETS_PAGE,
	        TASK_MANAGE_CASHIER_MODULE_PAGE, TASK_VIEW_CASHIER_REPORTS, APP_ACCESS_CASHIER_TASKS_PAGE };
	
	/**
	 * Gets all the privileges defined by the module.
	 *
	 * @return The module privileges.
	 */
	public static Set<Privilege> getModulePrivileges() {
		Set<Privilege> privileges = new HashSet<Privilege>(PRIVILEGE_NAMES.length);
		
		UserService service = Context.getUserService();
		if (service == null) {
			throw new IllegalStateException("The OpenMRS user service cannot be loaded.");
		}
		
		for (String name : PRIVILEGE_NAMES) {
			privileges.add(service.getPrivilege(name));
		}
		
		return privileges;
	}
	
	/**
	 * Gets the default privileges needed to fully use the module.
	 *
	 * @return A set containing the default set of privileges.
	 */
	public static Set<Privilege> getDefaultPrivileges() {
		Set<Privilege> privileges = getModulePrivileges();
		
		UserService service = Context.getUserService();
		if (service == null) {
			throw new IllegalStateException("The OpenMRS user service cannot be loaded.");
		}
		
		List<String> names = new ArrayList<String>();
		// Add other required cashier privileges
		names.add("View Inventory Items");
		names.add("View Inventory Metadata");
		
		names.add(org.openmrs.util.PrivilegeConstants.ADD_ENCOUNTERS);
		names.add(org.openmrs.util.PrivilegeConstants.ADD_VISITS);
		names.add(org.openmrs.util.PrivilegeConstants.EDIT_ENCOUNTERS);
		names.add(org.openmrs.util.PrivilegeConstants.EDIT_PATIENTS);
		names.add(org.openmrs.util.PrivilegeConstants.EDIT_VISITS);
		names.add(org.openmrs.util.PrivilegeConstants.VIEW_ADMIN_FUNCTIONS);
		names.add(org.openmrs.util.PrivilegeConstants.GET_CONCEPTS);
		names.add(org.openmrs.util.PrivilegeConstants.GET_ENCOUNTERS);
		names.add(org.openmrs.util.PrivilegeConstants.VIEW_NAVIGATION_MENU);
		names.add(org.openmrs.util.PrivilegeConstants.GET_OBS);
		names.add(org.openmrs.util.PrivilegeConstants.GET_PATIENTS);
		names.add(org.openmrs.util.PrivilegeConstants.GET_PROVIDERS);
		names.add(org.openmrs.util.PrivilegeConstants.GET_VISITS);
		
		for (String name : names) {
			Privilege privilege = service.getPrivilege(name);
			if (privilege != null) {
				privileges.add(privilege);
			} else {
				log.debug("------------NULL PRIVILEGE: {}", name);
			}
		}
		
		return privileges;
		
	}
}
