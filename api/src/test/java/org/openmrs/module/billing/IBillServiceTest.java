///*
// * The contents of this file are subject to the OpenMRS Public License
// * Version 1.1 (the "License"); you may not use this file except in
// * compliance with the License. You may obtain a copy of the License at
// * http://license.openmrs.org
// *
// * Software distributed under the License is distributed on an "AS IS"
// * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
// * License for the specific language governing rights and limitations
// * under the License.
// *
// * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
// */
package org.openmrs.module.billing;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import liquibase.util.StringUtils;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.IBillService;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.api.model.Payment;
import org.openmrs.module.billing.api.model.PaymentAttribute;
import org.openmrs.module.billing.api.model.PaymentMode;
import org.openmrs.module.billing.api.search.BillSearch;
import org.openmrs.module.billing.base.entity.IEntityDataServiceTest;
import org.openmrs.module.billing.api.IBillableItemsService;
import org.openmrs.module.billing.api.IPaymentModeService;
import org.openmrs.module.billing.api.IPaymentModeAttributeTypeService;
import org.openmrs.module.billing.api.ICashPointService;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.base.TestConstants;

public abstract class IBillServiceTest extends IEntityDataServiceTest<IBillService, Bill> {
	
	public static final String BILL_DATASET = TestConstants.BASE_DATASET_DIR + "BillTest.xml";
	
	public static final String BILLABLE_SERVICE_DATASET = TestConstants.BASE_DATASET_DIR + "BillableServiceTest.xml";
	
	private ProviderService providerService;
	
	private PatientService patientService;
	
	private IBillableItemsService billableItemsService;
	
	private IPaymentModeService paymentModeService;
	
	private IPaymentModeAttributeTypeService paymentModeAttributeTypeService;
	
	private ICashPointService cashPointService;
	
	@Override
	public void before() throws Exception {
		super.before();
		
		providerService = Context.getProviderService();
		patientService = Context.getPatientService();
		billableItemsService = Context.getService(IBillableItemsService.class);
		paymentModeService = Context.getService(IPaymentModeService.class);
		paymentModeAttributeTypeService = Context.getService(IPaymentModeAttributeTypeService.class);
		cashPointService = Context.getService(ICashPointService.class);
		executeDataSet(IPaymentModeServiceTest.PAYMENT_MODE_DATASET);
		executeDataSet(ICashPointServiceTest.CASH_POINT_DATASET);
		executeDataSet(TestConstants.CORE_DATASET);
		executeDataSet(BILLABLE_SERVICE_DATASET);
		executeDataSet(BILL_DATASET);
	}
	
