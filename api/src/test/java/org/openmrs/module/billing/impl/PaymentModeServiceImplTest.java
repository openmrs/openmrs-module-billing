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

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.PaymentModeService;
import org.openmrs.module.billing.api.impl.PaymentModeServiceImpl;
import org.openmrs.module.billing.api.model.PaymentMode;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

public class PaymentModeServiceImplTest extends BaseModuleContextSensitiveTest {
	
	private PaymentModeService paymentModeService;
	
	@BeforeEach
	public void setup() {
		paymentModeService = Context.getService(PaymentModeService.class);
		
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "PaymentModeTest.xml");
	}
	
	/**
	 * @see PaymentModeServiceImpl#savePaymentMode(PaymentMode)
	 */
	@Test
	public void savePaymentMode_shouldThrowNullPointerExceptionIfPaymentModeIsNull() {
		assertThrows(NullPointerException.class, () -> paymentModeService.savePaymentMode(null));
	}
	
	/**
	 * @see PaymentModeServiceImpl#getPaymentMode(Integer)
	 */
	@Test
	public void getPaymentMode_shouldReturnPaymentModeWithSpecifiedId() {
		PaymentMode paymentMode = paymentModeService.getPaymentMode(0);
		assertNotNull(paymentMode);
		assertEquals(0, paymentMode.getId());
		assertEquals("Test 1 Payment Mode", paymentMode.getName());
	}
	
	/**
	 * @see PaymentModeServiceImpl#getPaymentMode(Integer)
	 */
	@Test
	public void getPaymentMode_shouldReturnNullIfIdNotFound() {
		PaymentMode paymentMode = paymentModeService.getPaymentMode(999);
		assertNull(paymentMode);
	}
	
	/**
	 * @see PaymentModeServiceImpl#getPaymentMode(Integer)
	 */
	@Test
	public void getPaymentMode_shouldReturnNullIfIdIsNull() {
		PaymentMode paymentMode = paymentModeService.getPaymentMode(null);
		assertNull(paymentMode);
	}
	
	/**
	 * @see PaymentModeServiceImpl#getPaymentModeByUuid(String)
	 */
	@Test
	public void getPaymentModeByUuid_shouldReturnPaymentModeWithSpecifiedUuid() {
		PaymentMode paymentMode = paymentModeService.getPaymentMode(0);
		assertNotNull(paymentMode);
		String uuid = paymentMode.getUuid();
		
		PaymentMode foundPaymentMode = paymentModeService.getPaymentModeByUuid(uuid);
		assertNotNull(foundPaymentMode);
		assertEquals(uuid, foundPaymentMode.getUuid());
		assertEquals(0, foundPaymentMode.getId());
	}
	
	/**
	 * @see PaymentModeServiceImpl#getPaymentModeByUuid(String)
	 */
	@Test
	public void getPaymentModeByUuid_shouldReturnNullIfUuidNotFound() {
		PaymentMode paymentMode = paymentModeService.getPaymentModeByUuid("nonexistent-uuid");
		assertNull(paymentMode);
	}
	
	/**
	 * @see PaymentModeServiceImpl#getPaymentModeByUuid(String)
	 */
	@Test
	public void getPaymentModeByUuid_shouldReturnNullIfUuidIsEmpty() {
		PaymentMode paymentMode = paymentModeService.getPaymentModeByUuid("");
		assertNull(paymentMode);
	}
	
	/**
	 * @see PaymentModeServiceImpl#savePaymentMode(PaymentMode)
	 */
	@Test
	public void savePaymentMode_shouldCreateNewPaymentMode() {
		PaymentMode newPaymentMode = new PaymentMode();
		newPaymentMode.setName("New Test Payment Mode");
		newPaymentMode.setDescription("New test description");
		newPaymentMode.setSortOrder(10);
		newPaymentMode.setUuid(UUID.randomUUID().toString());
		
		PaymentMode savedPaymentMode = paymentModeService.savePaymentMode(newPaymentMode);
		
		assertNotNull(savedPaymentMode);
		assertNotNull(savedPaymentMode.getId());
		assertEquals("New Test Payment Mode", savedPaymentMode.getName());
		assertEquals("New test description", savedPaymentMode.getDescription());
		
		PaymentMode retrievedPaymentMode = paymentModeService.getPaymentMode(savedPaymentMode.getId());
		assertNotNull(retrievedPaymentMode);
		assertEquals("New Test Payment Mode", retrievedPaymentMode.getName());
	}
	
	/**
	 * @see PaymentModeServiceImpl#savePaymentMode(PaymentMode)
	 */
	@Test
	public void savePaymentMode_shouldUpdateExistingPaymentMode() {
		PaymentMode existingPaymentMode = paymentModeService.getPaymentMode(1);
		assertNotNull(existingPaymentMode);
		
		String newName = "Updated Payment Mode Name";
		existingPaymentMode.setName(newName);
		
		paymentModeService.savePaymentMode(existingPaymentMode);
		
		PaymentMode updatedPaymentMode = paymentModeService.getPaymentMode(1);
		assertEquals(newName, updatedPaymentMode.getName());
	}
	
	/**
	 * @see PaymentModeServiceImpl#purgePaymentMode(PaymentMode)
	 */
	@Test
	public void purgePaymentMode_shouldThrowNullPointerExceptionIfPaymentModeIsNull() {
		assertThrows(NullPointerException.class, () -> paymentModeService.purgePaymentMode(null));
	}
	
	/**
	 * @see PaymentModeServiceImpl#purgePaymentMode(PaymentMode)
	 */
	@Test
	public void purgePaymentMode_shouldDeletePaymentMode() {
		PaymentMode newPaymentMode = new PaymentMode();
		newPaymentMode.setName("Payment Mode To Delete");
		newPaymentMode.setDescription("To be deleted");
		newPaymentMode.setSortOrder(99);
		newPaymentMode.setUuid(UUID.randomUUID().toString());
		
		PaymentMode savedPaymentMode = paymentModeService.savePaymentMode(newPaymentMode);
		
		Integer paymentModeId = savedPaymentMode.getId();
		assertNotNull(paymentModeId);
		
		paymentModeService.purgePaymentMode(savedPaymentMode);
		
		PaymentMode deletedPaymentMode = paymentModeService.getPaymentMode(paymentModeId);
		assertNull(deletedPaymentMode);
	}
	
	/**
	 * @see PaymentModeServiceImpl#retirePaymentMode(PaymentMode, String)
	 */
	@Test
	public void retirePaymentMode_shouldRetirePaymentMode() {
		PaymentMode paymentMode = paymentModeService.getPaymentMode(0);
		assertNotNull(paymentMode);
		assertFalse(paymentMode.getRetired());
		
		String retireReason = "No longer in use";
		PaymentMode retiredPaymentMode = paymentModeService.retirePaymentMode(paymentMode, retireReason);
		
		assertNotNull(retiredPaymentMode);
	}
	
	/**
	 * @see PaymentModeServiceImpl#retirePaymentMode(PaymentMode, String)
	 */
	@Test
	public void retirePaymentMode_shouldThrowExceptionIfReasonIsEmpty() {
		PaymentMode paymentMode = paymentModeService.getPaymentMode(0);
		
		assertThrows(IllegalArgumentException.class, () -> paymentModeService.retirePaymentMode(paymentMode, ""));
	}
	
	/**
	 * @see PaymentModeServiceImpl#unretirePaymentMode(PaymentMode)
	 */
	@Test
	public void unretirePaymentMode_shouldUnretireRetiredPaymentMode() {
		PaymentMode paymentMode = paymentModeService.getPaymentMode(3);
		assertNotNull(paymentMode);
		assertTrue(paymentMode.getRetired());
		
		PaymentMode unretiredPaymentMode = paymentModeService.unretirePaymentMode(paymentMode);
		
		assertNotNull(unretiredPaymentMode);
	}
	
	/**
	 * @see PaymentModeServiceImpl#getPaymentMode(Integer)
	 */
	@Test
	public void getPaymentMode_shouldReturnPaymentModeWithAttributeTypes() {
		PaymentMode paymentMode = paymentModeService.getPaymentMode(0);
		assertNotNull(paymentMode);
		assertNotNull(paymentMode.getAttributeTypes());
		assertFalse(paymentMode.getAttributeTypes().isEmpty());
	}
}
