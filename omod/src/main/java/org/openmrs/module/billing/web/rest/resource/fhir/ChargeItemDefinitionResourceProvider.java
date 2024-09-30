package org.openmrs.module.billing.web.rest.resource.fhir;

import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
import org.openmrs.module.billing.api.ChargeItemDefinitionService;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.ChargeItemDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import org.hl7.fhir.r4.model.IdType;

@Component
@R4Provider
public class ChargeItemDefinitionResourceProvider implements IResourceProvider {

    @Autowired
    private ChargeItemDefinitionService chargeItemDefinitionService;

    @Override
    public Class<ChargeItemDefinition> getResourceType() {
        return ChargeItemDefinition.class;
    }

    public ChargeItemDefinitionResourceProvider() {
    }

    @Search
    public List<ChargeItemDefinition> searchByName(@RequiredParam(name = "name") TokenParam nameParam) {
        List<CashierItemPrice> servicePrices = chargeItemDefinitionService.getServicePriceByName(nameParam.getValue());
    
        if (servicePrices == null || servicePrices.isEmpty()) {
            return Collections.emptyList();
        }
    
        return servicePrices.stream()
                .map(this::mapToChargeItemDefinition)
                .collect(Collectors.toList());
    }

    @Search
    public List<ChargeItemDefinition> searchByCode(@RequiredParam(name = "code") TokenParam codeParam) {
        String code = codeParam.getValue();
        CashierItemPrice price = chargeItemDefinitionService.getChargeItemDefinitionByCode(code);

        if (price == null) {
            return Collections.emptyList();
        }

        return Collections.singletonList(mapToChargeItemDefinition(price));
    }

    @Create
    public MethodOutcome createChargeItemDefinition(@ResourceParam ChargeItemDefinition chargeItemDefinition) {

        return null;
    }

    private ChargeItemDefinition mapToChargeItemDefinition(CashierItemPrice cashierItemPrice) {
        ChargeItemDefinition chargeItemDefinition = new ChargeItemDefinition();
        chargeItemDefinition.setId(new IdType("ChargeItemDefinition/" + cashierItemPrice.getId()));
    
        // Add any additional mapping here for name, price, etc.
        chargeItemDefinition.setName(cashierItemPrice.getName());
    
        return chargeItemDefinition;
    }

    private CashierItemPrice mapFromChargeItemDefinition(ChargeItemDefinition chargeItemDefinition) {
        CashierItemPrice cashierItemPrice = new CashierItemPrice();

        // Example of mapping logic - uncomment and adapt as needed
        // cashierItemPrice.setPrice(chargeItemDefinition.getPropertyGroupFirstRep()
        //     .getPriceComponentFirstRep().getAmount().getValue());

        // Coding coding = chargeItemDefinition.getCode().getCodingFirstRep();
        // BillableService billableService = new BillableService();
        // billableService.setName(coding.getCode());
        // cashierItemPrice.setBillableService(billableService);

        return cashierItemPrice;
    }
}
