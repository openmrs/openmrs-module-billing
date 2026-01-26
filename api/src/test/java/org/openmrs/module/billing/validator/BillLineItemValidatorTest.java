package org.openmrs.module.billing.validator;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.stockmanagement.api.model.StockItem;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

public class BillLineItemValidatorTest {
	
	private BillLineItemValidator validator;
	
	@BeforeEach
	public void setup() {
		validator = new BillLineItemValidator();
	}
	
	@Test
	public void validate_shouldPassForValidLineItem() {
		BillLineItem lineItem = new BillLineItem();
		lineItem.setItem(new StockItem());
		lineItem.setQuantity(1);
		lineItem.setPrice(BigDecimal.TEN);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		
		Errors errors = new BindException(lineItem, "lineItem");
		validator.validate(lineItem, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldPassWhenBillableServiceIsSet() {
		BillLineItem lineItem = new BillLineItem();
		lineItem.setBillableService(new BillableService());
		lineItem.setQuantity(1);
		lineItem.setPrice(BigDecimal.TEN);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		
		Errors errors = new BindException(lineItem, "lineItem");
		validator.validate(lineItem, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldRejectWhenBothItemAndServiceAreNull() {
		BillLineItem lineItem = new BillLineItem();
		lineItem.setQuantity(1);
		lineItem.setPrice(BigDecimal.TEN);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		
		Errors errors = new BindException(lineItem, "lineItem");
		validator.validate(lineItem, errors);
		
		assertTrue(errors.hasErrors());
		assertTrue(errors.hasFieldErrors("item"));
	}
	
	@Test
	public void validate_shouldRejectWhenQuantityIsNull() {
		BillLineItem lineItem = new BillLineItem();
		lineItem.setItem(new StockItem());
		lineItem.setPrice(BigDecimal.TEN);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		
		Errors errors = new BindException(lineItem, "lineItem");
		validator.validate(lineItem, errors);
		
		assertTrue(errors.hasErrors());
		assertTrue(errors.hasFieldErrors("quantity"));
	}
	
	@Test
	public void validate_shouldRejectWhenQuantityIsZero() {
		BillLineItem lineItem = new BillLineItem();
		lineItem.setItem(new StockItem());
		lineItem.setQuantity(0);
		lineItem.setPrice(BigDecimal.TEN);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		
		Errors errors = new BindException(lineItem, "lineItem");
		validator.validate(lineItem, errors);
		
		assertTrue(errors.hasErrors());
		assertTrue(errors.hasFieldErrors("quantity"));
	}
	
	@Test
	public void validate_shouldRejectWhenQuantityIsNegative() {
		BillLineItem lineItem = new BillLineItem();
		lineItem.setItem(new StockItem());
		lineItem.setQuantity(-1);
		lineItem.setPrice(BigDecimal.TEN);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		
		Errors errors = new BindException(lineItem, "lineItem");
		validator.validate(lineItem, errors);
		
		assertTrue(errors.hasErrors());
		assertTrue(errors.hasFieldErrors("quantity"));
	}
	
	@Test
	public void validate_shouldRejectWhenPriceIsNull() {
		BillLineItem lineItem = new BillLineItem();
		lineItem.setItem(new StockItem());
		lineItem.setQuantity(1);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		
		Errors errors = new BindException(lineItem, "lineItem");
		validator.validate(lineItem, errors);
		
		assertTrue(errors.hasErrors());
		assertTrue(errors.hasFieldErrors("price"));
	}
	
	@Test
	public void validate_shouldRejectWhenPriceIsNegative() {
		BillLineItem lineItem = new BillLineItem();
		lineItem.setItem(new StockItem());
		lineItem.setQuantity(1);
		lineItem.setPrice(BigDecimal.valueOf(-10));
		lineItem.setPaymentStatus(BillStatus.PENDING);
		
		Errors errors = new BindException(lineItem, "lineItem");
		validator.validate(lineItem, errors);
		
		assertTrue(errors.hasErrors());
		assertTrue(errors.hasFieldErrors("price"));
	}
	
	@Test
	public void validate_shouldPassWhenPriceIsZero() {
		BillLineItem lineItem = new BillLineItem();
		lineItem.setItem(new StockItem());
		lineItem.setQuantity(1);
		lineItem.setPrice(BigDecimal.ZERO);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		
		Errors errors = new BindException(lineItem, "lineItem");
		validator.validate(lineItem, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldRejectWhenPaymentStatusIsNull() {
		BillLineItem lineItem = new BillLineItem();
		lineItem.setItem(new StockItem());
		lineItem.setQuantity(1);
		lineItem.setPrice(BigDecimal.TEN);
		
		Errors errors = new BindException(lineItem, "lineItem");
		validator.validate(lineItem, errors);
		
		assertTrue(errors.hasErrors());
		assertTrue(errors.hasFieldErrors("paymentStatus"));
	}
	
	@Test
	public void validate_shouldRejectWhenPaymentStatusIsNotPendingOrPaid() {
		BillLineItem lineItem = new BillLineItem();
		lineItem.setItem(new StockItem());
		lineItem.setQuantity(1);
		lineItem.setPrice(BigDecimal.TEN);
		lineItem.setPaymentStatus(BillStatus.POSTED);
		
		Errors errors = new BindException(lineItem, "lineItem");
		validator.validate(lineItem, errors);
		
		assertTrue(errors.hasErrors());
		assertTrue(errors.hasFieldErrors("paymentStatus"));
	}
	
	@Test
	public void validate_shouldPassWhenPaymentStatusIsPending() {
		BillLineItem lineItem = new BillLineItem();
		lineItem.setItem(new StockItem());
		lineItem.setQuantity(1);
		lineItem.setPrice(BigDecimal.TEN);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		
		Errors errors = new BindException(lineItem, "lineItem");
		validator.validate(lineItem, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldPassWhenPaymentStatusIsPaid() {
		BillLineItem lineItem = new BillLineItem();
		lineItem.setItem(new StockItem());
		lineItem.setQuantity(1);
		lineItem.setPrice(BigDecimal.TEN);
		lineItem.setPaymentStatus(BillStatus.PAID);
		
		Errors errors = new BindException(lineItem, "lineItem");
		validator.validate(lineItem, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldRejectWhenPriceNameExceeds255Characters() {
		BillLineItem lineItem = new BillLineItem();
		lineItem.setItem(new StockItem());
		lineItem.setQuantity(1);
		lineItem.setPrice(BigDecimal.TEN);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		lineItem.setPriceName(org.apache.commons.lang3.StringUtils.repeat("a", 256));
		
		Errors errors = new BindException(lineItem, "lineItem");
		validator.validate(lineItem, errors);
		
		assertTrue(errors.hasErrors());
		assertTrue(errors.hasFieldErrors("priceName"));
	}
	
	@Test
	public void validate_shouldPassWhenPriceNameIs255Characters() {
		BillLineItem lineItem = new BillLineItem();
		lineItem.setItem(new StockItem());
		lineItem.setQuantity(1);
		lineItem.setPrice(BigDecimal.TEN);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		lineItem.setPriceName(org.apache.commons.lang3.StringUtils.repeat("a", 255));
		
		Errors errors = new BindException(lineItem, "lineItem");
		validator.validate(lineItem, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldRejectVoidedLineItemWithoutVoidReason() {
		BillLineItem lineItem = new BillLineItem();
		lineItem.setItem(new StockItem());
		lineItem.setQuantity(1);
		lineItem.setPrice(BigDecimal.TEN);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		lineItem.setVoided(true);
		
		Errors errors = new BindException(lineItem, "lineItem");
		validator.validate(lineItem, errors);
		
		assertTrue(errors.hasErrors());
		assertTrue(errors.hasFieldErrors("voidReason"));
	}
	
	@Test
	public void validate_shouldPassVoidedLineItemWithVoidReason() {
		BillLineItem lineItem = new BillLineItem();
		lineItem.setItem(new StockItem());
		lineItem.setQuantity(1);
		lineItem.setPrice(BigDecimal.TEN);
		lineItem.setPaymentStatus(BillStatus.PENDING);
		lineItem.setVoided(true);
		lineItem.setVoidReason("Test void reason");
		
		Errors errors = new BindException(lineItem, "lineItem");
		validator.validate(lineItem, errors);
		
		assertFalse(errors.hasErrors());
	}
}
