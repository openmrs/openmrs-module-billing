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
package org.openmrs.module.billing.web.rest.resource;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.base.ProviderUtil;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.CashPoint;

/**
 * Unit tests for {@link BillResource}
 */
public class BillResourceTest {

	private BillResource resource;

	private BillService billService;

	private VisitService visitService;

	private MockedStatic<Context> contextMock;

	private MockedStatic<ProviderUtil> providerUtilMock;

	@BeforeEach
	public void setUp() {
		resource = new BillResource();
		billService = mock(BillService.class);
		visitService = mock(VisitService.class);

		contextMock = mockStatic(Context.class);
		contextMock.when(() -> Context.getService(BillService.class)).thenReturn(billService);
		contextMock.when(Context::getVisitService).thenReturn(visitService);

		providerUtilMock = mockStatic(ProviderUtil.class);
	}

	@AfterEach
	public void tearDown() {
		if (contextMock != null) {
			contextMock.close();
		}
		if (providerUtilMock != null) {
			providerUtilMock.close();
		}
	}

	@Test
	public void save_shouldAutoAssociateActiveVisitWhenNewBillHasNoVisit() {
		Patient patient = new Patient(1);
		Visit activeVisit = new Visit(1);

		when(visitService.getActiveVisitsByPatient(patient)).thenReturn(Collections.singletonList(activeVisit));

		Bill bill = newBillForPatient(patient);
		when(billService.saveBill(bill)).thenReturn(bill);

		resource.save(bill);

		assertSame(activeVisit, bill.getVisit(), "Bill should be associated with the patient's active visit");
	}

	@Test
	public void save_shouldLeaveVisitNullWhenPatientHasNoActiveVisit() {
		Patient patient = new Patient(1);

		when(visitService.getActiveVisitsByPatient(patient)).thenReturn(Collections.emptyList());

		Bill bill = newBillForPatient(patient);
		when(billService.saveBill(bill)).thenReturn(bill);

		resource.save(bill);

		assertNull(bill.getVisit(), "Bill visit should remain null when patient has no active visit");
	}

	@Test
	public void save_shouldNotOverrideExplicitlySetVisit() {
		Patient patient = new Patient(1);
		Visit explicitVisit = new Visit(10);
		Visit anotherActiveVisit = new Visit(20);

		when(visitService.getActiveVisitsByPatient(patient)).thenReturn(Collections.singletonList(anotherActiveVisit));

		Bill bill = newBillForPatient(patient);
		bill.setVisit(explicitVisit);
		when(billService.saveBill(bill)).thenReturn(bill);

		resource.save(bill);

		assertSame(explicitVisit, bill.getVisit(), "Explicitly set visit should not be overridden by auto-association");
	}

	@Test
	public void save_shouldNotAutoAssociateVisitForExistingBill() {
		Patient patient = new Patient(1);

		Bill bill = newBillForPatient(patient);
		bill.setId(99); // existing bill — not new
		when(billService.saveBill(bill)).thenReturn(bill);

		resource.save(bill);

		verify(visitService, never()).getActiveVisitsByPatient(any());
		assertNull(bill.getVisit(), "Existing bill should not have visit auto-associated");
	}

	/**
	 * Creates a new (unsaved) bill with a pre-set patient, cashier, and cash point
	 * so that only the visit auto-association logic is exercised in isolation.
	 */
	private Bill newBillForPatient(Patient patient) {
		Bill bill = new Bill();
		bill.setPatient(patient);
		Provider cashier = new Provider();
		cashier.setId(1);
		bill.setCashier(cashier); // pre-set to skip cashier auto-assignment
		bill.setCashPoint(new CashPoint()); // pre-set to skip cash point lookup
		bill.setPayments(new HashSet<>()); // prevent NPE in synchronizeBillStatus
		return bill;
	}
}
