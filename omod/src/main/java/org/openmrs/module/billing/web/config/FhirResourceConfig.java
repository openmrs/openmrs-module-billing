package org.openmrs.module.billing.web.config;


import org.openmrs.module.billing.web.rest.resource.fhir.ChargeItemDefinitionResourceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import ca.uhn.fhir.context.FhirContext;


@Configuration
public class FhirResourceConfig {

    @Autowired
    @Qualifier("fhirR4")
    private FhirContext fhirContext;

    @Bean
    public ChargeItemDefinitionResourceProvider chargeItemDefinitionResourceProvider() {
        return new ChargeItemDefinitionResourceProvider();
    }
}

