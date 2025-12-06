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
package org.openmrs.module.billing.api.validator;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.base.BaseModuleContextTest;
import org.openmrs.module.stockmanagement.api.model.StockItem;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for {@link BillLineItemValidator}.
 */
public class BillLineItemValidatorTest extends BaseModuleContextTest {
	
	private BillLineItemValidator validator;
	
	@Before
	public void setUp() {
		validator = new BillLineItemValidator();
	}
	
	/**
	 * Test validation of a valid BillLineItem with all required fields. Should pass with no errors.
	 */
	@Test
	public void testValidBillLineItem() {
		BillLineItem billLineItem = new BillLineItem();
		
		// Set valid properties
		StockItem item = new StockItem();
		billLineItem.setItem(item);
		billLineItem.setQuantity(1);
		billLineItem.setPrice(BigDecimal.valueOf(100.0));
		billLineItem.setPaymentStatus(BillStatus.PENDING);
		
		Errors errors = new BindException(billLineItem, "billLineItem");
		
		validator.validate(billLineItem, errors);
		
		assertFalse("Should not have validation errors", errors.hasErrors());
		assertEquals("Should have zero errors", 0, errors.getErrorCount());
	}
	
	/**
	 * Test validation when both item and billableService are null. Should fail with error code
	 * "billing.billLineItem.itemOrServiceRequired".
	 */
	@Test
	public void testBillLineItemWithNullItemAndService() {
		BillLineItem billLineItem = new BillLineItem();
		
		// Both item and billableService are null
		billLineItem.setItem(null);
		billLineItem.setBillableService(null);
		billLineItem.setQuantity(1);
		billLineItem.setPrice(BigDecimal.valueOf(100.0));
		billLineItem.setPaymentStatus(BillStatus.PENDING);
		
		Errors errors = new BindException(billLineItem, "billLineItem");
		
		validator.validate(billLineItem, errors);
		
		assertTrue("Should have validation errors", errors.hasErrors());
		assertNotNull("Should have field error for item", errors.getFieldError("item"));
		assertEquals("Should have correct error code", "billing.billLineItem.itemOrServiceRequired",
		    errors.getFieldError("item").getCode());
	}
	
	/**
	 * Test validation when quantity is zero. Should fail with error code
	 * "billing.billLineItem.quantityInvalid".
	 */
	@Test
	public void testBillLineItemWithZeroQuantity() {
		BillLineItem billLineItem = new BillLineItem();
		
		StockItem item = new StockItem();
		billLineItem.setItem(item);
		billLineItem.setQuantity(0);
		billLineItem.setPrice(BigDecimal.valueOf(100.0));
		billLineItem.setPaymentStatus(BillStatus.PENDING);
		
		Errors errors = new BindException(billLineItem, "billLineItem");
		
		validator.validate(billLineItem, errors);
		
		assertTrue("Should have validation errors", errors.hasErrors());
		assertNotNull("Should have field error for quantity", errors.getFieldError("quantity"));
		assertEquals("Should have correct error code", "billing.billLineItem.quantityInvalid",
		    errors.getFieldError("quantity").getCode());
	}
	
	/**
	 * Test validation when quantity is negative. Should fail with error code
	 * "billing.billLineItem.quantityInvalid".
	 */
	@Test
	public void testBillLineItemWithNegativeQuantity() {
		BillLineItem billLineItem = new BillLineItem();
		
		StockItem item = new StockItem();
		billLineItem.setItem(item);
		billLineItem.setQuantity(-5);
		billLineItem.setPrice(BigDecimal.valueOf(100.0));
		billLineItem.setPaymentStatus(BillStatus.PENDING);
		
		Errors errors = new BindException(billLineItem, "billLineItem");
		
		validator.validate(billLineItem, errors);
		
		assertTrue("Should have validation errors", errors.hasErrors());
		assertNotNull("Should have field error for quantity", errors.getFieldError("quantity"));
		assertEquals("Should have correct error code", "billing.billLineItem.quantityInvalid",
		    errors.getFieldError("quantity").getCode());
	}
	
	/**
	 * Test validation when price is negative. Should fail with error code
	 * "billing.billLineItem.priceInvalid".
	 */
	@Test
	public void testBillLineItemWithNegativePrice() {
		BillLineItem billLineItem = new BillLineItem();
		
		StockItem item = new StockItem();
		billLineItem.setItem(item);
		billLineItem.setQuantity(1);
		billLineItem.setPrice(BigDecimal.valueOf(-100.0));
		billLineItem.setPaymentStatus(BillStatus.PENDING);
		
		Errors errors = new BindException(billLineItem, "billLineItem");
		
		validator.validate(billLineItem, errors);
		
		assertTrue("Should have validation errors", errors.hasErrors());
		assertNotNull("Should have field error for price", errors.getFieldError("price"));
		assertEquals("Should have correct error code", "billing.billLineItem.priceInvalid",
		    errors.getFieldError("price").getCode());
	}
	
