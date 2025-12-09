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
package org.openmrs.module.billing.api.model;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link BillLineItem} class.
 */
public class BillLineItemTest {
	
	private BillLineItem billLineItem;
	
	@Before
	public void setUp() {
		billLineItem = new BillLineItem();
	}
	
	/**
	 * @verifies throw IllegalStateException when price is null
	 * @see BillLineItem#getTotal()
	 */
	@Test(expected = IllegalStateException.class)
	public void testGetTotal_WithNullPrice_ThrowsException() {
		billLineItem.setPrice(null);
		billLineItem.setQuantity(5);
		
		billLineItem.getTotal();
	}
	
	/**
	 * @verifies throw IllegalStateException when quantity is null
	 * @see BillLineItem#getTotal()
	 */
	@Test(expected = IllegalStateException.class)
	public void testGetTotal_WithNullQuantity_ThrowsException() {
		billLineItem.setPrice(BigDecimal.valueOf(10.00));
		billLineItem.setQuantity(null);
		
		billLineItem.getTotal();
	}
	
	/**
	 * @verifies throw IllegalStateException when both price and quantity are null
	 * @see BillLineItem#getTotal()
	 */
	@Test(expected = IllegalStateException.class)
	public void testGetTotal_WithBothNull_ThrowsException() {
		billLineItem.setPrice(null);
		billLineItem.setQuantity(null);
		
		billLineItem.getTotal();
	}
	
	/**
	 * @verifies return correct total when both price and quantity are valid
	 * @see BillLineItem#getTotal()
	 */
	@Test
	public void testGetTotal_WithValidValues_CalculatesCorrectly() {
		billLineItem.setPrice(BigDecimal.valueOf(10.50));
		billLineItem.setQuantity(3);
		
		BigDecimal expectedTotal = BigDecimal.valueOf(31.50);
		BigDecimal actualTotal = billLineItem.getTotal();
		
		Assert.assertEquals(0, expectedTotal.compareTo(actualTotal));
	}
	
	/**
	 * @verifies include descriptive error message identifying which field is null
	 * @see BillLineItem#getTotal()
	 */
	@Test
	public void testGetTotal_ErrorMessageIdentifiesProblem() {
		// Test null price message
		billLineItem.setPrice(null);
		billLineItem.setQuantity(5);
		
		try {
			billLineItem.getTotal();
			Assert.fail("Expected IllegalStateException to be thrown");
		}
		catch (IllegalStateException e) {
			Assert.assertTrue("Error message should contain required text",
			    e.getMessage().contains("Cannot calculate total: price and quantity must not be null"));
			Assert.assertTrue("Error message should identify price as null", e.getMessage().contains("price is"));
		}
		
		// Test null quantity message
		billLineItem.setPrice(BigDecimal.valueOf(10.00));
		billLineItem.setQuantity(null);
		
		try {
			billLineItem.getTotal();
			Assert.fail("Expected IllegalStateException to be thrown");
		}
		catch (IllegalStateException e) {
			Assert.assertTrue("Error message should contain required text",
			    e.getMessage().contains("Cannot calculate total: price and quantity must not be null"));
			Assert.assertTrue("Error message should identify quantity as null", e.getMessage().contains("quantity is"));
		}
		
		// Test both null message
		billLineItem.setPrice(null);
		billLineItem.setQuantity(null);
		
		try {
			billLineItem.getTotal();
			Assert.fail("Expected IllegalStateException to be thrown");
		}
		catch (IllegalStateException e) {
			Assert.assertTrue("Error message should contain required text",
			    e.getMessage().contains("Cannot calculate total: price and quantity must not be null"));
			Assert.assertTrue("Error message should identify both as null",
			    e.getMessage().contains("price and quantity are"));
		}
	}
	
	/**
	 * @verifies not affect existing code that properly initializes price and quantity
	 * @see BillLineItem#getTotal()
	 */
	@Test
	public void testGetTotal_ExistingCode_UnaffectedByFix() {
		// Simulate typical usage pattern from existing code
		billLineItem.setPrice(BigDecimal.valueOf(25.00));
		billLineItem.setQuantity(2);
		
		BigDecimal total = billLineItem.getTotal();
		Assert.assertNotNull("Total should not be null", total);
		Assert.assertEquals(0, BigDecimal.valueOf(50.00).compareTo(total));
		
		// Test with zero values (edge case)
		billLineItem.setPrice(BigDecimal.ZERO);
		billLineItem.setQuantity(5);
		Assert.assertEquals(0, BigDecimal.ZERO.compareTo(billLineItem.getTotal()));
		
		billLineItem.setPrice(BigDecimal.valueOf(10.00));
		billLineItem.setQuantity(0);
		Assert.assertEquals(0, BigDecimal.ZERO.compareTo(billLineItem.getTotal()));
		
		// Test with decimal precision
		billLineItem.setPrice(new BigDecimal("19.99"));
		billLineItem.setQuantity(4);
		BigDecimal expected = new BigDecimal("79.96");
		Assert.assertEquals(0, expected.compareTo(billLineItem.getTotal()));
	}
}
