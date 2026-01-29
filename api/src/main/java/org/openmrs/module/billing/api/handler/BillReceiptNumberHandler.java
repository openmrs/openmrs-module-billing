package org.openmrs.module.billing.api.handler;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.api.handler.SaveHandler;
import org.openmrs.module.billing.api.IReceiptNumberGenerator;
import org.openmrs.module.billing.api.ReceiptNumberGeneratorFactory;
import org.openmrs.module.billing.api.model.Bill;

import java.util.Date;

@Handler(supports = Bill.class, order = 1000)
public class BillReceiptNumberHandler implements SaveHandler<Bill> {
	
	@Override
	public void handle(Bill bill, User user, Date date, String s) {
		if (StringUtils.isEmpty(bill.getReceiptNumber())) {
			IReceiptNumberGenerator receiptNumberGenerator = ReceiptNumberGeneratorFactory.getGenerator();
			if (receiptNumberGenerator != null) {
				bill.setReceiptNumber(receiptNumberGenerator.generateNumber(bill));
			}
		}
	}
}
