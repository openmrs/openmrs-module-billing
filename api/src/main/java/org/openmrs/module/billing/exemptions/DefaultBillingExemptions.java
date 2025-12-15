package org.openmrs.module.billing.exemptions;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;

public class DefaultBillingExemptions extends BillingExemptions {
	
	private static final Log LOG = LogFactory.getLog(DefaultBillingExemptions.class);
	
	private static final String CONFIG_FILE_PATH = "/billing/exemptions/SampleBillingExemptions.json";
	
	@Override
	public void buildBillingExemptionList() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			BillingExemptionsConfig config = mapper.readValue(new File(CONFIG_FILE_PATH), BillingExemptionsConfig.class);
			
			setSERVICES(config.getServices());
			setCOMMODITIES(config.getCommodities());
			
		}
		catch (IOException e) {
			LOG.error("Failed to load billing exemptions from " + CONFIG_FILE_PATH + ": " + e.getMessage());
			throw new RuntimeException("Unable to load billing exemptions", e);
		}
	}
}
