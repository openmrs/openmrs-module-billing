package org.openmrs.module.billing.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.powermock.modules.junit4.PowerMockRunner;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class ChargeItemDefinitionServiceTest {
	
	@Mock
	private ChargeItemDefinitionService chargeItemDefinitionService;
	
	@InjectMocks
	private ChargeItemDefinitionServiceTest chargeItemDefinitionServiceTest;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testGetChargeItemDefinition() {
		BillableService billableService = new BillableService();
		CashierItemPrice price1 = new CashierItemPrice();
		CashierItemPrice price2 = new CashierItemPrice();
		List<CashierItemPrice> expectedPrices = Arrays.asList(price1, price2);
		
		when(chargeItemDefinitionService.getChargeItemDefinition(billableService)).thenReturn(expectedPrices);
		
		List<CashierItemPrice> actualPrices = chargeItemDefinitionService.getChargeItemDefinition(billableService);
		
		assertEquals(expectedPrices, actualPrices);
		verify(chargeItemDefinitionService, times(1)).getChargeItemDefinition(billableService);
	}
	
	@Test
	public void testSaveChargeItemDefinition() {
		CashierItemPrice cashierItemPrice = new CashierItemPrice();
		when(chargeItemDefinitionService.saveChargeItemDefinition(cashierItemPrice)).thenReturn(cashierItemPrice);
		
		CashierItemPrice savedPrice = chargeItemDefinitionService.saveChargeItemDefinition(cashierItemPrice);
		
		assertEquals(cashierItemPrice, savedPrice);
		verify(chargeItemDefinitionService, times(1)).saveChargeItemDefinition(cashierItemPrice);
	}
	
	@Test
	public void testGetChargeItemDefinitionByCode() {
		String code = "testCode";
		CashierItemPrice expectedPrice = new CashierItemPrice();
		when(chargeItemDefinitionService.getChargeItemDefinitionByCode(code)).thenReturn(expectedPrice);
		
		CashierItemPrice actualPrice = chargeItemDefinitionService.getChargeItemDefinitionByCode(code);
		
		assertEquals(expectedPrice, actualPrice);
		verify(chargeItemDefinitionService, times(1)).getChargeItemDefinitionByCode(code);
	}
}
