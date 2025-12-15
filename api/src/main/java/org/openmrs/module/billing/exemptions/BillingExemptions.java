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
package org.openmrs.module.billing.exemptions;

import java.util.Map;
import java.util.Set;

/**
 * An object with details of services and commodities exempted from billing. The class variables
 * should be populated once on startup and should live in memory during application use. If this
 * list grows big, we should think of a separate way to keep this.
 */
public abstract class BillingExemptions {
	
	/**
	 * Should contain a list of unique concept ids which are exempted from billing. The convention is to
	 * have keys that map to a set of concept ids. This is not prescriptive and should be up to an
	 * implementation to define which convention works A sample payload can look like the below: {
	 * "services": { "all": [111,112,113], "program:HIV": [211,212,213], "program:TB": [220,220,220],
	 * "age<5": [311,312,313], "visitAttribute:prisoner": [711,712,713] }, "commodities": { "all":
	 * [511,512,513], "program:HIV": [611,612,613] } } Please note that the key can be anything, as long
	 * as the implementation takes care of the evaluation logic There should be a separate logic to
	 * populate the services and commodities and a separate one to check for exemptions and bill
	 * appropriately TODO: make the implementation as generic as possible
	 */
	public static Map<String, Set<Integer>> SERVICES;
	
	public static Map<String, Set<Integer>> COMMODITIES;
	
	public abstract void buildBillingExemptionList();
	
	public static void setSERVICES(Map<String, Set<Integer>> SERVICES) {
		BillingExemptions.SERVICES = SERVICES;
	}
	
	public static void setCOMMODITIES(Map<String, Set<Integer>> COMMODITIES) {
		BillingExemptions.COMMODITIES = COMMODITIES;
	}
}
