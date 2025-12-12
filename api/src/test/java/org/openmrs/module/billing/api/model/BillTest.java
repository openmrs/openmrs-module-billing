package org.openmrs.module.billing.api.model;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;

import org.junit.Test;

/**
 * Test for verifying Bill model methods, particularly getTotalPayments()
 */
public class BillTest {
	
	@Test
	public void getTotalPayments_shouldExcludeVoidedPaymentsFromTotal() {
		Bill bill = new Bill();
		bill.setPayments(new HashSet<>());
		
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
		bill.setPayments(new HashSet<>());
		
		Payment voidedPayment = new Payment();
		voidedPayment.setAmountTendered(BigDecimal.valueOf(100));
		voidedPayment.setVoided(true);
		bill.getPayments().add(voidedPayment);
		
		assertEquals(BigDecimal.ZERO, bill.getTotalPayments());
	}
	
	@Test
	public void getTotal_shouldExcludeVoidedLineItemsFromTotal() {
		Bill bill = new Bill();
		bill.setLineItems(new ArrayList<>());
		
		BillLineItem lineItem1 = new BillLineItem();
		lineItem1.setPrice(BigDecimal.valueOf(100));
		lineItem1.setQuantity(2);
		lineItem1.setVoided(false);
		bill.getLineItems().add(lineItem1);
		
		BillLineItem lineItem2 = new BillLineItem();
		lineItem2.setPrice(BigDecimal.valueOf(50));
		lineItem2.setQuantity(1);
		lineItem2.setVoided(false);
		bill.getLineItems().add(lineItem2);
		
		BillLineItem voidedLineItem1 = new BillLineItem();
		voidedLineItem1.setPrice(BigDecimal.valueOf(75));
		voidedLineItem1.setQuantity(3);
		voidedLineItem1.setVoided(true);
		bill.getLineItems().add(voidedLineItem1);
		
		BillLineItem voidedLineItem2 = new BillLineItem();
		voidedLineItem2.setPrice(BigDecimal.valueOf(30));
		voidedLineItem2.setQuantity(2);
		voidedLineItem2.setVoided(true);
		bill.getLineItems().add(voidedLineItem2);
		
		assertEquals(BigDecimal.valueOf(250), bill.getTotal());
	}
	
	@Test
	public void getTotal_shouldReturnZeroWhenAllLineItemsAreVoided() {
		Bill bill = new Bill();
		bill.setLineItems(new ArrayList<>());
		
		BillLineItem voidedLineItem = new BillLineItem();
		voidedLineItem.setPrice(BigDecimal.valueOf(100));
		voidedLineItem.setQuantity(5);
		voidedLineItem.setVoided(true);
		bill.getLineItems().add(voidedLineItem);
		
		assertEquals(BigDecimal.ZERO, bill.getTotal());
	}
	
	@Test
	public void synchronizeBillStatus_shouldUpdateStatusToPaidWhenFullyPaid() {
		Bill bill = new Bill();
		bill.setLineItems(new ArrayList<>());
		bill.setPayments(new HashSet<>());
		
		BillLineItem lineItem = new BillLineItem();
		lineItem.setPrice(BigDecimal.valueOf(100));
		lineItem.setQuantity(1);
		lineItem.setVoided(false);
		bill.getLineItems().add(lineItem);
		
		Payment payment = new Payment();
		payment.setAmountTendered(BigDecimal.valueOf(100));
		payment.setVoided(false);
		bill.getPayments().add(payment);
		
		bill.synchronizeBillStatus();
		
		assertEquals(BillStatus.PAID, bill.getStatus());
	}
	
	@Test
	public void synchronizeBillStatus_shouldUpdateStatusToPostedWhenPartiallyPaid() {
		Bill bill = new Bill();
		bill.setLineItems(new ArrayList<>());
		bill.setPayments(new HashSet<>());
		
		BillLineItem lineItem = new BillLineItem();
		lineItem.setPrice(BigDecimal.valueOf(100));
		lineItem.setQuantity(1);
		lineItem.setVoided(false);
		bill.getLineItems().add(lineItem);
		
		Payment payment = new Payment();
		payment.setAmountTendered(BigDecimal.valueOf(50));
		payment.setVoided(false);
		bill.getPayments().add(payment);
		
		bill.synchronizeBillStatus();
		
		assertEquals(BillStatus.POSTED, bill.getStatus());
	}
	
	@Test
	public void synchronizeBillStatus_shouldUpdateStatusToPaidAfterVoidingLineItems() {
		Bill bill = new Bill();
		bill.setLineItems(new ArrayList<>());
		bill.setPayments(new HashSet<>());
		
		BillLineItem lineItem1 = new BillLineItem();
		lineItem1.setPrice(BigDecimal.valueOf(100));
		lineItem1.setQuantity(1);
		lineItem1.setVoided(false);
		bill.getLineItems().add(lineItem1);
		
		BillLineItem lineItem2 = new BillLineItem();
		lineItem2.setPrice(BigDecimal.valueOf(50));
		lineItem2.setQuantity(1);
		lineItem2.setVoided(false);
		bill.getLineItems().add(lineItem2);
		
		Payment payment = new Payment();
		payment.setAmountTendered(BigDecimal.valueOf(100));
		payment.setVoided(false);
		bill.getPayments().add(payment);
		
		bill.synchronizeBillStatus();
		assertEquals(BillStatus.POSTED, bill.getStatus());
		
		lineItem2.setVoided(true);
		
		bill.synchronizeBillStatus();
		assertEquals(BillStatus.PAID, bill.getStatus());
	}
	
	@Test
	public void setLineItems_shouldAllowSettingLineItemsOnNewBill() {
		Bill bill = new Bill();
		bill.setStatus(BillStatus.PENDING);
		
		ArrayList<BillLineItem> lineItems = new ArrayList<>();
		BillLineItem lineItem = new BillLineItem();
		lineItem.setPrice(BigDecimal.valueOf(100));
		lineItem.setQuantity(1);
		lineItems.add(lineItem);
		
		// Should not throw exception for new bill (no ID)
		bill.setLineItems(lineItems);
		assertEquals(1, bill.getLineItems().size());
	}
	
}
