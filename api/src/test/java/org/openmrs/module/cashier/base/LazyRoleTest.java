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
package org.openmrs.module.cashier.base;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Role;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;

public class LazyRoleTest extends BaseModuleContextTest {
	
	public static final String BILL_DATASET = TestConstants.BASE_DATASET_DIR + "BillTest.xml";
	
	private UserService userService;
	
	@Before
	public void before() throws Exception {
		super.executeDataSet(TestConstants.CORE_DATASET);
		
		userService = Context.getUserService();
	}
	
	@Test
	public void selectAll_ShouldReturnAllRoles() throws Exception {
		List<Role> roles = userService.getAllRoles();
		
		Assert.assertEquals(8, roles.size());
	}
}
