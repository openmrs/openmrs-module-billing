package org.openmrs.module.billing.api.db;

import org.openmrs.module.billing.api.model.BillDiscount;

public interface BillDiscountDAO {

	BillDiscount getBillDiscountById(Integer id);
	
	BillDiscount getBillDiscountByUuid(String uuid);
	
	BillDiscount getBillDiscountByBillId(Integer billId);

	BillDiscount saveBillDiscount(BillDiscount billDiscount);
}
