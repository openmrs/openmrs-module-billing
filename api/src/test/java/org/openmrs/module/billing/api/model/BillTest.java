package org.openmrs.module.billing.api.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;

import org.junit.Test;
import org.openmrs.api.APIException;

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
		assertEquals(BillStatus.PAID, lineItem.getPaymentStatus());
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
		// Only non-voided line items should be set to PAID
		assertEquals(BillStatus.PAID, lineItem1.getPaymentStatus());
	}
	
	@Test
	public void synchronizeBillStatus_shouldUpdateAllNonVoidedLineItemsToPaidWhenBillIsFullyPaid() {
		Bill bill = new Bill();
		bill.setLineItems(new ArrayList<>());
		bill.setPayments(new HashSet<>());
		
		BillLineItem lineItem1 = new BillLineItem();
		lineItem1.setPrice(BigDecimal.valueOf(50));
		lineItem1.setQuantity(1);
		lineItem1.setVoided(false);
		bill.getLineItems().add(lineItem1);
		
		BillLineItem lineItem2 = new BillLineItem();
		lineItem2.setPrice(BigDecimal.valueOf(30));
		lineItem2.setQuantity(1);
		lineItem2.setVoided(false);
		bill.getLineItems().add(lineItem2);
		
		BillLineItem voidedLineItem = new BillLineItem();
		voidedLineItem.setPrice(BigDecimal.valueOf(20));
		voidedLineItem.setQuantity(1);
		voidedLineItem.setVoided(true);
		bill.getLineItems().add(voidedLineItem);
		
		Payment payment = new Payment();
		payment.setAmountTendered(BigDecimal.valueOf(80));
		payment.setVoided(false);
		bill.getPayments().add(payment);
		
		bill.synchronizeBillStatus();
		
		assertEquals(BillStatus.PAID, bill.getStatus());
		assertEquals(BillStatus.PAID, lineItem1.getPaymentStatus());
		assertEquals(BillStatus.PAID, lineItem2.getPaymentStatus());
		// Voided line items should not be updated
		assertNull(voidedLineItem.getPaymentStatus());
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
	
	@Test
	public void getTotal_shouldSubtractApprovedDiscount() {
		Bill bill = createBillWithLineItems(BigDecimal.valueOf(100), BigDecimal.valueOf(50));
		
		bill.setDiscountStatus(DiscountStatus.APPROVED);
		bill.setDiscountAmount(BigDecimal.valueOf(30));
		
		assertEquals(BigDecimal.valueOf(120), bill.getTotal());
	}
	
	@Test
	public void getTotal_shouldNotSubtractPendingDiscount() {
		Bill bill = createBillWithLineItems(BigDecimal.valueOf(100), BigDecimal.valueOf(50));
		
		bill.setDiscountStatus(DiscountStatus.PENDING);
		bill.setDiscountAmount(BigDecimal.valueOf(30));
		
		assertEquals(BigDecimal.valueOf(150), bill.getTotal());
	}
	
	@Test
	public void getTotal_shouldNotSubtractRejectedDiscount() {
		Bill bill = createBillWithLineItems(BigDecimal.valueOf(100), BigDecimal.valueOf(50));
		
		bill.setDiscountStatus(DiscountStatus.REJECTED);
		bill.setDiscountAmount(BigDecimal.valueOf(30));
		
		assertEquals(BigDecimal.valueOf(150), bill.getTotal());
	}
	
	@Test
	public void getLineItemsTotal_shouldReturnOriginalTotal() {
		Bill bill = createBillWithLineItems(BigDecimal.valueOf(100), BigDecimal.valueOf(50));
		
		bill.setDiscountStatus(DiscountStatus.APPROVED);
		bill.setDiscountAmount(BigDecimal.valueOf(30));
		
		assertEquals(BigDecimal.valueOf(150), bill.getLineItemsTotal());
	}
	
	@Test
	public void synchronizeBillStatus_shouldUseTotalAfterDiscount() {
		Bill bill = new Bill();
		bill.setLineItems(new ArrayList<>());
		bill.setPayments(new HashSet<>());
		
		BillLineItem lineItem = new BillLineItem();
		lineItem.setPrice(BigDecimal.valueOf(100));
		lineItem.setQuantity(1);
		lineItem.setVoided(false);
		bill.getLineItems().add(lineItem);
		
		bill.setDiscountStatus(DiscountStatus.APPROVED);
		bill.setDiscountAmount(BigDecimal.valueOf(30));
		
		Payment payment = new Payment();
		payment.setAmountTendered(BigDecimal.valueOf(70));
		payment.setVoided(false);
		bill.getPayments().add(payment);
		
		bill.synchronizeBillStatus();
		
		assertEquals(BillStatus.PAID, bill.getStatus());
	}
	
	@Test(expected = APIException.class)
	public void initiateDiscount_shouldRejectWhenBillStatusIsPaid() {
		Bill bill = createBillWithLineItems(BigDecimal.valueOf(100));
		bill.setStatus(BillStatus.PAID);
		bill.initiateDiscount(DiscountType.FIXED_AMOUNT, BigDecimal.valueOf(10), "Test reason");
	}
	
	@Test(expected = APIException.class)
	public void initiateDiscount_shouldRejectWhenDiscountAlreadyExists() {
		Bill bill = createBillWithLineItems(BigDecimal.valueOf(100));
		bill.setStatus(BillStatus.POSTED);
		bill.setDiscountStatus(DiscountStatus.PENDING);
		bill.initiateDiscount(DiscountType.FIXED_AMOUNT, BigDecimal.valueOf(10), "Test reason");
	}
	
	@Test(expected = APIException.class)
	public void initiateDiscount_shouldRejectPercentageOver100() {
		Bill bill = createBillWithLineItems(BigDecimal.valueOf(100));
		bill.setStatus(BillStatus.POSTED);
		bill.initiateDiscount(DiscountType.PERCENTAGE, BigDecimal.valueOf(150), "Test reason");
	}
	
	@Test(expected = APIException.class)
	public void initiateDiscount_shouldRejectAmountExceedingTotal() {
		Bill bill = createBillWithLineItems(BigDecimal.valueOf(100));
		bill.setStatus(BillStatus.POSTED);
		bill.initiateDiscount(DiscountType.FIXED_AMOUNT, BigDecimal.valueOf(200), "Test reason");
	}
	
	@Test
	public void rejectDiscount_shouldSetRejectedStatus() {
		Bill bill = createBillWithLineItems(BigDecimal.valueOf(100));
		bill.setDiscountStatus(DiscountStatus.PENDING);
		bill.rejectDiscount();
		assertEquals(DiscountStatus.REJECTED, bill.getDiscountStatus());
	}
	
	@Test(expected = APIException.class)
	public void rejectDiscount_shouldRejectWhenNotPending() {
		Bill bill = createBillWithLineItems(BigDecimal.valueOf(100));
		bill.setDiscountStatus(DiscountStatus.APPROVED);
		bill.rejectDiscount();
	}
	
	private Bill createBillWithLineItems(BigDecimal... prices) {
		Bill bill = new Bill();
		bill.setLineItems(new ArrayList<>());
		for (BigDecimal price : prices) {
			BillLineItem lineItem = new BillLineItem();
			lineItem.setPrice(price);
			lineItem.setQuantity(1);
			lineItem.setVoided(false);
			bill.getLineItems().add(lineItem);
		}
		return bill;
	}
	
}
