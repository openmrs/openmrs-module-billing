/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.Patient;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.PatientPaymentStatus;
import org.openmrs.module.billing.api.model.PatientPaymentStatusResult;

@ExtendWith(MockitoExtension.class)
public class DefaultPatientPaymentStatusResolverTest {
	
	private static final String PATIENT_UUID = "patient-uuid-123";
	
	@Mock
	private BillService billService;
	
	private DefaultPatientPaymentStatusResolver resolver;
	
	private Patient patient;
	
	@BeforeEach
	public void setUp() {
		resolver = new DefaultPatientPaymentStatusResolver(billService);
		patient = new Patient();
		patient.setUuid(PATIENT_UUID);
	}
	
	@Test
	public void resolve_shouldReturnPaidWhenPatientHasNoBills() {
		when(billService.getBillsByPatientUuid(PATIENT_UUID, null)).thenReturn(Collections.emptyList());
		
		PatientPaymentStatusResult result = resolver.resolve(patient);
		
		assertEquals(PatientPaymentStatus.PAID, result.getStatus());
		assertEquals("No outstanding bills", result.getReason());
	}
	
	@Test
	public void resolve_shouldReturnUnpaidWhenAnyBillIsPending() {
		when(billService.getBillsByPatientUuid(PATIENT_UUID, null))
		        .thenReturn(Collections.singletonList(bill(BillStatus.PENDING, false)));
		
		PatientPaymentStatusResult result = resolver.resolve(patient);
		
		assertEquals(PatientPaymentStatus.UNPAID, result.getStatus());
		assertEquals("Outstanding bill(s) present", result.getReason());
	}
	
	@Test
	public void resolve_shouldReturnUnpaidWhenAnyBillIsPosted() {
		when(billService.getBillsByPatientUuid(PATIENT_UUID, null))
		        .thenReturn(Collections.singletonList(bill(BillStatus.POSTED, false)));
		
		assertEquals(PatientPaymentStatus.UNPAID, resolver.resolve(patient).getStatus());
	}
	
	@Test
	public void resolve_shouldReturnPaidWhenAllBillsArePaid() {
		when(billService.getBillsByPatientUuid(PATIENT_UUID, null))
		        .thenReturn(Arrays.asList(bill(BillStatus.PAID, false), bill(BillStatus.PAID, false)));
		
		assertEquals(PatientPaymentStatus.PAID, resolver.resolve(patient).getStatus());
	}
	
	@Test
	public void resolve_shouldReturnPaidForExemptedAndRefundedBills() {
		when(billService.getBillsByPatientUuid(PATIENT_UUID, null))
		        .thenReturn(Arrays.asList(bill(BillStatus.EXEMPTED, false), bill(BillStatus.REFUNDED, false),
		            bill(BillStatus.PARTIALLY_REFUNDED, false), bill(BillStatus.CANCELLED, false)));
		
		assertEquals(PatientPaymentStatus.PAID, resolver.resolve(patient).getStatus());
	}
	
	@Test
	public void resolve_shouldIgnoreVoidedPendingBills() {
		when(billService.getBillsByPatientUuid(PATIENT_UUID, null))
		        .thenReturn(Collections.singletonList(bill(BillStatus.PENDING, true)));
		
		assertEquals(PatientPaymentStatus.PAID, resolver.resolve(patient).getStatus());
	}
	
	@Test
	public void resolve_shouldReturnUnpaidWhenMixHasPendingAlongsidePaid() {
		when(billService.getBillsByPatientUuid(PATIENT_UUID, null)).thenReturn(
		    Arrays.asList(bill(BillStatus.PAID, false), bill(BillStatus.PENDING, false), bill(BillStatus.PAID, false)));
		
		assertEquals(PatientPaymentStatus.UNPAID, resolver.resolve(patient).getStatus());
	}
	
	@Test
	public void resolve_shouldPassPatientUuidToBillService() {
		when(billService.getBillsByPatientUuid(PATIENT_UUID, null)).thenReturn(Collections.emptyList());
		
		resolver.resolve(patient);
		
		verify(billService).getBillsByPatientUuid(eq(PATIENT_UUID), isNull());
	}
	
	private Bill bill(BillStatus status, boolean voided) {
		Bill bill = new Bill();
		bill.setStatus(status);
		bill.setVoided(voided);
		return bill;
	}
}
