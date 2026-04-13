package org.openmrs.module.billing.api;

import org.openmrs.module.billing.api.model.BillDiscount;

public interface BillDiscountService {
	
	BillDiscount saveBillDiscount(BillDiscount billDiscount);
	
	BillDiscount getBillDiscountById(Integer id);
	
	BillDiscount getBillDiscountByUuid(String uuid);
	
	BillDiscount getBillDiscountByBillId(Integer billId);
}
