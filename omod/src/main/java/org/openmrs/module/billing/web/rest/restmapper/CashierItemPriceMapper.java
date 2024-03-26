package org.openmrs.module.billing.web.rest.restmapper;

import java.math.BigDecimal;

public class CashierItemPriceMapper {
    private String name;
    private BigDecimal price;
    private String paymentMode;

    public CashierItemPriceMapper() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }
}
