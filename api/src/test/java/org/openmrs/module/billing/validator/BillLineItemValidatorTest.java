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

package org.openmrs.module.billing.validator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests for {@link BillLineItemValidator}
 */
public class BillLineItemValidatorTest extends BaseModuleContextSensitiveTest {
	
	private BillLineItemValidator billLineItemValidator;
	
	@BeforeEach
	public void setup() throws Exception {
		billLineItemValidator = new BillLineItemValidator();
	}
	
	@Test
	public void validate_shouldRejectWhenLineItemUuidIsNull() {
		BillLineItemValidator.VoidRequest request = new BillLineItemValidator.VoidRequest(null, "Test reason");
		
		Errors errors = new BindException(request, "request");
		billLineItemValidator.validate(request, errors);
		
		assertTrue(errors.hasErrors());
		assertTrue(errors.hasFieldErrors("lineItemUuid"));
		assertEquals("billing.error.lineItemUuidRequired", errors.getFieldError("lineItemUuid").getCode());
	}
	
	@Test
	public void validate_shouldRejectWhenLineItemUuidIsEmpty() {
		BillLineItemValidator.VoidRequest request = new BillLineItemValidator.VoidRequest("", "Test reason");
		
		Errors errors = new BindException(request, "request");
		billLineItemValidator.validate(request, errors);
		
		assertTrue(errors.hasErrors());
		assertTrue(errors.hasFieldErrors("lineItemUuid"));
		assertEquals("billing.error.lineItemUuidRequired", errors.getFieldError("lineItemUuid").getCode());
	}
	
	@Test
	public void validate_shouldRejectWhenVoidReasonIsNull() {
		BillLineItemValidator.VoidRequest request = new BillLineItemValidator.VoidRequest("some-uuid", null);
		
		Errors errors = new BindException(request, "request");
		billLineItemValidator.validate(request, errors);
		
		assertTrue(errors.hasErrors());
		assertTrue(errors.hasFieldErrors("voidReason"));
		assertEquals("billing.error.voidReasonRequired", errors.getFieldError("voidReason").getCode());
	}
	
	@Test
	public void validate_shouldRejectWhenVoidReasonIsEmpty() {
		BillLineItemValidator.VoidRequest request = new BillLineItemValidator.VoidRequest("some-uuid", "");
		
		Errors errors = new BindException(request, "request");
		billLineItemValidator.validate(request, errors);
		
		assertTrue(errors.hasErrors());
		assertTrue(errors.hasFieldErrors("voidReason"));
		assertEquals("billing.error.voidReasonRequired", errors.getFieldError("voidReason").getCode());
	}
	
	@Test
	public void validate_shouldRejectWhenBothLineItemUuidAndVoidReasonAreNull() {
		BillLineItemValidator.VoidRequest request = new BillLineItemValidator.VoidRequest(null, null);
		
		Errors errors = new BindException(request, "request");
		billLineItemValidator.validate(request, errors);
		
		assertTrue(errors.hasErrors());
		assertTrue(errors.hasFieldErrors("lineItemUuid"));
		assertTrue(errors.hasFieldErrors("voidReason"));
		assertEquals(2, errors.getErrorCount());
	}
	
	@Test
	public void validate_shouldNotRejectWhenBothFieldsAreValid() {
		BillLineItemValidator.VoidRequest request = new BillLineItemValidator.VoidRequest("valid-uuid", "Valid reason");
		
		Errors errors = new BindException(request, "request");
		billLineItemValidator.validate(request, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldNotRejectWhenTargetIsBillLineItem() {
		BillLineItem lineItem = new BillLineItem();
		
		Errors errors = new BindException(lineItem, "billLineItem");
		billLineItemValidator.validate(lineItem, errors);
		
		assertFalse(errors.hasErrors());
	}
	
}
