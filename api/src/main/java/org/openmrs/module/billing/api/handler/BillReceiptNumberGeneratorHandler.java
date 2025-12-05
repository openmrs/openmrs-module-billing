package org.openmrs.module.billing.api.handler;

import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.api.handler.SaveHandler;
import org.openmrs.module.billing.api.IReceiptNumberGenerator;
import org.openmrs.module.billing.api.ReceiptNumberGeneratorFactory;
import org.openmrs.module.billing.api.model.Bill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@Handler(supports = { Bill.class })
public class BillReceiptNumberGeneratorHandler implements SaveHandler<Bill> {

    private static final Logger LOG = LoggerFactory.getLogger(BillReceiptNumberGeneratorHandler.class);

    @Override
    public void handle(Bill bill, User user, Date date, String other) {
        if (bill.getReceiptNumber() == null) {
            IReceiptNumberGenerator generator = ReceiptNumberGeneratorFactory.getGenerator();
            if (generator == null) {
                LOG.warn("Cannot generate receipt as no receipt number generator is defined.");
            } else {
                bill.setReceiptNumber(generator.generateNumber(bill));
            }
        }
    }
}
