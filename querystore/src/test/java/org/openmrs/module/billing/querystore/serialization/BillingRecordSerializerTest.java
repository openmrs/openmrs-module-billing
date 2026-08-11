/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.querystore.serialization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillDiscount;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillRefund;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.api.model.DiscountStatus;
import org.openmrs.module.billing.api.model.DiscountType;
import org.openmrs.module.billing.api.model.Payment;
import org.openmrs.module.billing.api.model.PaymentMode;
import org.openmrs.module.billing.api.model.RefundStatus;
import org.openmrs.module.querystore.model.QueryDocument;

public class BillingRecordSerializerTest {
	
	private final BillRecordSerializer billSerializer = new BillRecordSerializer();
	
	private final BillDiscountRecordSerializer discountSerializer = new BillDiscountRecordSerializer();
	
	private final BillRefundRecordSerializer refundSerializer = new BillRefundRecordSerializer();
	
	@Test
	public void serialize_shouldProjectBillWithItemsPaymentAndStatus() {
		Patient patient = new Patient();
		patient.setUuid("patient-uuid");
		
		Bill bill = new Bill();
		bill.setUuid("bill-uuid");
		bill.setPatient(patient);
		bill.setReceiptNumber("RCT-1001");
		bill.setStatus(BillStatus.PAID);
		bill.setDateCreated(new Date());
		bill.setCashPoint(cashPoint("OPD Cashier"));
		bill.setLineItems(Arrays.asList(lineItem("Consultation", "100", 1), lineItem("Malaria RDT", "50", 2)));
		bill.setPayments(cashPayment("200"));
		
		QueryDocument doc = billSerializer.serialize(bill);
		
		assertThat(doc, is(notNullValue()));
		assertThat(doc.getResourceType(), is("billing_bill"));
		assertThat(doc.getResourceUuid(), is("bill-uuid"));
		assertThat(doc.getPatientUuid(), is("patient-uuid"));
		// text leads with the clinically-useful billed items, then the payment picture
		assertThat(doc.getText(), containsString("Consultation"));
		assertThat(doc.getText(), containsString("Malaria RDT (x2)"));
		assertThat(doc.getText(), containsString("Status: paid"));
		assertThat(doc.getText(), containsString("Total: 200"));
		assertThat(doc.getText(), containsString("paid: 200"));
		assertThat(doc.getMetadata().get("bill_status"), is((Object) "PAID"));
		assertThat(doc.getMetadata().get("total"), is((Object) "200"));
		assertThat(doc.getMetadata().get("amount_paid"), is((Object) "200"));
		assertThat(asStrings(doc.getMetadata().get("services")), hasItem("Consultation"));
		assertThat(asStrings(doc.getMetadata().get("payment_modes")), hasItem("Cash"));
	}
	
	@Test
	public void serialize_shouldSkipVoidedLineItems() {
		Bill bill = new Bill();
		bill.setUuid("bill-uuid");
		bill.setPatient(patient("patient-uuid"));
		bill.setStatus(BillStatus.PENDING);
		BillLineItem voided = lineItem("Voided service", "10", 1);
		voided.setVoided(true);
		bill.setLineItems(Arrays.asList(lineItem("Consultation", "100", 1), voided));
		
		QueryDocument doc = billSerializer.serialize(bill);
		
		assertThat(doc.getText(), containsString("Consultation"));
		assertThat(doc.getText().contains("Voided service"), is(false));
	}
	
	@Test
	public void serialize_shouldProjectDiscountScopedToPatientViaBill() {
		Bill bill = new Bill();
		bill.setReceiptNumber("RCT-1001");
		bill.setPatient(patient("patient-uuid"));
		
		BillDiscount discount = new BillDiscount();
		discount.setUuid("discount-uuid");
		discount.setBill(bill);
		discount.setDiscountType(DiscountType.FIXED_AMOUNT);
		discount.setDiscountValue(new BigDecimal("50"));
		discount.setStatus(DiscountStatus.APPROVED);
		discount.setJustification("Under-5 waiver");
		discount.setDateCreated(new Date());
		
		QueryDocument doc = discountSerializer.serialize(discount);
		
		assertThat(doc, is(notNullValue()));
		assertThat(doc.getResourceType(), is("billing_discount"));
		assertThat(doc.getPatientUuid(), is("patient-uuid"));
		assertThat(doc.getText(), containsString("Under-5 waiver"));
		assertThat(doc.getText(), containsString("Status: approved"));
		assertThat(doc.getMetadata().get("discount_status"), is((Object) "APPROVED"));
	}
	
	@Test
	public void serialize_shouldProjectRefundScopedToPatientViaBill() {
		Bill bill = new Bill();
		bill.setReceiptNumber("RCT-1001");
		bill.setPatient(patient("patient-uuid"));
		
		BillRefund refund = new BillRefund();
		refund.setUuid("refund-uuid");
		refund.setBill(bill);
		refund.setRefundAmount(new BigDecimal("120"));
		refund.setStatus(RefundStatus.COMPLETED);
		refund.setReason("Overcharge on lab test");
		refund.setDateCreated(new Date());
		
		QueryDocument doc = refundSerializer.serialize(refund);
		
		assertThat(doc, is(notNullValue()));
		assertThat(doc.getResourceType(), is("billing_refund"));
		assertThat(doc.getPatientUuid(), is("patient-uuid"));
		assertThat(doc.getText(), containsString("Overcharge on lab test"));
		assertThat(doc.getText(), containsString("amount 120"));
		assertThat(doc.getMetadata().get("refund_status"), is((Object) "COMPLETED"));
	}
	
