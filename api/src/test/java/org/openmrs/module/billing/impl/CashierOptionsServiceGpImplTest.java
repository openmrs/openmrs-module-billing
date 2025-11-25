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

package org.openmrs.module.billing.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.ModuleSettings;
import org.openmrs.module.billing.api.impl.CashierOptionsServiceGpImpl;
import org.openmrs.module.billing.api.model.CashierOptions;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CashierOptionsServiceGpImplTest extends BaseModuleContextSensitiveTest {
	
	private CashierOptionsServiceGpImpl service;
	
	private AdministrationService adminService;
	
	@BeforeEach
	public void setup() {
		service = new CashierOptionsServiceGpImpl();
		adminService = Context.getAdministrationService();
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.CashierOptionsServiceGpImpl#getOptions()
	 */
	@Test
	public void getOptions_shouldReturnCashierOptionsWithDefaults() {
		CashierOptions options = service.getOptions();
		assertNotNull(options);
		assertFalse(options.isTimesheetRequired());
		assertEquals(CashierOptions.RoundingMode.MID, options.getRoundingMode());
		assertEquals(0, options.getRoundToNearest());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.CashierOptionsServiceGpImpl#getOptions()
	 */
	@Test
	public void getOptions_shouldLoadDefaultReceiptReportIdFromGlobalProperty() {
		adminService.setGlobalProperty(ModuleSettings.RECEIPT_REPORT_ID_PROPERTY, "123");
		
		CashierOptions options = service.getOptions();
		assertNotNull(options);
		assertEquals(123, options.getDefaultReceiptReportId());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.CashierOptionsServiceGpImpl#getOptions()
	 */
	@Test
	public void getOptions_shouldHandleInvalidReceiptReportId() {
		adminService.setGlobalProperty(ModuleSettings.RECEIPT_REPORT_ID_PROPERTY, "invalid");
		
		CashierOptions options = service.getOptions();
		assertNotNull(options);
		assertEquals(0, options.getDefaultReceiptReportId());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.CashierOptionsServiceGpImpl#getOptions()
	 */
	@Test
	public void getOptions_shouldLoadTimesheetRequiredFromGlobalProperty() {
		adminService.setGlobalProperty(ModuleSettings.TIMESHEET_REQUIRED_PROPERTY, "true");
		
		CashierOptions options = service.getOptions();
		assertNotNull(options);
		assertTrue(options.isTimesheetRequired());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.CashierOptionsServiceGpImpl#getOptions()
	 */
	@Test
	public void getOptions_shouldDefaultToFalseIfTimesheetRequiredIsNotSpecified() {
		adminService.setGlobalProperty(ModuleSettings.TIMESHEET_REQUIRED_PROPERTY, "");
		
		CashierOptions options = service.getOptions();
		assertNotNull(options);
		assertFalse(options.isTimesheetRequired());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.CashierOptionsServiceGpImpl#getOptions()
	 */
	@Test
	public void getOptions_shouldHandleInvalidTimesheetRequiredValue() {
		adminService.setGlobalProperty(ModuleSettings.TIMESHEET_REQUIRED_PROPERTY, "invalid");
		
		CashierOptions options = service.getOptions();
		assertNotNull(options);
		assertFalse(options.isTimesheetRequired());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.CashierOptionsServiceGpImpl#getOptions()
	 */
	@Test
	public void getOptions_shouldSetDefaultRoundingOptionsWhenRoundingItemUuidIsEmpty() {
		CashierOptions options = service.getOptions();
		assertNotNull(options);
		assertEquals(CashierOptions.RoundingMode.MID, options.getRoundingMode());
		assertEquals(0, options.getRoundToNearest());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.CashierOptionsServiceGpImpl#getOptions()
	 */
	@Test
	public void getOptions_shouldNotThrowExceptionIfNumericOptionsAreNull() {
		adminService.setGlobalProperty(ModuleSettings.RECEIPT_REPORT_ID_PROPERTY, "");
		adminService.setGlobalProperty(ModuleSettings.ROUND_TO_NEAREST_PROPERTY, "");
		
		assertDoesNotThrow(() -> {
			CashierOptions options = service.getOptions();
			assertNotNull(options);
		});
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.CashierOptionsServiceGpImpl#getOptions()
	 */
	@Test
	public void getOptions_shouldHandleMultiplePropertiesSet() {
		adminService.setGlobalProperty(ModuleSettings.RECEIPT_REPORT_ID_PROPERTY, "456");
		adminService.setGlobalProperty(ModuleSettings.TIMESHEET_REQUIRED_PROPERTY, "true");
		
		CashierOptions options = service.getOptions();
		assertNotNull(options);
		assertEquals(456, options.getDefaultReceiptReportId());
		assertTrue(options.isTimesheetRequired());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.CashierOptionsServiceGpImpl#getOptions()
	 */
	@Test
	public void getOptions_shouldLoadCashierOptionsFromTheDatabase() {
		adminService.setGlobalProperty(ModuleSettings.RECEIPT_REPORT_ID_PROPERTY, "1");
		adminService.setGlobalProperty(ModuleSettings.TIMESHEET_REQUIRED_PROPERTY, "true");
		
		CashierOptions options = service.getOptions();
		
		assertNotNull(options);
		assertEquals(1, options.getDefaultReceiptReportId());
		assertEquals(CashierOptions.RoundingMode.MID, options.getRoundingMode());
		assertEquals(0, options.getRoundToNearest());
		assertTrue(options.isTimesheetRequired());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.CashierOptionsServiceGpImpl#getOptions()
	 */
	@Test
	public void getOptions_shouldHandleNullGlobalProperties() {
		adminService.setGlobalProperty(ModuleSettings.RECEIPT_REPORT_ID_PROPERTY, null);
		adminService.setGlobalProperty(ModuleSettings.ROUNDING_MODE_PROPERTY, null);
		adminService.setGlobalProperty(ModuleSettings.ROUND_TO_NEAREST_PROPERTY, null);
		adminService.setGlobalProperty(ModuleSettings.TIMESHEET_REQUIRED_PROPERTY, null);
		
		CashierOptions options = service.getOptions();
		
		assertNotNull(options);
		assertEquals(0, options.getDefaultReceiptReportId());
		assertEquals(CashierOptions.RoundingMode.MID, options.getRoundingMode());
		assertEquals(0, options.getRoundToNearest());
		assertFalse(options.isTimesheetRequired());
	}
}