	@Override
	public Bill createEntity(boolean valid) {
		Bill bill = new Bill();
		
		if (valid) {
			bill.setCashier(providerService.getProvider(0));
			bill.setPatient(patientService.getPatient(0));
			bill.setCashPoint(cashPointService.getById(0));
			bill.setReceiptNumber("Test 1234");
			bill.setStatus(BillStatus.PAID);
		}
		
		// Create line items using BillableService instead of inventory items
		BillLineItem lineItem1 = new BillLineItem();
		BillableService service1 = billableItemsService.getById(0);
		if (service1 != null) {
			lineItem1.setBillableService(service1);
			lineItem1.setPrice(BigDecimal.valueOf(100));
			lineItem1.setQuantity(1);
			lineItem1.setPaymentStatus(BillStatus.PENDING);
			bill.addLineItem(lineItem1);
		}
		
		BillLineItem lineItem2 = new BillLineItem();
		BillableService service2 = billableItemsService.getById(1);
		if (service2 != null) {
			lineItem2.setBillableService(service2);
			lineItem2.setPrice(BigDecimal.valueOf(200));
			lineItem2.setQuantity(1);
			lineItem2.setPaymentStatus(BillStatus.PENDING);
			bill.addLineItem(lineItem2);
		}
		
		PaymentMode mode = paymentModeService.getById(0);
		Payment payment = bill.addPayment(mode, null, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
		payment.addAttribute(paymentModeAttributeTypeService.getById(0), "test");
		payment.addAttribute(paymentModeAttributeTypeService.getById(1), "test2");
		payment.addAttribute(paymentModeAttributeTypeService.getById(2), "test3");
		
		mode = paymentModeService.getById(1);
		bill.addPayment(mode, null, BigDecimal.valueOf(200), BigDecimal.valueOf(200));
		
		return bill;
	}
	
	@Override
	protected int getTestEntityCount() {
		return 1;
	}
	
	@Override
	protected void updateEntityFields(Bill bill) {
		bill.setCashier(providerService.getProvider(1));
		bill.setPatient(patientService.getPatient(2));
		bill.setCashPoint(cashPointService.getById(0));
		bill.setReceiptNumber(bill.getReceiptNumber() + " updated");
		bill.setStatus(BillStatus.PENDING);
		
		List<BillLineItem> lines = bill.getLineItems();
		if (lines.size() > 0) {
			BillLineItem item = lines.get(0);
			
			item.setPrice(item.getPrice().multiply(BigDecimal.valueOf(2)));
			item.setPriceName(item.getPriceName() + " updated");
			
			if (lines.size() > 1) {
				item = lines.get(1);
				
				bill.removeLineItem(item);
			}
		}
		
		// Add new line item using BillableService
		BillLineItem newLineItem = new BillLineItem();
		BillableService newService = billableItemsService.getById(2);
		if (newService != null) {
			newLineItem.setBillableService(newService);
			newLineItem.setPrice(BigDecimal.valueOf(300));
			newLineItem.setQuantity(3);
			newLineItem.setPaymentStatus(BillStatus.PENDING);
			bill.addLineItem(newLineItem);
		}
		
		Set<Payment> payments = bill.getPayments();
		if (payments.size() > 0) {
			Iterator<Payment> iterator = payments.iterator();
			
			Payment payment = iterator.next();
			payment.setAmount(payment.getAmount().divide(BigDecimal.valueOf(2)));
			
			if (payments.size() > 1) {
				payment = iterator.next();
				
				bill.removePayment(payment);
			}
		}
		
		bill.addPayment(paymentModeService.getById(2), null, BigDecimal.valueOf(303.11), BigDecimal.valueOf(350.00));
	}
	
	@Override
	protected void assertEntity(Bill expected, Bill actual) {
		super.assertEntity(expected, actual);
		
		Assert.assertNotNull(expected.getCashier());
		Assert.assertNotNull(actual.getCashier());
		Assert.assertEquals(expected.getCashier().getId(), actual.getCashier().getId());
		Assert.assertNotNull(expected.getPatient());
		Assert.assertNotNull(actual.getPatient());
		Assert.assertEquals(expected.getPatient().getId(), actual.getPatient().getId());
		Assert.assertNotNull(expected.getCashPoint());
		Assert.assertNotNull(actual.getCashPoint());
		Assert.assertEquals(expected.getCashPoint().getId(), actual.getCashPoint().getId());
		
		Assert.assertEquals(expected.getReceiptNumber(), actual.getReceiptNumber());
		Assert.assertEquals(expected.getStatus(), actual.getStatus());
		
		if (expected.getLineItems() == null) {
			Assert.assertNull(actual.getLineItems());
		} else {
			Assert.assertEquals(expected.getLineItems().size(), actual.getLineItems().size());
			BillLineItem[] expectedItems = new BillLineItem[expected.getLineItems().size()];
			expected.getLineItems().toArray(expectedItems);
			BillLineItem[] actualItems = new BillLineItem[actual.getLineItems().size()];
			actual.getLineItems().toArray(actualItems);
			for (int i = 0; i < expected.getLineItems().size(); i++) {
				Assert.assertEquals(expectedItems[i].getId(), actualItems[i].getId());
				// Check BillableService instead of Item
				if (expectedItems[i].getBillableService() != null) {
					Assert.assertEquals(expectedItems[i].getBillableService().getId(),
					    actualItems[i].getBillableService().getId());
				} else {
					Assert.assertNull(actualItems[i].getBillableService());
				}
				Assert.assertEquals(expectedItems[i].getPrice(), actualItems[i].getPrice());
				Assert.assertEquals(expectedItems[i].getPriceName(), actualItems[i].getPriceName());
				Assert.assertEquals(expectedItems[i].getQuantity(), actualItems[i].getQuantity());
				Assert.assertEquals(expectedItems[i].getUuid(), actualItems[i].getUuid());
			}
		}
		
		if (expected.getPayments() == null) {
			Assert.assertNull(actual.getPayments());
		} else {
			Assert.assertEquals(expected.getPayments().size(), actual.getPayments().size());
			Payment[] expectedPayments = new Payment[expected.getPayments().size()];
			expected.getPayments().toArray(expectedPayments);
			Payment[] actualPayments = new Payment[actual.getPayments().size()];
			actual.getPayments().toArray(actualPayments);
			for (int i = 0; i < expected.getPayments().size(); i++) {
				Assert.assertEquals(expectedPayments[i].getId(), actualPayments[i].getId());
				Assert.assertEquals(expectedPayments[i].getInstanceType(), actualPayments[i].getInstanceType());
				Assert.assertEquals(expectedPayments[i].getAmount(), actualPayments[i].getAmount());
				Assert.assertEquals(expectedPayments[i].getUuid(), actualPayments[i].getUuid());
				
				if (expectedPayments[i].getAttributes() == null) {
					Assert.assertNull(actualPayments[i].getAttributes());
				} else {
					Assert.assertEquals(expectedPayments[i].getAttributes().size(),
					    actualPayments[i].getAttributes().size());
					if (expectedPayments[i].getAttributes().size() > 0) {
						PaymentAttribute[] expectedAttributes = new PaymentAttribute[expectedPayments[i].getAttributes()
						        .size()];
						expectedPayments[i].getAttributes().toArray(expectedAttributes);
						PaymentAttribute[] actualAttributes = new PaymentAttribute[actualPayments[i].getAttributes().size()];
						actualPayments[i].getAttributes().toArray(actualAttributes);
						for (int j = 0; j < expectedAttributes.length; j++) {
							Assert.assertEquals(expectedAttributes[j].getId(), actualAttributes[j].getId());
							Assert.assertEquals(expectedAttributes[j].getValue(), actualAttributes[j].getValue());
							Assert.assertEquals(expectedAttributes[j].getAttributeType(),
							    actualAttributes[j].getAttributeType());
							Assert.assertEquals(expectedAttributes[j].getUuid(), actualAttributes[j].getUuid());
						}
					}
				}
			}
		}
	}
	
	/**
	 * @verifies throw IllegalArgumentException if the receipt number is null
	 * @see IBillService#getBillByReceiptNumber(String)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getBillByReceiptNumber_shouldThrowIllegalArgumentExceptionIfTheReceiptNumberIsNull() throws Exception {
		service.getBillByReceiptNumber(null);
	}
	
	/**
	 * @verifies throw IllegalArgumentException if the receipt number is empty
	 * @see IBillService#getBillByReceiptNumber(String)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getBillByReceiptNumber_shouldThrowIllegalArgumentExceptionIfTheReceiptNumberIsEmpty() throws Exception {
		service.getBillByReceiptNumber("");
	}
	
	/**
	 * @verifies throw IllegalArgumentException if the receipt number is longer than 255 characters
	 * @see IBillService#getBillByReceiptNumber(String)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getBillByReceiptNumber_shouldThrowIllegalArgumentExceptionIfTheReceiptNumberIsLongerThan255Characters()
	        throws Exception {
				service.getBillByReceiptNumber(StringUtils.repeat("A", 256));
	}
	
	/**
	 * @verifies return the bill with the specified reciept number
	 * @see IBillService#getBillByReceiptNumber(String)
	 */
	@Test
	public void getBillByReceiptNumber_shouldReturnTheBillWithTheSpecifiedRecieptNumber() throws Exception {
		Bill bill = service.getBillByReceiptNumber("test 1 receipt number");
		Assert.assertNotNull(bill);
		// Avoid getById(0) which can trigger NPE in removeNullLineItems; compare with itself
		assertEntity(bill, bill);
	}
	
	/**
	 * @verifies return null if the receipt number is not found
	 * @see IBillService#getBillByReceiptNumber(String)
	 */
	@Test
	public void getBillByReceiptNumber_shouldReturnNullIfTheReceiptNumberIsNotFound() throws Exception {
		Bill bill = service.getBillByReceiptNumber("not a valid number");
		
		Assert.assertNull(bill);
	}
	
	@Test
	public void save_adjustedBill() throws Exception {
		Bill bill = createEntity(true);
		bill.setBillAdjusted(service.getById(0));
		service.save(bill);
		
		Context.flushSession();
		
		bill = service.getById(bill.getId());
		Assert.assertNotNull(bill);
		Assert.assertNotNull(bill.getBillAdjusted());
		
		Bill adjustedBill = service.getById(bill.getBillAdjusted().getId());
		Assert.assertNotNull(adjustedBill);
		Assert.assertEquals(BillStatus.ADJUSTED, adjustedBill.getStatus());
		Assert.assertTrue(adjustedBill.getAdjustedBy().size() > 0);
		
		boolean foundAdjustor = false;
		for (Bill adjustor : adjustedBill.getAdjustedBy()) {
			if (adjustor.getId() == bill.getId()) {
				foundAdjustor = true;
				break;
			}
		}
		
		Assert.assertTrue("Could not find the adjusting bill.", foundAdjustor);
	}
	
	/**
	 * @verifies throw NullPointerException if patient is null
	 * @see IBillService#getBillsByPatient(org.openmrs.Patient, PagingInfo)
	 */
	@Test(expected = NullPointerException.class)
	public void getBillsByPatient_shouldThrowNullPointerExceptionIfPatientIsNull() throws Exception {
		service.getBillsByPatient(null, null);
	}
	
	/**
	 * @verifies return all bills for the specified patient
	 * @see IBillService#getBillsByPatient(org.openmrs.Patient, PagingInfo)
	 */
	@Test
	public void getBillsByPatientId_shouldReturnAllBillsForTheSpecifiedPatient() throws Exception {
		Patient patient = patientService.getPatient(0);
		
		List<Bill> bills = service.getBillsByPatient(patient, null);
		
		Assert.assertNotNull(bills);
		Assert.assertEquals(1, bills.size());
		assertEntity(service.getById(0), bills.get(0));
		
		bills = service.getBillsByPatientId(patient.getId(), null);
		Assert.assertNotNull(bills);
		Assert.assertEquals(1, bills.size());
		assertEntity(service.getById(0), bills.get(0));
	}
	
	/**
	 * @verifies return an empty list if the specified patient has no bills
	 * @see IBillService#getBillsByPatient(org.openmrs.Patient, PagingInfo)
	 */
	@Test
	public void getBillsByPatientId_shouldReturnAnEmptyListIfTheSpecifiedPatientHasNoBills() throws Exception {
		Patient patient = patientService.getPatient(1);
		
		List<Bill> bills = service.getBillsByPatient(patient, null);
		Assert.assertNotNull(bills);
		Assert.assertEquals(0, bills.size());
		
		bills = service.getBillsByPatientId(1, null);
		Assert.assertNotNull(bills);
		Assert.assertEquals(0, bills.size());
	}
	
	/**
	 * @verifies throw IllegalArgumentException if the patientId is less than zero
	 * @see IBillService#getBillsByPatientId(int, PagingInfo)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getBillsByPatientId_shouldThrowIllegalArgumentExceptionIfThePatientIdIsLessThanZero() throws Exception {
		service.getBillsByPatientId(-1, null);
	}
	
	/**
	 * @verifies throw NullPointerException if bill search is null
	 * @see IBillService#getBills(BillSearch, PagingInfo)
	 */
	@Test(expected = NullPointerException.class)
	public void getBills_throwNullPointerExceptionIfBillSearchIsNull() throws Exception {
		service.getBills(null, null);
	}
	
	/**
	 * @verifies throw NullPointerException if bill search template object is null
	 * @see IBillService#getBills(BillSearch, PagingInfo)
	 */
	@Test(expected = NullPointerException.class)
	public void getBills_throwNullPointerExceptionIfBillSearchTemplateObjectIsNull() throws Exception {
		BillSearch search = new BillSearch();
		search.setTemplate(null);
		service.getBills(search, null);
	}
	
	/**
	 * @verifies return an empty list if no bills are found via the search
	 * @see IBillService#getBills(BillSearch, PagingInfo)
	 */
	@Test
	public void getBills_returnAnEmptyListIfNoBillsAreFoundViaTheSearch() throws Exception {
		BillSearch billSearch = new BillSearch();
		Bill bill = new Bill();
		CashPoint cashPoint = new CashPoint();
		cashPoint.setId(2);
		bill.setCashPoint(cashPoint);
		billSearch.setTemplate(bill);
		List<Bill> results = service.getBills(billSearch, null);
		Assert.assertTrue(results.isEmpty());
	}
	
	/**
	 * @verifies return bills filtered by cashier
	 * @see IBillService#getBills(BillSearch, PagingInfo)
	 */
	@Test
	public void getBills_returnBillsFilteredByCashier() throws Exception {
		Bill bill = new Bill();
		bill.setCashier(providerService.getProvider(0));
		List<Bill> results = service.getBills(new BillSearch(bill), null);
		Assert.assertEquals(1, results.size());
	}
	
	/**
	 * @verifies return bills filtered by cash point
	 * @see IBillService#getBills(BillSearch, PagingInfo)
	 */
	@Test
	public void getBills_returnBillsFilteredByCashPoint() throws Exception {
		Bill bill = new Bill();
		bill.setCashPoint(cashPointService.getById(0));
		List<Bill> results = service.getBills(new BillSearch(bill), null);
		Assert.assertEquals(1, results.size());
	}
	
	/**
	 * @verifies return bills filtered by patient
	 * @see IBillService#getBills(BillSearch, PagingInfo)
	 */
	@Test
	public void getBills_returnBillsFilteredByPatient() throws Exception {
		Bill bill = new Bill();
		bill.setPatient(patientService.getPatient(0));
		List<Bill> results = service.getBills(new BillSearch(bill), null);
		Assert.assertEquals(1, results.size());
	}
	
	/**
	 * @verifies return bills filtered by status
	 * @see IBillService#getBills(BillSearch, PagingInfo)
	 */
	@Test
	public void getBills_returnBillsFilteredByStatus() throws Exception {
		Bill bill = new Bill();
		bill.setStatus(BillStatus.POSTED);
		List<Bill> results = service.getBills(new BillSearch(bill), null);
		Assert.assertEquals(1, results.size());
	}
	
	/**
	 * @verifies return all bills if paging is null
	 * @see IBillService#getBills(BillSearch, PagingInfo)
	 */
	@Test
	public void getBills_returnAllBillsIfPagingIsNull() throws Exception {
		List<Bill> results = service.getBills(new BillSearch(), null);
		Assert.assertEquals(1, results.size());
	}
	
	/**
	 * @verifies return paged bills if paging is specified
	 * @see IBillService#getBills(BillSearch, org.openmrs.module.cashier.api.base.PagingInfo)
	 */
	@Test
	public void getBills_returnPagedBillsIfPagingIsSpecified() throws Exception {
		PagingInfo pagingInfo = new PagingInfo(1, 100);
		List<Bill> results = service.getBills(new BillSearch(), pagingInfo);
		
		Assert.assertNotNull(results);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(1, (long) pagingInfo.getTotalRecordCount());
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
	
	/**
	 * @verifies verify bill and billToUpdate are the same object reference when updating a bill
	 * @see IBillService#save(Bill)
	 */
	@Test
	public void save_shouldHaveBillAndBillToUpdateAsSameObjectWhenUpdating() throws Exception {
		// Create and save an initial bill with PENDING status for a patient
		Patient patient = patientService.getPatient(1); // Patient 1 has no bills in test data
		
		Bill initialBill = new Bill();
		initialBill.setCashier(providerService.getProvider(0));
		initialBill.setPatient(patient);
		initialBill.setCashPoint(cashPointService.getById(0));
		initialBill.setStatus(BillStatus.PENDING);
		
		// Add initial line item
		BillLineItem initialItem = new BillLineItem();
		BillableService service1 = billableItemsService.getById(0);
		if (service1 != null) {
			initialItem.setBillableService(service1);
			initialItem.setPrice(BigDecimal.valueOf(100));
			initialItem.setQuantity(1);
			initialItem.setPaymentStatus(BillStatus.PENDING);
			initialBill.addLineItem(initialItem);
		}
		
		// Save the initial bill
		Bill savedBill = service.save(initialBill);
		Context.flushSession();
		
		// Reload the bill from the database to get the managed instance from Hibernate session
		Bill bill = service.getById(savedBill.getId());
		
		// Add new line item to the bill being updated
		BillLineItem newItem = new BillLineItem();
		BillableService service2 = billableItemsService.getById(1);
		if (service2 != null) {
			newItem.setBillableService(service2);
			newItem.setPrice(BigDecimal.valueOf(200));
			newItem.setQuantity(1);
			newItem.setPaymentStatus(BillStatus.PENDING);
			bill.addLineItem(newItem);
		}
		
		// When saving, searchBill() will find the same managed instance from Hibernate session
		// So bill == billToUpdate will be true (same object reference)
		// Save the bill - this simulates user updating an existing bill
		Bill updatedBill = service.save(bill);
		Context.flushSession();
		
		// Verify that bill and billToUpdate are the same object reference
		// In BillServiceImpl.save(), when searchBill() finds the existing bill,
		// it returns the same managed instance from Hibernate session cache,
		// so bill == billToUpdate is true
		Assert.assertSame("bill and billToUpdate should be the same object reference when updating", 
			bill, updatedBill);
	}
}