	@Test
	public void serialize_shouldSkipDiscountWhenBillHasNoPatient() {
		BillDiscount discount = new BillDiscount();
		discount.setUuid("discount-uuid");
		discount.setBill(new Bill());
		discount.setDiscountType(DiscountType.FIXED_AMOUNT);
		discount.setDiscountValue(new BigDecimal("50"));
		discount.setStatus(DiscountStatus.PENDING);
		discount.setDateCreated(new Date());
		
		QueryDocument doc = discountSerializer.serialize(discount);
		
		// No patient scope -> no document at all (parity with the backfill scan's patient filter).
		assertThat(doc, is(nullValue()));
	}
	
	@Test
	public void serialize_shouldUseRawTotalUnaffectedByApprovedDiscount() {
		// billing_bill must not denormalize the discount-adjusted total: saveBillDiscount does not
		// re-save the bill, so a folded after-discount figure would go stale. Assert we expose the
		// raw line-item total even when an approved discount is present on the bill.
		Bill bill = new Bill();
		bill.setUuid("bill-uuid");
		bill.setPatient(patient("patient-uuid"));
		bill.setStatus(BillStatus.POSTED);
		bill.setLineItems(Arrays.asList(lineItem("Consultation", "200", 1)));
		BillDiscount approved = new BillDiscount();
		approved.setBill(bill);
		approved.setDiscountType(DiscountType.FIXED_AMOUNT);
		approved.setDiscountValue(new BigDecimal("50"));
		approved.setStatus(DiscountStatus.APPROVED);
		approved.setVoided(false);
		Set<BillDiscount> discounts = new HashSet<BillDiscount>();
		discounts.add(approved);
		bill.setDiscounts(discounts);
		
		QueryDocument doc = billSerializer.serialize(bill);
		
		assertThat(doc.getMetadata().get("total"), is((Object) "200"));
		assertThat(doc.getText(), containsString("Total: 200"));
	}
	
	@Test
	public void serialize_shouldProjectBillWithNoLineItems() {
		Bill bill = new Bill();
		bill.setUuid("bill-uuid");
		bill.setPatient(patient("patient-uuid"));
		bill.setStatus(BillStatus.PENDING);
		
		QueryDocument doc = billSerializer.serialize(bill);
		
		assertThat(doc, is(notNullValue()));
		assertThat(doc.getText(), containsString("Total: 0"));
		assertThat(doc.getMetadata().get("services"), is(nullValue()));
		assertThat(doc.getMetadata().get("billed_service_count"), is(nullValue()));
	}
	
	@Test
	public void serialize_shouldExposeDiscountPercentForPercentageDiscount() {
		Bill bill = new Bill();
		bill.setReceiptNumber("RCT-1001");
		bill.setPatient(patient("patient-uuid"));
		bill.setLineItems(Arrays.asList(lineItem("Consultation", "200", 1)));
		
		BillDiscount discount = new BillDiscount();
		discount.setUuid("discount-uuid");
		discount.setBill(bill);
		discount.setDiscountType(DiscountType.PERCENTAGE);
		discount.setDiscountValue(new BigDecimal("10"));
		discount.setStatus(DiscountStatus.APPROVED);
		discount.setDateCreated(new Date());
		
		QueryDocument doc = discountSerializer.serialize(discount);
		
		assertThat(doc.getMetadata().get("discount_percent"), is((Object) "10"));
		// discount_amount is always the resolved currency amount: 10% of the 200 bill total = 20.00
		assertThat(doc.getMetadata().get("discount_amount"), is((Object) "20.00"));
		assertThat(doc.getMetadata().get("discount_type"), is((Object) "PERCENTAGE"));
	}
	
	private static Patient patient(String uuid) {
		Patient patient = new Patient();
		patient.setUuid(uuid);
		return patient;
	}
	
	private static CashPoint cashPoint(String name) {
		CashPoint cashPoint = new CashPoint();
		cashPoint.setName(name);
		return cashPoint;
	}
	
	private static BillLineItem lineItem(String serviceName, String price, int quantity) {
		BillableService service = new BillableService();
		service.setName(serviceName);
		BillLineItem lineItem = new BillLineItem();
		lineItem.setBillableService(service);
		lineItem.setPrice(new BigDecimal(price));
		lineItem.setQuantity(quantity);
		lineItem.setVoided(false);
		return lineItem;
	}
	
	private static Set<Payment> cashPayment(String amountTendered) {
		PaymentMode mode = new PaymentMode();
		mode.setName("Cash");
		Payment payment = new Payment();
		payment.setInstanceType(mode);
		payment.setAmountTendered(new BigDecimal(amountTendered));
		payment.setVoided(false);
		Set<Payment> payments = new HashSet<Payment>();
		payments.add(payment);
		return payments;
	}
	
	@SuppressWarnings("unchecked")
	private static List<String> asStrings(Object metadataValue) {
		return (List<String>) metadataValue;
	}
}
