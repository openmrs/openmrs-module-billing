package org.openmrs.module.billing.api.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.billing.api.ItemPriceService;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.openmrs.module.stockmanagement.api.model.StockItem;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ChargeItemDefinitionServiceImplTest {
	
	@InjectMocks
	private ChargeItemDefinitionServiceImpl chargeItemDefinitionService;
	
	@Mock
	private ItemPriceService itemPriceService;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testGetChargeItemDefinition() {
		BillableService billableService = new BillableService();
		CashierItemPrice cashierItemPrice = new CashierItemPrice();
		List<CashierItemPrice> expectedPrices = Collections.singletonList(cashierItemPrice);
		
		when(itemPriceService.getServicePrice(billableService)).thenReturn(expectedPrices);
		
		List<CashierItemPrice> actualPrices = chargeItemDefinitionService.getChargeItemDefinition(billableService);
		
		assertEquals(expectedPrices, actualPrices);
		verify(itemPriceService, times(1)).getServicePrice(billableService);
	}
	
	@Test
	public void testGetItemPrice() {
		StockItem stockItem = new StockItem();
		
		List<CashierItemPrice> actualPrices = chargeItemDefinitionService.getItemPrice(stockItem);
		
		assertTrue(actualPrices.isEmpty());
	}
	
	@Test
	public void testGetServicePriceByName() {
		String name = "Test Service";
		
		List<CashierItemPrice> actualPrices = chargeItemDefinitionService.getServicePriceByName(name);
		
		assertTrue(actualPrices.isEmpty());
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testGetPrivileges() {
		chargeItemDefinitionService.getPrivileges();
	}
	
	@Test
	public void testSaveChargeItemDefinition() {
		CashierItemPrice cashierItemPrice = new CashierItemPrice();
		
		when(itemPriceService.save(cashierItemPrice)).thenReturn(cashierItemPrice);
		
		CashierItemPrice savedPrice = chargeItemDefinitionService.saveChargeItemDefinition(cashierItemPrice);
		
		assertEquals(cashierItemPrice, savedPrice);
		verify(itemPriceService, times(1)).save(cashierItemPrice);
	}
	
	@Test
	public void testGetServicePrice() {
		BillableService billableService = new BillableService();
		CashierItemPrice cashierItemPrice = new CashierItemPrice();
		List<CashierItemPrice> expectedPrices = Collections.singletonList(cashierItemPrice);
		
		when(itemPriceService.getServicePrice(billableService)).thenReturn(expectedPrices);
		
		List<CashierItemPrice> actualPrices = chargeItemDefinitionService.getServicePrice(billableService);
		
		assertEquals(expectedPrices, actualPrices);
		verify(itemPriceService, times(1)).getServicePrice(billableService);
	}
	
	@Test
	public void testGetById() {
		int id = 1;
		CashierItemPrice expectedPrice = new CashierItemPrice();
		
		when(itemPriceService.getById(id)).thenReturn(expectedPrice);
		
		CashierItemPrice actualPrice = chargeItemDefinitionService.getById(id);
		
		assertEquals(expectedPrice, actualPrice);
		verify(itemPriceService, times(1)).getById(id);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testValidate_NullCashierItemPrice() {
		chargeItemDefinitionService.validate(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testValidate_NullPrice() {
		CashierItemPrice cashierItemPrice = new CashierItemPrice();
		chargeItemDefinitionService.validate(cashierItemPrice);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testValidate_ZeroPrice() {
		CashierItemPrice cashierItemPrice = new CashierItemPrice();
		cashierItemPrice.setPrice(BigDecimal.ZERO);
		chargeItemDefinitionService.validate(cashierItemPrice);
	}
	
	@Test
	public void testValidate_ValidPrice() {
		CashierItemPrice cashierItemPrice = new CashierItemPrice();
		cashierItemPrice.setPrice(BigDecimal.ONE);
		chargeItemDefinitionService.validate(cashierItemPrice);
	}
}
