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

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.api.model.Timesheet;
import org.openmrs.module.querystore.model.QueryDocument;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TimesheetRecordSerializerTest {
	
	private static final String TIMESHEET_UUID = "timesheet-uuid-1";
	
	private final TimesheetRecordSerializer serializer = new TimesheetRecordSerializer();
	
	@Test
	public void serialize_shouldSetCoreFields() {
		Timesheet timesheet = newTimesheet(new Date(), null);
		
		QueryDocument doc = serializer.serialize(timesheet);
		
		assertNotNull(doc);
		assertEquals(BillingQueryStoreConstants.RESOURCE_TYPE_TIMESHEET, doc.getResourceType());
		assertEquals(TIMESHEET_UUID, doc.getResourceUuid());
		// Timesheet is provider-scoped, not patient-scoped — patientUuid must be null so the
		// document doesn't accidentally get filed under any patient.
		assertNull(doc.getPatientUuid());
		assertNotNull(doc.getDate());
	}
	
	@Test
	public void serialize_shouldEmitProviderAndCashPointMetadata() {
		// Provider.getName() derives from the linked Person's PersonName when there's no metadata
		// name; build a Person+PersonName to match the production data shape.
		Timesheet timesheet = newTimesheet(new Date(), null);
		Provider cashier = new Provider();
		cashier.setUuid("provider-uuid");
		org.openmrs.Person person = new org.openmrs.Person();
		org.openmrs.PersonName personName = new org.openmrs.PersonName();
		personName.setGivenName("Mary");
		personName.setFamilyName("");
		person.addName(personName);
		cashier.setPerson(person);
		timesheet.setCashier(cashier);
		CashPoint cashPoint = new CashPoint();
		cashPoint.setUuid("cashpoint-uuid");
		cashPoint.setName("Main Counter");
		timesheet.setCashPoint(cashPoint);
		
		QueryDocument doc = serializer.serialize(timesheet);
		
		assertNotNull(doc);
		assertEquals("provider-uuid", doc.getMetadata().get(BillingQueryStoreConstants.FIELD_PROVIDER_UUID));
		assertEquals("Mary", doc.getMetadata().get(BillingQueryStoreConstants.FIELD_CASHIER_NAME));
		assertEquals("cashpoint-uuid", doc.getMetadata().get(BillingQueryStoreConstants.FIELD_CASH_POINT_UUID));
		assertEquals("Main Counter", doc.getMetadata().get(BillingQueryStoreConstants.FIELD_CASH_POINT_NAME));
	}
	
	@Test
	public void serialize_shouldEmitClockInAndClockOut() {
		Date clockIn = new Date(1000L);
		Date clockOut = new Date(60000L);
		Timesheet timesheet = newTimesheet(clockIn, clockOut);
		
		QueryDocument doc = serializer.serialize(timesheet);
		
		assertNotNull(doc);
		assertEquals(clockIn, doc.getMetadata().get(BillingQueryStoreConstants.FIELD_CLOCK_IN));
		assertEquals(clockOut, doc.getMetadata().get(BillingQueryStoreConstants.FIELD_CLOCK_OUT));
	}
	
	@Test
	public void serialize_shouldOmitClockOutForOpenTimesheet() {
		// "Who is on duty right now?" — open timesheets have clockOut=null. The field must be
		// absent (not stored as null) so an exists-filter on clock_out cleanly separates closed
		// timesheets from open ones.
		Timesheet timesheet = newTimesheet(new Date(), null);
		
		QueryDocument doc = serializer.serialize(timesheet);
		
		assertNotNull(doc);
		assertFalse(doc.getMetadata().containsKey(BillingQueryStoreConstants.FIELD_CLOCK_OUT));
		assertTrue(doc.getText().contains("Clock out: open"), doc.getText());
	}
	
	@Test
	public void serialize_shouldEmitVoidedFlag() {
		Timesheet timesheet = newTimesheet(new Date(), new Date());
		timesheet.setVoided(true);
		
		QueryDocument doc = serializer.serialize(timesheet);
		
		assertNotNull(doc);
		assertEquals(Boolean.TRUE, doc.getMetadata().get(BillingQueryStoreConstants.FIELD_VOIDED));
	}
	
	@Test
	public void serialize_shouldEmitAuditFieldsWhenPresent() {
		// Same shared audit-fields contract as Bill / BillRefund / BillDiscount: "who clocked
		// this in" and "who voided this row" must be answerable through the index.
		Timesheet timesheet = newTimesheet(new Date(), new Date());
		User creator = new User();
		creator.setUuid("creator-uuid");
		timesheet.setCreator(creator);
		Date changed = new Date();
		timesheet.setDateChanged(changed);
		
		QueryDocument doc = serializer.serialize(timesheet);
		
		assertNotNull(doc);
		assertEquals("creator-uuid", doc.getMetadata().get(BillingQueryStoreConstants.FIELD_CREATOR_UUID));
		assertEquals(changed, doc.getMetadata().get(BillingQueryStoreConstants.FIELD_DATE_CHANGED));
		assertNotNull(doc.getMetadata().get(BillingQueryStoreConstants.FIELD_CREATED_AT));
	}
	
	private Timesheet newTimesheet(Date clockIn, Date clockOut) {
		Timesheet timesheet = new Timesheet();
		timesheet.setUuid(TIMESHEET_UUID);
		timesheet.setClockIn(clockIn);
		timesheet.setClockOut(clockOut);
		timesheet.setVoided(false);
		timesheet.setDateCreated(new Date());
		return timesheet;
	}
}
