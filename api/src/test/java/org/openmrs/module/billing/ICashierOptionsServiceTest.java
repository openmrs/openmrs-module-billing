/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.ICashierOptionsService;
import org.openmrs.module.billing.api.model.CashierOptions;
import org.openmrs.module.billing.base.BaseModuleContextTest;

public class ICashierOptionsServiceTest extends BaseModuleContextTest {
	
	public static final String OPTIONS_DATASET_VALID = TestConstants.BASE_DATASET_DIR + "CashierOptionsTestValid.xml";
	
	public static final String OPTIONS_DATASET_INVALID = TestConstants.BASE_DATASET_DIR + "CashierOptionsTestInvalid.xml";
	
	private ICashierOptionsService cashierOptionsService;
	
	@BeforeEach
	public void before() {
		cashierOptionsService = Context.getService(ICashierOptionsService.class);
	}
	
	/**
	 * @verifies Load options
	 * @see ICashierOptionsService#getOptions()
	 */
	@Test
	public void getOptions_shouldLoadOptions() {
		executeDataSet(OPTIONS_DATASET_VALID);
		//		executeDataSet(IDepartmentDataServiceTest.DEPARTMENT_DATASET);
		//		executeDataSet(IItemDataServiceTest.ITEM_DATASET);
		
		CashierOptions options = cashierOptionsService.getOptions();
		Assertions.assertNull(options.getRoundingItemUuid());
		Assertions.assertEquals(3, options.getDefaultReceiptReportId());
		Assertions.assertEquals(CashierOptions.RoundingMode.MID, options.getRoundingMode());
		Assertions.assertEquals(0, (int) options.getRoundToNearest());
		Assertions.assertTrue(options.isTimesheetRequired());
	}
	
	/**
	 * @verifies Revert to defaults if there are problems loading options
	 * @see ICashierOptionsService#getOptions()
	 */
	@Test
	public void getOptions_shouldRevertToDefaultsIfThereAreProblemsLoadingOptions() {
		executeDataSet(OPTIONS_DATASET_INVALID);
		CashierOptions reference = new CashierOptions();
		CashierOptions options = cashierOptionsService.getOptions();
		Assertions.assertEquals(reference.getRoundingItemUuid(), options.getRoundingItemUuid());
		Assertions.assertEquals(reference.getDefaultReceiptReportId(), options.getDefaultReceiptReportId());
		Assertions.assertEquals(reference.getRoundingMode(), options.getRoundingMode());
		Assertions.assertEquals(reference.getRoundToNearest(), options.getRoundToNearest());
		Assertions.assertEquals(reference.isTimesheetRequired(), options.isTimesheetRequired());
	}
	
}
