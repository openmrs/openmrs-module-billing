package org.openmrs.module.billing.api.db;

import org.openmrs.module.billing.api.model.BillDiscount;

public interface BillDiscountDAO {
	
	BillDiscount saveBillDiscount(BillDiscount billDiscount);
	
	BillDiscount getBillDiscountById(Integer id);
	
	BillDiscount getBillDiscountByUuid(String uuid);
	
	BillDiscount getBillDiscountByBillId(Integer billId);
}