	/**
	 * Test validation when payment status is an invalid value (not PENDING or PAID). Should fail with
	 * error code "billing.billLineItem.paymentStatusInvalid".
	 */
	@Test
	public void testBillLineItemWithInvalidPaymentStatus() {
		BillLineItem billLineItem = new BillLineItem();
		
		StockItem item = new StockItem();
		billLineItem.setItem(item);
		billLineItem.setQuantity(1);
		billLineItem.setPrice(BigDecimal.valueOf(100.0));
		// Set an invalid payment status (not PENDING or PAID)
		billLineItem.setPaymentStatus(BillStatus.POSTED);
		
		Errors errors = new BindException(billLineItem, "billLineItem");
		
		validator.validate(billLineItem, errors);
		
		assertTrue("Should have validation errors", errors.hasErrors());
		assertNotNull("Should have field error for paymentStatus", errors.getFieldError("paymentStatus"));
		assertEquals("Should have correct error code", "billing.billLineItem.paymentStatusInvalid",
		    errors.getFieldError("paymentStatus").getCode());
	}
	
	/**
	 * Test validation when a field exceeds maximum length. Should fail with error code
	 * "billing.billLineItem.fieldTooLong".
	 */
	@Test
	public void testBillLineItemFieldLengthViolation() {
		BillLineItem billLineItem = new BillLineItem();
		
		StockItem item = new StockItem();
		billLineItem.setItem(item);
		billLineItem.setQuantity(1);
		billLineItem.setPrice(BigDecimal.valueOf(100.0));
		billLineItem.setPaymentStatus(BillStatus.PENDING);
		
		// Create a string that exceeds the maximum length of 255 characters
		StringBuilder longPriceName = new StringBuilder();
		for (int i = 0; i < 260; i++) {
			longPriceName.append("a");
		}
		billLineItem.setPriceName(longPriceName.toString());
		
		Errors errors = new BindException(billLineItem, "billLineItem");
		
		validator.validate(billLineItem, errors);
		
		assertTrue("Should have validation errors", errors.hasErrors());
		assertNotNull("Should have field error for priceName", errors.getFieldError("priceName"));
		assertEquals("Should have correct error code", "billing.billLineItem.fieldTooLong",
		    errors.getFieldError("priceName").getCode());
	}
	
	/**
	 * Test validation with billableService instead of item. Should pass with no errors.
	 */
	@Test
	public void testValidBillLineItemWithBillableService() {
		BillLineItem billLineItem = new BillLineItem();
		
		// Set billableService instead of item
		BillableService service = new BillableService();
		billLineItem.setBillableService(service);
		billLineItem.setQuantity(1);
		billLineItem.setPrice(BigDecimal.valueOf(100.0));
		billLineItem.setPaymentStatus(BillStatus.PAID);
		
		Errors errors = new BindException(billLineItem, "billLineItem");
		
		validator.validate(billLineItem, errors);
		
		assertFalse("Should not have validation errors", errors.hasErrors());
		assertEquals("Should have zero errors", 0, errors.getErrorCount());
	}
	
	/**
	 * Test validation when quantity is null. Should fail with error code
	 * "billing.billLineItem.quantityInvalid".
	 */
	@Test
	public void testBillLineItemWithNullQuantity() {
		BillLineItem billLineItem = new BillLineItem();
		
		StockItem item = new StockItem();
		billLineItem.setItem(item);
		billLineItem.setQuantity(null);
		billLineItem.setPrice(BigDecimal.valueOf(100.0));
		billLineItem.setPaymentStatus(BillStatus.PENDING);
		
		Errors errors = new BindException(billLineItem, "billLineItem");
		
		validator.validate(billLineItem, errors);
		
		assertTrue("Should have validation errors", errors.hasErrors());
		assertNotNull("Should have field error for quantity", errors.getFieldError("quantity"));
		assertEquals("Should have correct error code", "billing.billLineItem.quantityInvalid",
		    errors.getFieldError("quantity").getCode());
	}
	
	/**
	 * Test that validator supports BillLineItem class.
	 */
	@Test
	public void testSupportsMethod() {
		assertTrue("Validator should support BillLineItem class", validator.supports(BillLineItem.class));
		assertFalse("Validator should not support other classes", validator.supports(Object.class));
	}
	
	/**
	 * Test validation with valid price as zero (free item). Should pass with no errors.
	 */
	@Test
	public void testBillLineItemWithZeroPrice() {
		BillLineItem billLineItem = new BillLineItem();
		
		StockItem item = new StockItem();
		billLineItem.setItem(item);
		billLineItem.setQuantity(1);
		billLineItem.setPrice(BigDecimal.ZERO);
		billLineItem.setPaymentStatus(BillStatus.PENDING);
		
		Errors errors = new BindException(billLineItem, "billLineItem");
		
		validator.validate(billLineItem, errors);
		
		assertFalse("Should not have validation errors for zero price", errors.hasErrors());
	}
}
