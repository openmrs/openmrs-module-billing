/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.db;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.db.PaymentModeDAO;
import org.openmrs.module.billing.api.model.PaymentMode;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

public class HibernatePaymentModeDAOTest extends BaseModuleContextSensitiveTest {
	
	private PaymentModeDAO paymentModeDAO;
	
	@BeforeEach
	public void setup() {
		paymentModeDAO = Context.getRegisteredComponent("paymentModeDAO", PaymentModeDAO.class);
		
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "PaymentModeTest.xml");
	}
	
	@Test
	public void getPaymentMode_shouldReturnPaymentModeById() {
		PaymentMode paymentMode = paymentModeDAO.getPaymentMode(0);
		assertNotNull(paymentMode);
		assertEquals(0, paymentMode.getId());
		assertEquals("Test 1 Payment Mode", paymentMode.getName());
	}
	
	@Test
	public void getPaymentMode_shouldReturnNullIfPaymentModeNotFound() {
		PaymentMode paymentMode = paymentModeDAO.getPaymentMode(999);
		assertNull(paymentMode);
	}
	
	@Test
	public void getPaymentModeByUuid_shouldReturnPaymentModeByUuid() {
		PaymentMode paymentMode = paymentModeDAO.getPaymentMode(0);
		assertNotNull(paymentMode);
		String uuid = paymentMode.getUuid();
		
		PaymentMode foundPaymentMode = paymentModeDAO.getPaymentModeByUuid(uuid);
		assertNotNull(foundPaymentMode);
		assertEquals(uuid, foundPaymentMode.getUuid());
		assertEquals(0, foundPaymentMode.getId());
	}
	
	@Test
	public void getPaymentModeByUuid_shouldReturnNullIfUuidNotFound() {
		PaymentMode paymentMode = paymentModeDAO.getPaymentModeByUuid("nonexistent-uuid");
		assertNull(paymentMode);
	}
	
	@Test
	public void savePaymentMode_shouldCreateNewPaymentMode() {
		PaymentMode newPaymentMode = new PaymentMode();
		newPaymentMode.setName("New Test Payment Mode");
		newPaymentMode.setDescription("New test description");
		newPaymentMode.setSortOrder(10);
		newPaymentMode.setUuid(UUID.randomUUID().toString());
		
		PaymentMode savedPaymentMode = paymentModeDAO.savePaymentMode(newPaymentMode);
		
		assertNotNull(savedPaymentMode);
		assertNotNull(savedPaymentMode.getId());
		assertEquals("New Test Payment Mode", savedPaymentMode.getName());
		assertEquals("New test description", savedPaymentMode.getDescription());
		assertEquals(10, savedPaymentMode.getSortOrder());
	}
	
	@Test
	public void savePaymentMode_shouldUpdateExistingPaymentMode() {
		PaymentMode existingPaymentMode = paymentModeDAO.getPaymentMode(1);
		assertNotNull(existingPaymentMode);
		
		String newName = "Updated Payment Mode";
		existingPaymentMode.setName(newName);
		
		PaymentMode updatedPaymentMode = paymentModeDAO.savePaymentMode(existingPaymentMode);
		assertEquals(newName, updatedPaymentMode.getName());
	}
	
	@Test
	public void purgePaymentMode_shouldDeletePaymentMode() {
		PaymentMode newPaymentMode = new PaymentMode();
		newPaymentMode.setName("Payment Mode To Delete");
		newPaymentMode.setDescription("To be deleted");
		newPaymentMode.setSortOrder(99);
		newPaymentMode.setUuid(UUID.randomUUID().toString());
		
		PaymentMode savedPaymentMode = paymentModeDAO.savePaymentMode(newPaymentMode);
		
		Integer paymentModeId = savedPaymentMode.getId();
		assertNotNull(paymentModeId);
		
		paymentModeDAO.purgePaymentMode(savedPaymentMode);
		
		PaymentMode deletedPaymentMode = paymentModeDAO.getPaymentMode(paymentModeId);
		assertNull(deletedPaymentMode);
	}
	
	@Test
	public void getPaymentMode_shouldReturnRetiredPaymentMode() {
		PaymentMode paymentMode = paymentModeDAO.getPaymentMode(3);
		assertNotNull(paymentMode);
		assertTrue(paymentMode.getRetired());
		assertEquals("Retired Payment Mode", paymentMode.getName());
	}
	
	@Test
	public void savePaymentMode_shouldUpdateSortOrder() {
		PaymentMode paymentMode = paymentModeDAO.getPaymentMode(0);
		assertNotNull(paymentMode);
		assertEquals(0, paymentMode.getSortOrder());
		
		paymentMode.setSortOrder(100);
		PaymentMode updatedPaymentMode = paymentModeDAO.savePaymentMode(paymentMode);
		assertEquals(100, updatedPaymentMode.getSortOrder());
	}
	
	@Test
	public void getPaymentModes_shouldReturnOnlyNonRetiredPaymentModesWhenIncludeRetiredIsFalse() {
		List<PaymentMode> paymentModes = paymentModeDAO.getPaymentModes(false);
		
		assertNotNull(paymentModes);
		assertEquals(3, paymentModes.size());
		for (PaymentMode paymentMode : paymentModes) {
			assertFalse(paymentMode.getRetired());
		}
	}
	
	@Test
	public void getPaymentModes_shouldReturnAllPaymentModesIncludingRetiredWhenIncludeRetiredIsTrue() {
		List<PaymentMode> paymentModes = paymentModeDAO.getPaymentModes(true);
		
		assertNotNull(paymentModes);
		assertEquals(4, paymentModes.size());
	}
}
