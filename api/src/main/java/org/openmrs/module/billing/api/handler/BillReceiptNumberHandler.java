/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
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
