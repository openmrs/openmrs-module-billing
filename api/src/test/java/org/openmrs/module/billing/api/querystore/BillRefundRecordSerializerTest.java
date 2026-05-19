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
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillRefund;
import org.openmrs.module.billing.api.model.RefundStatus;
import org.openmrs.module.querystore.model.QueryDocument;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BillRefundRecordSerializerTest {
	
	private static final String REFUND_UUID = "refund-uuid-1";
	
	private static final String BILL_UUID = "bill-uuid-1";
	
	private static final String PATIENT_UUID = "patient-uuid-1";
	
	private final BillRefundRecordSerializer serializer = new BillRefundRecordSerializer();
	
	@Test
	public void serialize_shouldSetCoreFieldsFromRefund() {
		BillRefund refund = newRefund(new BigDecimal("50.00"), RefundStatus.REQUESTED, "Patient error");
		
		QueryDocument doc = serializer.serialize(refund);
		
		assertNotNull(doc);
		assertEquals(BillingQueryStoreConstants.RESOURCE_TYPE_BILL_REFUND, doc.getResourceType());
		assertEquals(REFUND_UUID, doc.getResourceUuid());
		assertEquals(PATIENT_UUID, doc.getPatientUuid());
		assertNotNull(doc.getDate());
	}
	
	@Test
	public void serialize_shouldSetMetadataFields() {
		BillRefund refund = newRefund(new BigDecimal("50.00"), RefundStatus.APPROVED, "Patient error");
		refund.setDateApproved(new Date());
		
		QueryDocument doc = serializer.serialize(refund);
		
		assertNotNull(doc);
		assertEquals(BILL_UUID, doc.getMetadata().get(BillingQueryStoreConstants.FIELD_BILL_UUID));
		assertEquals(new BigDecimal("50.00"), doc.getMetadata().get(BillingQueryStoreConstants.FIELD_REFUND_AMOUNT));
		assertEquals("APPROVED", doc.getMetadata().get(BillingQueryStoreConstants.FIELD_STATUS));
		assertEquals("Patient error", doc.getMetadata().get(BillingQueryStoreConstants.FIELD_REASON));
		assertNotNull(doc.getMetadata().get(BillingQueryStoreConstants.FIELD_DATE_APPROVED));
	}
	
	@Test
	public void serialize_shouldIncludeLineItemUuidWhenLineScoped() {
		BillRefund refund = newRefund(new BigDecimal("25.00"), RefundStatus.REQUESTED, "Line correction");
		BillLineItem lineItem = new BillLineItem();
		lineItem.setUuid("line-item-uuid-1");
		refund.setLineItem(lineItem);
		
		QueryDocument doc = serializer.serialize(refund);
		
		assertNotNull(doc);
		assertEquals("line-item-uuid-1", doc.getMetadata().get(BillingQueryStoreConstants.FIELD_BILL_LINE_ITEM_UUID));
	}
	
	@Test
	public void serialize_shouldIncludeInitiatorApproverCompleterWhenPresent() {
		BillRefund refund = newRefund(new BigDecimal("50.00"), RefundStatus.COMPLETED, "Patient error");
		User initiator = userWithUuid("initiator-uuid");
		User approver = userWithUuid("approver-uuid");
		User completer = userWithUuid("completer-uuid");
		refund.setInitiator(initiator);
		refund.setApprover(approver);
		refund.setCompleter(completer);
		
		QueryDocument doc = serializer.serialize(refund);
		
		assertNotNull(doc);
		assertEquals("initiator-uuid", doc.getMetadata().get(BillingQueryStoreConstants.FIELD_INITIATOR_UUID));
		assertEquals("approver-uuid", doc.getMetadata().get(BillingQueryStoreConstants.FIELD_APPROVER_UUID));
		assertEquals("completer-uuid", doc.getMetadata().get(BillingQueryStoreConstants.FIELD_COMPLETER_UUID));
	}
	
	@Test
	public void serialize_shouldOmitOptionalReferencesWhenAbsent() {
		BillRefund refund = newRefund(new BigDecimal("50.00"), RefundStatus.REQUESTED, "Patient error");
		
		QueryDocument doc = serializer.serialize(refund);
		
		assertNotNull(doc);
		assertFalse(doc.getMetadata().containsKey(BillingQueryStoreConstants.FIELD_BILL_LINE_ITEM_UUID));
		assertFalse(doc.getMetadata().containsKey(BillingQueryStoreConstants.FIELD_INITIATOR_UUID));
		assertFalse(doc.getMetadata().containsKey(BillingQueryStoreConstants.FIELD_APPROVER_UUID));
		assertFalse(doc.getMetadata().containsKey(BillingQueryStoreConstants.FIELD_COMPLETER_UUID));
	}
	
	@Test
	public void serialize_shouldReturnNullWhenRefundAmountAbsent() {
		// Defensive null-guard: a partially constructed refund (validator gap, recovered transient)
		// must be skipped, not crash. Without the guard, the advice's per-entity RuntimeException
		// swallow would drop this refund from the index with only a warn log.
		BillRefund refund = newRefund(null, RefundStatus.REQUESTED, "any");
		
		QueryDocument doc = serializer.serialize(refund);
		
		assertNull(doc);
	}
	
	@Test
	public void serialize_shouldReturnNullWhenBillAbsent() {
		BillRefund refund = newRefund(new BigDecimal("50.00"), RefundStatus.REQUESTED, "any");
		refund.setBill(null);
		
		QueryDocument doc = serializer.serialize(refund);
		
		assertNull(doc);
	}
	
	@Test
	public void serialize_shouldReturnNullWhenPatientAbsent() {
		BillRefund refund = newRefund(new BigDecimal("50.00"), RefundStatus.REQUESTED, "any");
		refund.getBill().setPatient(null);
		
		QueryDocument doc = serializer.serialize(refund);
		
		assertNull(doc);
	}
	
	private BillRefund newRefund(BigDecimal amount, RefundStatus status, String reason) {
		Bill bill = new Bill();
		bill.setUuid(BILL_UUID);
		bill.setReceiptNumber("R-100");
		Patient patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		bill.setPatient(patient);
		
		BillRefund refund = new BillRefund();
		refund.setUuid(REFUND_UUID);
		refund.setBill(bill);
		refund.setRefundAmount(amount);
		refund.setStatus(status);
		refund.setReason(reason);
		refund.setVoided(false);
		refund.setDateCreated(new Date());
		return refund;
	}
	
	private User userWithUuid(String uuid) {
		User user = new User();
		user.setUuid(uuid);
		return user;
	}
}
