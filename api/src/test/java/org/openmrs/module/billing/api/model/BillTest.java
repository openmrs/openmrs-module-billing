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
		
		// Add valid payment of 50
		Payment validPayment = new Payment();
		validPayment.setAmountTendered(BigDecimal.valueOf(50));
		validPayment.setVoided(false);
		bill.getPayments().add(validPayment);
		
		// Add voided payment of 30 (should be excluded)
		Payment voidedPayment = new Payment();
		voidedPayment.setAmountTendered(BigDecimal.valueOf(30));
		voidedPayment.setVoided(true);
		bill.getPayments().add(voidedPayment);
		
		// Total should be 50, not 80
		assertEquals(BigDecimal.valueOf(50), bill.getTotalPayments());
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
	
	@Test
	public void getTotalPayments_shouldIncludeAllNonVoidedPayments() {
		Bill bill = new Bill();
		bill.setPayments(new HashSet<Payment>());
		
		// Add multiple valid payments
		Payment payment1 = new Payment();
		payment1.setAmountTendered(BigDecimal.valueOf(30));
		payment1.setVoided(false);
		bill.getPayments().add(payment1);
		
		Payment payment2 = new Payment();
		payment2.setAmountTendered(BigDecimal.valueOf(70));
		payment2.setVoided(false);
		bill.getPayments().add(payment2);
		
		assertEquals(BigDecimal.valueOf(100), bill.getTotalPayments());
	}
}
