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

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.IBillServiceTest;
import org.openmrs.module.billing.api.IBillService;
import org.openmrs.module.billing.api.IBillableItemsService;
import org.openmrs.module.billing.api.ICashPointService;
import org.openmrs.module.billing.api.IReceiptNumberGenerator;
import org.openmrs.module.billing.api.ReceiptNumberGeneratorFactory;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.BillableService;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Concrete implementation of IBillServiceTest for testing BillServiceImpl
 */
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@PowerMockIgnore({ "javax.management.*", "javax.net.ssl.*" })
@PrepareForTest({ ReceiptNumberGeneratorFactory.class })
public class BillServiceImplTest extends IBillServiceTest {
	
	private IReceiptNumberGenerator receiptNumberGenerator;
	
	private ProviderService providerService;
	
	private PatientService patientService;
	
	private IBillableItemsService billableItemsService;
	
	private ICashPointService cashPointService;
	
	@Before
	public void before() throws Exception {
		super.before();
		
		providerService = Context.getProviderService();
		patientService = Context.getPatientService();
		billableItemsService = Context.getService(IBillableItemsService.class);
		cashPointService = Context.getService(ICashPointService.class);
		
		mockStatic(ReceiptNumberGeneratorFactory.class);
		receiptNumberGenerator = mock(IReceiptNumberGenerator.class);
		
		when(ReceiptNumberGeneratorFactory.getGenerator()).thenReturn(receiptNumberGenerator);
	}
	
	@Override
	protected IBillService createService() {
		return Context.getService(IBillService.class);
	}
	
	/**
	 * @verifies Generate a new receipt number if one has not been defined.
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#save(Bill)
	 */
	@Test
	public void save_shouldGenerateANewReceiptNumberIfOneHasNotBeenDefined() throws Exception {
		Bill bill = createEntity(true);
		bill.setReceiptNumber(null);
		
		String receiptNumber = "Test Number";
		when(receiptNumberGenerator.generateNumber(bill)).thenReturn(receiptNumber);
		
		service.save(bill);
		Context.flushSession();
		
		Bill savedBill = service.getById(bill.getId());
		Assert.assertEquals(receiptNumber, savedBill.getReceiptNumber());
		
		verify(receiptNumberGenerator, times(1)).generateNumber(bill);
	}
	
	/**
	 * @verifies Not generate a receipt number if one has already been defined.
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#save(Bill)
	 */
	@Test
	public void save_shouldNotGenerateAReceiptNumberIfOneHasAlreadyBeenDefined() throws Exception {
		String receiptNumber = "Test Number";
		Bill bill = createEntity(true);
		bill.setReceiptNumber(receiptNumber);
		
		service.save(bill);
		Context.flushSession();
		
		Bill savedBill = service.getById(bill.getId());
		Assert.assertEquals(receiptNumber, savedBill.getReceiptNumber());
		
		verify(receiptNumberGenerator, times(0)).generateNumber(bill);
	}
	
	/**
	 * @verifies Throw APIException if receipt number cannot be generated.
	 * @see org.openmrs.module.billing.api.impl.BillServiceImpl#save(Bill)
	 */
	@Test(expected = APIException.class)
	public void save_shouldThrowAPIExceptionIfReceiptNumberCannotBeGenerated() throws Exception {
		Bill bill = createEntity(true);
		bill.setReceiptNumber(null);
		
		when(receiptNumberGenerator.generateNumber(bill)).thenThrow(new APIException("Test exception"));
		
		service.save(bill);
	}
	
	/**
	 * @verifies save a new bill when no existing pending bill is found for the patient
	 * @see IBillService#save(Bill)
	 */
	@Test
	public void save_shouldSaveNewBillWhenNoExistingPendingBillFound() throws Exception {
		// Create a bill for a patient that has no existing pending bills
		Patient patient = patientService.getPatient(1); // Patient 1 has no bills in test data
		
		Bill newBill = new Bill();
		newBill.setCashier(providerService.getProvider(0));
		newBill.setPatient(patient);
		newBill.setCashPoint(cashPointService.getById(0));
		newBill.setStatus(BillStatus.PENDING);
		
		// Add line items
		BillLineItem lineItem1 = new BillLineItem();
		BillableService service1 = billableItemsService.getById(0);
		if (service1 != null) {
			lineItem1.setBillableService(service1);
			lineItem1.setPrice(BigDecimal.valueOf(150));
			lineItem1.setQuantity(2);
			lineItem1.setPaymentStatus(BillStatus.PENDING);
			newBill.addLineItem(lineItem1);
		}
		
		// Save the bill - should create a new bill since no pending bill exists
		Bill savedBill = service.save(newBill);
		Context.flushSession();
		
		Assert.assertNotNull("Bill should be saved", savedBill);
		Assert.assertNotNull("Bill should have an ID", savedBill.getId());
		Assert.assertEquals("Bill should have PENDING status", BillStatus.PENDING, savedBill.getStatus());
		Assert.assertEquals("Bill should have 1 line item", 1, savedBill.getLineItems().size());
		Assert.assertEquals("Bill total should be 300", BigDecimal.valueOf(300), savedBill.getTotal());
		
		// Verify the bill can be retrieved
		Bill retrievedBill = service.getById(savedBill.getId());
		Assert.assertNotNull("Retrieved bill should not be null", retrievedBill);
		Assert.assertEquals("Patient should match", patient.getId(), retrievedBill.getPatient().getId());
	}
}
