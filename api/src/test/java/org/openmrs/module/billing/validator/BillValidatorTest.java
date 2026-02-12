/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.billing.validator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Integration tests for {@link BillValidator}
 */
public class BillValidatorTest extends BaseModuleContextSensitiveTest {

	private BillValidator billValidator;

	private BillService billService;

	private PatientService patientService;

	private VisitService visitService;

	@BeforeEach
	public void setup() {
		billValidator = new BillValidator();
		billService = Context.getService(BillService.class);
		patientService = Context.getPatientService();
		visitService = Context.getVisitService();

		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "StockOperationType.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "PaymentModeTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "BillTest.xml");
	}

	@Test
	public void validate_shouldNotRejectPendingBill() {
		Bill pendingBill = billService.getBill(2);
		assertNotNull(pendingBill);
		assertEquals(BillStatus.PENDING, pendingBill.getStatus());

		Errors errors = new BindException(pendingBill, "bill");
		billValidator.validate(pendingBill, errors);

		assertFalse(errors.hasErrors());
	}

	@Test
	public void validate_shouldNotRejectUnmodifiedPaidBill() {
		Bill paidBill = billService.getBill(1);
		assertNotNull(paidBill);
		assertEquals(BillStatus.PAID, paidBill.getStatus());

		Errors errors = new BindException(paidBill, "bill");
		billValidator.validate(paidBill, errors);

		// Unmodified PAID bills should pass validation - rejection happens only when
		// attempting to modify line items
		assertFalse(errors.hasErrors());
	}

	@Test
	public void validate_shouldAcceptBillWithMatchingVisitPatient() {
		Bill bill = billService.getBill(2); // PENDING bill
		assertNotNull(bill);

		Patient patient = bill.getPatient();
		assertNotNull(patient);

		// Get a visit for the same patient
		Visit visit = visitService.getVisit(3); // From test data - patient_id=2
		assertNotNull(visit);
		assertEquals(patient.getId(), visit.getPatient().getId());

		bill.setVisit(visit);

		Errors errors = new BindException(bill, "bill");
		billValidator.validate(bill, errors);

		assertFalse(errors.hasErrors(), "Bill with matching visit patient should pass validation");
	}

	@Test
	public void validate_shouldRejectBillWithMismatchedVisitPatient() {
		Bill bill = billService.getBill(2); // PENDING bill, patient_id=2
		assertNotNull(bill);

		Patient billPatient = bill.getPatient();
		assertNotNull(billPatient);

		// Get a visit for a DIFFERENT patient
		Visit visit = visitService.getVisit(1); // From test data - patient_id=0
		assertNotNull(visit);
		assertNotEquals(billPatient.getId(), visit.getPatient().getId(), "Visit should be for different patient");

		bill.setVisit(visit);

		Errors errors = new BindException(bill, "bill");
		billValidator.validate(bill, errors);

		assertTrue(errors.hasErrors(), "Bill with mismatched visit patient should be rejected");
		assertTrue(errors.hasFieldErrors("visit"), "Error should be on visit field");
	}

	@Test
	public void validate_shouldAcceptBillWithNullVisit() {
		Bill bill = billService.getBill(2);
		assertNotNull(bill);

		bill.setVisit(null);

		Errors errors = new BindException(bill, "bill");
		billValidator.validate(bill, errors);

		assertFalse(errors.hasErrors(), "Bill with null visit should pass validation");
	}

	@Test
	public void validate_shouldAcceptBillWithNullPatientAndNullVisit() {
		Bill bill = new Bill();
		bill.setPatient(null);
		bill.setVisit(null);

		Errors errors = new BindException(bill, "bill");
		billValidator.validate(bill, errors);

		// Should not have visit-related errors (other validation may fail but not visit)
		assertFalse(errors.hasFieldErrors("visit"), "Should not reject null visit when patient is null");
	}
}
