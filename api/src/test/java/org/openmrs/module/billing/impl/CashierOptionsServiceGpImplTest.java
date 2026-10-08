/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
		
		CashierOptions options = service.getOptions();
		assertNotNull(options);
		assertEquals(456, options.getDefaultReceiptReportId());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.CashierOptionsServiceGpImpl#getOptions()
	 */
	@Test
	public void getOptions_shouldLoadCashierOptionsFromTheDatabase() {
		adminService.setGlobalProperty(ModuleSettings.RECEIPT_REPORT_ID_PROPERTY, "1");
		
		CashierOptions options = service.getOptions();
		
		assertNotNull(options);
		assertEquals(1, options.getDefaultReceiptReportId());
		assertEquals(CashierOptions.RoundingMode.MID, options.getRoundingMode());
		assertEquals(0, options.getRoundToNearest());
	}
	
	/**
	 * @see org.openmrs.module.billing.api.impl.CashierOptionsServiceGpImpl#getOptions()
	 */
	@Test
	public void getOptions_shouldHandleNullGlobalProperties() {
		adminService.setGlobalProperty(ModuleSettings.RECEIPT_REPORT_ID_PROPERTY, null);
		adminService.setGlobalProperty(ModuleSettings.ROUNDING_MODE_PROPERTY, null);
		adminService.setGlobalProperty(ModuleSettings.ROUND_TO_NEAREST_PROPERTY, null);
		
		CashierOptions options = service.getOptions();
		
		assertNotNull(options);
		assertEquals(0, options.getDefaultReceiptReportId());
		assertEquals(CashierOptions.RoundingMode.MID, options.getRoundingMode());
		assertEquals(0, options.getRoundToNearest());
	}
}
