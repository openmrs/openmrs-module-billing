package org.openmrs.module.billing.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Order;
import org.openmrs.module.billing.api.model.*;
import org.openmrs.module.stockmanagement.api.model.StockItem;

class BillLineItemTest {

    @InjectMocks
    private BillLineItem billLineItem;

    @Mock
    private Bill bill;

    @Mock
    private StockItem stockItem;

    @Mock
    private BillableService billableService;

    @Mock
    private CashierItemPrice itemPrice;

    @Mock
    private Order order;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        billLineItem = new BillLineItem();
    }

    @Test
    void setId_ShouldSetIdCorrectly() {
        billLineItem.setId(1);
        assertEquals(1, billLineItem.getId());
    }

    @Test
    void setBill_ShouldSetBillCorrectly() {
        billLineItem.setBill(bill);
        assertEquals(bill, billLineItem.getBill());
    }

    @Test
    void setItem_ShouldSetItemCorrectly() {
        billLineItem.setItem(stockItem);
        assertEquals(stockItem, billLineItem.getItem());
    }

    @Test
    void setBillableService_ShouldSetBillableServiceCorrectly() {
        billLineItem.setBillableService(billableService);
        assertEquals(billableService, billLineItem.getBillableService());
    }

    @Test
    void setPrice_ShouldSetPriceCorrectly() {
        BigDecimal price = BigDecimal.valueOf(100.50);
        billLineItem.setPrice(price);
        assertEquals(price, billLineItem.getPrice());
    }

    @Test
    void setPriceName_ShouldSetPriceNameCorrectly() {
        String priceName = "Standard";
        billLineItem.setPriceName(priceName);
        assertEquals(priceName, billLineItem.getPriceName());
    }

    @Test
    void setItemPrice_ShouldSetItemPriceCorrectly() {
        billLineItem.setItemPrice(itemPrice);
        assertEquals(itemPrice, billLineItem.getItemPrice());
    }

    @Test
    void setQuantity_ShouldSetQuantityCorrectly() {
        Integer quantity = 10;
        billLineItem.setQuantity(quantity);
        assertEquals(quantity, billLineItem.getQuantity());
    }

    @Test
    void setLineItemOrder_ShouldSetLineItemOrderCorrectly() {
        Integer lineItemOrder = 1;
        billLineItem.setLineItemOrder(lineItemOrder);
        assertEquals(lineItemOrder, billLineItem.getLineItemOrder());
    }

    @Test
    void setPaymentStatus_ShouldSetPaymentStatusCorrectly() {
        BillStatus paymentStatus = BillStatus.PAID;
        billLineItem.setPaymentStatus(paymentStatus);
        assertEquals(paymentStatus, billLineItem.getPaymentStatus());
    }

    @Test
    void setOrder_ShouldSetOrderCorrectly() {
        billLineItem.setOrder(order);
        assertEquals(order, billLineItem.getOrder());
    }

    @Test
    void getTotal_ShouldReturnCorrectTotal() {
        BigDecimal price = BigDecimal.valueOf(100.50);
        Integer quantity = 10;
        billLineItem.setPrice(price);
        billLineItem.setQuantity(quantity);
        BigDecimal expectedTotal = price.multiply(BigDecimal.valueOf(quantity));
        assertEquals(expectedTotal, billLineItem.getTotal());
    }
}
