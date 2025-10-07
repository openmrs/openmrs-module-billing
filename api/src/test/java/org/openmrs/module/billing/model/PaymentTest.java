package org.openmrs.module.billing.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.module.billing.api.model.PaymentAttribute;
import org.openmrs.module.billing.api.model.PaymentMode;
import org.openmrs.module.billing.api.model.PaymentModeAttributeType;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.Payment;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentTest {

    private Payment payment;

    @BeforeEach
    public void setUp() {
        payment = new Payment();
    }

    @Test
    public void shouldGetAndSetId() {
        Integer id = 12345;
        
        payment.setId(id);
        
        assertEquals(id, payment.getId());
    }

    @Test
    public void shouldGetAndSetAmount() {
        BigDecimal amount = new BigDecimal("100.00");
        
        payment.setAmount(amount);
        
        assertEquals(amount, payment.getAmount());
    }

    @Test
    public void shouldGetAndSetAmountTendered() {
        BigDecimal amountTendered = new BigDecimal("150.00");
        
        payment.setAmountTendered(amountTendered);
        
        assertEquals(amountTendered, payment.getAmountTendered());
    }

    @Test
    public void shouldGetAndSetBill() {
        Bill bill = new Bill();
        
        payment.setBill(bill);
        
        assertEquals(bill, payment.getBill());
    }

    @Test
    public void shouldAddAttribute() {
        PaymentModeAttributeType type = new PaymentModeAttributeType();
        type.setName("Test Type");
        String value = "Test Value";

        PaymentAttribute attribute = payment.addAttribute(type, value);

        assertEquals(type, attribute.getAttributeType());
        assertEquals(value, attribute.getValue());
        assertTrue(payment.getAttributes().contains(attribute));
    }

    @Test
    public void shouldThrowExceptionWhenAttributeTypeIsNull() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            payment.addAttribute(null, "Test Value");
        });

        assertEquals("The payment mode attribute type must be defined.", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenAttributeValueIsNull() {
        PaymentModeAttributeType type = new PaymentModeAttributeType();
        type.setName("Test Type");

        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            payment.addAttribute(type, null);
        });

        assertEquals("The payment attribute value must be defined.", exception.getMessage());
    }
    
    @Test
    public void shouldReturnNullForUnsetAmount() {
        assertNull(payment.getAmount());
    }

    @Test
    public void shouldReturnNullForUnsetAmountTendered() {
        assertNull(payment.getAmountTendered());
    }

    @Test
    public void shouldReturnNullForUnsetBill() {
        assertNull(payment.getBill());
    }

    @Test
    public void shouldReturnNullForUnsetId() {
        assertNull(payment.getId());
    }
}
