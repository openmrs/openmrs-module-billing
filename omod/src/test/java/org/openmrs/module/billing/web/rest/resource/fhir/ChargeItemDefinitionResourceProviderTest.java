package org.openmrs.module.billing.web.rest.resource.fhir;

import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.r4.model.ChargeItemDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.module.billing.api.ChargeItemDefinitionService;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChargeItemDefinitionResourceProviderTest {

    @Mock
    private ChargeItemDefinitionService chargeItemDefinitionService;

    @InjectMocks
    private ChargeItemDefinitionResourceProvider resourceProvider;

    private CashierItemPrice cashierItemPrice;

    @BeforeEach
    public void setUp() {
        cashierItemPrice = new CashierItemPrice();
        cashierItemPrice.setId(1);
        cashierItemPrice.setName("Test Item");
    }

    @Test
    public void testSearchByName() {
        when(chargeItemDefinitionService.getServicePriceByName("Test Item"))
                .thenReturn(Collections.singletonList(cashierItemPrice));

        List<ChargeItemDefinition> result = resourceProvider.searchByName(new TokenParam("name", "Test Item"));

        assertEquals(1, result.size());
        assertEquals("Test Item", result.get(0).getName());
        verify(chargeItemDefinitionService, times(1)).getServicePriceByName("Test Item");
    }

    @Test
    public void testSearchByNameEmpty() {
        when(chargeItemDefinitionService.getServicePriceByName("Nonexistent Item"))
                .thenReturn(Collections.emptyList());

        List<ChargeItemDefinition> result = resourceProvider.searchByName(new TokenParam("name", "Nonexistent Item"));

        assertEquals(0, result.size());
        verify(chargeItemDefinitionService, times(1)).getServicePriceByName("Nonexistent Item");
    }

    @Test
    public void testSearchByCode() {
        when(chargeItemDefinitionService.getChargeItemDefinitionByCode("12345"))
                .thenReturn(cashierItemPrice);

        List<ChargeItemDefinition> result = resourceProvider.searchByCode(new TokenParam("code", "12345"));

        assertEquals(1, result.size());
        assertEquals("Test Item", result.get(0).getName());
        verify(chargeItemDefinitionService, times(1)).getChargeItemDefinitionByCode("12345");
    }

    @Test
    public void testSearchByCodeEmpty() {
        when(chargeItemDefinitionService.getChargeItemDefinitionByCode("67890"))
                .thenReturn(null);

        List<ChargeItemDefinition> result = resourceProvider.searchByCode(new TokenParam("code", "67890"));

        assertEquals(0, result.size());
        verify(chargeItemDefinitionService, times(1)).getChargeItemDefinitionByCode("67890");
    }
}