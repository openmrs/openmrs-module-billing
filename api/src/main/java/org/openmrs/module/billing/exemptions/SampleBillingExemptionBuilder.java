package org.openmrs.module.billing.exemptions;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.util.CashierModuleConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/*
 * Builds a list of exemptions from json file
*/
public class SampleBillingExemptionBuilder extends BillingExemptions {
	
	private static final Log LOG = LogFactory.getLog(SampleBillingExemptionBuilder.class);
	
	public SampleBillingExemptionBuilder() {
	}
	
	@Override
	public void buildBillingExemptionList() {
		GlobalProperty gpConfiguredFilePath = Context.getAdministrationService()
		        .getGlobalPropertyObject(CashierModuleConstants.BILLING_EXEMPTIONS_CONFIG_FILE_PATH);
		if (gpConfiguredFilePath == null || StringUtils.isBlank(gpConfiguredFilePath.getPropertyValue())) {
			try {
				initializeExemptionsConfig();
			}
			catch (Exception e) {
				LOG.error("Billing exemptions have not been configured...", e);
			}
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
			try {
				initializeExemptionsConfig();
			}
			catch (Exception ex) {
				LOG.error("The configuration file for billing exemptions was found, but could not be processed", ex);
			}
			return;
		}
		
		if (fileInputStream != null) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				config = mapper.readValue(fileInputStream, ObjectNode.class);
			}
			catch (IOException e) {
				e.printStackTrace();
				try {
					initializeExemptionsConfig();
				}
				catch (Exception ex) {
					LOG.error(
					    "The configuration file for billing exemptions was found, but could not be understood. Check that the JSON object is well formed",
					    ex);
				}
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
	
	private Map<String, Set<Integer>> mapConcepts(ObjectNode node) {
		Map<String, Set<Integer>> exemptionList = new HashMap<>();
		if (node != null) {
			Iterator<Map.Entry<String, JsonNode>> iterator = node.getFields();
			iterator.forEachRemaining(entry -> {
				Set<Integer> conceptSet = new HashSet<>();
				String key = entry.getKey();
				ArrayNode conceptIds = (ArrayNode) entry.getValue();
				if (conceptIds.isArray() && conceptIds.size() > 0) {
					for (int i = 0; i < conceptIds.size(); i++) {
						try {
							conceptSet.add(conceptIds.get(i).getIntValue());
						}
						catch (Exception e) {
							LOG.error("Error converting concept ID to integer: " + conceptIds.get(i).toString(), e);
						}
					}
				}
				if (conceptSet.size() > 0) {
					exemptionList.put(key, conceptSet);
				}
			});
		}
		return exemptionList;
	}
	
	private void initializeExemptionsConfig() {
		BillingExemptions.setCOMMODITIES(new HashMap<>());
		BillingExemptions.setSERVICES(new HashMap<>());
	}
}
