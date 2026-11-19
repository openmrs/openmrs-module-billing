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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.model.PatientBillSummary;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

public class PatientBillSummaryServiceTest extends BaseModuleContextSensitiveTest {
	
	private BillService service;
	
	@BeforeEach
	public void setup() {
		service = Context.getService(BillService.class);
	}
	
	@Test
	public void shouldReturnSummariesThroughService() throws Exception {
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "StockOperationType.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "PaymentModeTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "PatientBillSummaryTest.xml");
		
		List<PatientBillSummary> result = service.getPatientBillSummaries(null, new PagingInfo(1, 10));
		
		assertEquals(3, result.size());
		assertEquals(Integer.valueOf(9002), result.get(0).getPatient().getPatientId());
	}
	
	@Test
	public void shouldRequireViewBillsPrivilege() {
		Context.logout();
		
		assertThrows(APIAuthenticationException.class, () -> service.getPatientBillSummaries(null, null));
	}
	
	@Test
	public void shouldDenyLoggedInUserWithoutViewBillsPrivilege() {
		// "butch" (core standard test dataset) has only the Provider role, without View Cashier Bills.
		Context.becomeUser("butch");
		
		assertThrows(APIAuthenticationException.class, () -> service.getPatientBillSummaries(null, null));
	}
}
