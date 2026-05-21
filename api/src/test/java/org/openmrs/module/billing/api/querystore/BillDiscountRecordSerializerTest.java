/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.querystore;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillDiscount;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.DiscountStatus;
import org.openmrs.module.billing.api.model.DiscountType;
import org.openmrs.module.querystore.model.QueryDocument;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BillDiscountRecordSerializerTest {
	
	private static final String DISCOUNT_UUID = "discount-uuid-1";
	
	private static final String BILL_UUID = "bill-uuid-1";
	
	private static final String PATIENT_UUID = "patient-uuid-1";
	
	private final BillDiscountRecordSerializer serializer = new BillDiscountRecordSerializer();
	
	@Test
	public void serialize_shouldSetCoreFieldsFromDiscount() {
		BillDiscount discount = newDiscount(DiscountType.FIXED_AMOUNT, new BigDecimal("25.00"), DiscountStatus.PENDING);
		
		QueryDocument doc = serializer.serialize(discount);
		
		assertNotNull(doc);
		assertEquals(BillingQueryStoreConstants.RESOURCE_TYPE_BILL_DISCOUNT, doc.getResourceType());
		assertEquals(DISCOUNT_UUID, doc.getResourceUuid());
		assertEquals(PATIENT_UUID, doc.getPatientUuid(),
		    "patientUuid must come from the parent bill so the approval-queue query stays patient-scoped");
		assertNotNull(doc.getDate());
	}
	
	@Test
	public void serialize_shouldEmitDiscountSpecificMetadata() {
		BillDiscount discount = newDiscount(DiscountType.FIXED_AMOUNT, new BigDecimal("25.00"), DiscountStatus.APPROVED);
		discount.setJustification("Charity-eligible patient");
		
		QueryDocument doc = serializer.serialize(discount);
		
		assertNotNull(doc);
		assertEquals(BILL_UUID, doc.getMetadata().get(BillingQueryStoreConstants.FIELD_BILL_UUID));
		assertEquals("APPROVED", doc.getMetadata().get(BillingQueryStoreConstants.FIELD_STATUS));
		assertEquals("FIXED_AMOUNT", doc.getMetadata().get(BillingQueryStoreConstants.FIELD_DISCOUNT_TYPE));
		assertEquals(new BigDecimal("25.00"), doc.getMetadata().get(BillingQueryStoreConstants.FIELD_DISCOUNT_VALUE));
		assertEquals(new BigDecimal("25.00"), doc.getMetadata().get(BillingQueryStoreConstants.FIELD_DISCOUNT_AMOUNT));
		assertEquals("Charity-eligible patient", doc.getMetadata().get(BillingQueryStoreConstants.FIELD_JUSTIFICATION));
	}
	
	@Test
	public void serialize_shouldIncludeLineItemUuidWhenLineScoped() {
		BillDiscount discount = newDiscount(DiscountType.PERCENTAGE, new BigDecimal("10"), DiscountStatus.APPROVED);
		BillLineItem lineItem = new BillLineItem();
		lineItem.setUuid("line-item-uuid-1");
		lineItem.setPrice(new BigDecimal("50.00"));
		lineItem.setQuantity(2);
		lineItem.setVoided(false);
		discount.setLineItem(lineItem);
		
		QueryDocument doc = serializer.serialize(discount);
		
		assertNotNull(doc);
		assertEquals("line-item-uuid-1", doc.getMetadata().get(BillingQueryStoreConstants.FIELD_BILL_LINE_ITEM_UUID));
	}
	
	@Test
	public void serialize_shouldIncludeInitiatorAndApprover() {
		BillDiscount discount = newDiscount(DiscountType.FIXED_AMOUNT, new BigDecimal("10"), DiscountStatus.APPROVED);
		discount.setInitiator(userWithUuid("initiator-uuid"));
		discount.setApprover(userWithUuid("approver-uuid"));
		
		QueryDocument doc = serializer.serialize(discount);
		
		assertNotNull(doc);
		assertEquals("initiator-uuid", doc.getMetadata().get(BillingQueryStoreConstants.FIELD_INITIATOR_UUID));
		assertEquals("approver-uuid", doc.getMetadata().get(BillingQueryStoreConstants.FIELD_APPROVER_UUID));
	}
	
	@Test
	public void serialize_shouldOmitApproverWhenStillPending() {
		// PENDING discounts haven't been approved yet — the approver_uuid field must be absent
		// (not an empty string) so a "find unassigned approvals" query can use a missing-field
		// filter rather than equality against an empty value.
		BillDiscount discount = newDiscount(DiscountType.FIXED_AMOUNT, new BigDecimal("10"), DiscountStatus.PENDING);
		discount.setInitiator(userWithUuid("initiator-uuid"));
		
		QueryDocument doc = serializer.serialize(discount);
		
		assertNotNull(doc);
		assertFalse(doc.getMetadata().containsKey(BillingQueryStoreConstants.FIELD_APPROVER_UUID));
	}
	
	@Test
	public void serialize_shouldEmitTextBlobIncludingJustification() {
		BillDiscount discount = newDiscount(DiscountType.PERCENTAGE, new BigDecimal("15"), DiscountStatus.APPROVED);
		discount.setJustification("Charity-eligible patient");
		
		QueryDocument doc = serializer.serialize(discount);
		
		assertNotNull(doc);
		assertTrue(doc.getText().contains("Discount on bill R-100."), doc.getText());
		assertTrue(doc.getText().contains("Status: APPROVED."), doc.getText());
		assertTrue(doc.getText().contains("Type: PERCENTAGE."), doc.getText());
		assertTrue(doc.getText().contains("Reason: Charity-eligible patient."), doc.getText());
	}
	
	@Test
	public void serialize_shouldReturnNullWhenBillAbsent() {
		BillDiscount discount = newDiscount(DiscountType.FIXED_AMOUNT, new BigDecimal("10"), DiscountStatus.PENDING);
		discount.setBill(null);
		
		QueryDocument doc = serializer.serialize(discount);
		
		assertNull(doc);
	}
	
	@Test
	public void serialize_shouldReturnNullWhenPatientAbsent() {
		BillDiscount discount = newDiscount(DiscountType.FIXED_AMOUNT, new BigDecimal("10"), DiscountStatus.PENDING);
		discount.getBill().setPatient(null);
		
		QueryDocument doc = serializer.serialize(discount);
		
		assertNull(doc);
	}
	
	@Test
	public void serialize_shouldReturnNullWhenDiscountTypeAbsent() {
		// Discount type drives getDiscountAmount(); a partially constructed row (validator gap)
		// would NPE inside that derivation. The advice swallows the NPE per-entity and would
		// silently drop the row from the approval queue — better to skip with a null doc here.
		BillDiscount discount = newDiscount(null, new BigDecimal("10"), DiscountStatus.PENDING);
		
		QueryDocument doc = serializer.serialize(discount);
		
		assertNull(doc);
	}
	
	@Test
	public void serialize_shouldReturnNullWhenDiscountValueAbsent() {
		BillDiscount discount = newDiscount(DiscountType.FIXED_AMOUNT, null, DiscountStatus.PENDING);
		
		QueryDocument doc = serializer.serialize(discount);
		
		assertNull(doc);
	}
	
	private BillDiscount newDiscount(DiscountType type, BigDecimal value, DiscountStatus status) {
		Bill bill = new Bill();
		bill.setUuid(BILL_UUID);
		bill.setReceiptNumber("R-100");
		Patient patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		bill.setPatient(patient);
		
		BillDiscount discount = new BillDiscount();
		discount.setUuid(DISCOUNT_UUID);
		discount.setBill(bill);
		discount.setDiscountType(type);
		discount.setDiscountValue(value);
		discount.setStatus(status);
		discount.setVoided(false);
		discount.setDateCreated(new Date());
		return discount;
	}
	
	private User userWithUuid(String uuid) {
		User user = new User();
		user.setUuid(uuid);
		return user;
	}
}
