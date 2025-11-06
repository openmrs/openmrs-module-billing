package org.openmrs.module.billing.api.model;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.HashSet;

import org.junit.Test;

/**
 * Test for verifying Bill model methods, particularly getTotalPayments()
 */
public class BillTest {
	
	@Test
	public void getTotalPayments_shouldExcludeVoidedPaymentsFromTotal() {
		Bill bill = new Bill();
		bill.setPayments(new HashSet<Payment>());
		
		Payment validPayment1 = new Payment();
		validPayment1.setAmountTendered(BigDecimal.valueOf(50));
		validPayment1.setVoided(false);
		bill.getPayments().add(validPayment1);
		
		Payment validPayment2 = new Payment();
		validPayment2.setAmountTendered(BigDecimal.valueOf(30));
		validPayment2.setVoided(false);
		bill.getPayments().add(validPayment2);
		
		Payment voidedPayment1 = new Payment();
		voidedPayment1.setAmountTendered(BigDecimal.valueOf(20));
		voidedPayment1.setVoided(true);
		bill.getPayments().add(voidedPayment1);
		
		Payment voidedPayment2 = new Payment();
		voidedPayment2.setAmountTendered(BigDecimal.valueOf(40));
		voidedPayment2.setVoided(true);
		bill.getPayments().add(voidedPayment2);
		
		assertEquals(BigDecimal.valueOf(80), bill.getTotalPayments());
	}
	
	@Test
	public void getTotalPayments_shouldReturnZeroWhenAllPaymentsAreVoided() {
		Bill bill = new Bill();
		bill.setPayments(new HashSet<Payment>());
		
		// Add only voided payments
		Payment voidedPayment = new Payment();
		voidedPayment.setAmountTendered(BigDecimal.valueOf(100));
		voidedPayment.setVoided(true);
		bill.getPayments().add(voidedPayment);
		
		assertEquals(BigDecimal.ZERO, bill.getTotalPayments());
	}
	

}
