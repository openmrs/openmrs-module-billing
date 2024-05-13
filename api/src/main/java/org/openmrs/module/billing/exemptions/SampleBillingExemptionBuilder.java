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

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.util.CashierModuleConstants;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Builds a list of exemptions from json file
 */
public class SampleBillingExemptionBuilder extends BillingExemptions {
	
	public SampleBillingExemptionBuilder() {
	}
	
	/**
	 * It is good practice to adopt a convention that will make it easy to read the config.
	 */
	@Override
	public void buildBillingExemptionList() {
		GlobalProperty gpConfiguredFilePath = Context.getAdministrationService()
		        .getGlobalPropertyObject(CashierModuleConstants.BILLING_EXEMPTIONS_CONFIG_FILE_PATH);
		if (gpConfiguredFilePath == null || StringUtils.isBlank(gpConfiguredFilePath.getPropertyValue())) {
			// initialize null sets for services and commodities
			initializeExemptionsConfig();
			System.out.println("Billing exemptions have not been configured...");
			return;
		}
		String configurationFilePath = gpConfiguredFilePath.getPropertyValue();
		FileInputStream fileInputStream;
		ObjectNode config = null;
		try {
			fileInputStream = new FileInputStream(configurationFilePath);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			initializeExemptionsConfig();
			System.out.println("The configuration file for billing exemptions was found, but could not be processed");
			return;
		}
		
		if (fileInputStream != null) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				config = mapper.readValue(fileInputStream, ObjectNode.class);
			}
			catch (IOException e) {
				e.printStackTrace();
				initializeExemptionsConfig();
				System.out.println(
				    "The configuration file for billing exemptions was found, but could not be understood. Check that the JSON object is well formed");
				return;
			}
		}
		
		if (config != null) {
			ObjectNode configuredServices = (ObjectNode) config.get("services");
			ObjectNode commodities = (ObjectNode) config.get("commodities");
			
			if (configuredServices != null) {
				Map<String, Set<Integer>> exemptedServices = mapConcepts(configuredServices);
				BillingExemptions.setSERVICES(exemptedServices);
			}
			
			if (commodities != null) {
				Map<String, Set<Integer>> exemptedCommodities = mapConcepts(commodities);
				BillingExemptions.setCOMMODITIES(exemptedCommodities);
			}
		} else {
			initializeExemptionsConfig();
		}
	}
	
	/**
	 * Map exemption list in maps for fast access
	 * 
	 * @param node
	 * @return
	 */
	private Map<String, Set<Integer>> mapConcepts(ObjectNode node) {
		Map<String, Set<Integer>> exemptionList = new HashMap<String, Set<Integer>>();
		if (node != null) {
			Iterator<Map.Entry<String, JsonNode>> iterator = node.getFields();
			iterator.forEachRemaining(entry -> {
				Set<Integer> conceptSet = new HashSet<>();
				String key = entry.getKey();
				ArrayNode conceptIds = (ArrayNode) entry.getValue();
				if (conceptIds.isArray() && conceptIds.size() > 0) {
					for (int i = 0; i < conceptIds.size(); i++) {
						conceptSet.add((conceptIds.get(i).getIntValue()));
					}
				}
				if (conceptSet.size() > 0) {
					exemptionList.put(key, conceptSet);
				}
			});
		}
		return exemptionList;
	}
	
	/**
	 * Setting these to empty sets which are easy to work with than nulls
	 */
	private void initializeExemptionsConfig() {
		BillingExemptions.setCOMMODITIES(new HashMap<>());
		BillingExemptions.setSERVICES(new HashMap<>());
	}
}
