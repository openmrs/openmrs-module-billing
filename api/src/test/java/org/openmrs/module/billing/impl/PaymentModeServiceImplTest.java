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

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.PaymentModeService;
import org.openmrs.module.billing.api.impl.PaymentModeServiceImpl;
import org.openmrs.module.billing.api.model.PaymentMode;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
		assertTrue(retiredPaymentMode.getRetired());
		assertNotNull(retiredPaymentMode.getRetireReason());
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
	
	/**
	 * @see PaymentModeServiceImpl#getPaymentModes(boolean)
	 */
	@Test
	public void getPaymentModes_shouldReturnOnlyNonRetiredPaymentModesWhenIncludeRetiredIsFalse() {
		List<PaymentMode> paymentModes = paymentModeService.getPaymentModes(false);
		
		assertNotNull(paymentModes);
		assertEquals(3, paymentModes.size());
		for (PaymentMode paymentMode : paymentModes) {
			assertFalse(paymentMode.getRetired());
		}
	}
	
	/**
	 * @see PaymentModeServiceImpl#getPaymentModes(boolean)
	 */
	@Test
	public void getPaymentModes_shouldReturnAllPaymentModesIncludingRetiredWhenIncludeRetiredIsTrue() {
		List<PaymentMode> paymentModes = paymentModeService.getPaymentModes(true);
		
		assertNotNull(paymentModes);
		assertEquals(4, paymentModes.size());
	}
}
