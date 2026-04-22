package org.openmrs.module.billing.api;

import org.openmrs.annotation.Authorized;
import org.openmrs.module.billing.api.model.BillDiscount;
import org.openmrs.module.billing.api.util.PrivilegeConstants;

public interface BillDiscountService {
	
	@Authorized(PrivilegeConstants.VIEW_BILL_DISCOUNTS)
	BillDiscount getBillDiscountById(Integer id);
	
	@Authorized(PrivilegeConstants.VIEW_BILL_DISCOUNTS)
	BillDiscount getBillDiscountByUuid(String uuid);
	
	@Authorized(PrivilegeConstants.VIEW_BILL_DISCOUNTS)
	BillDiscount getBillDiscountByBillId(Integer billId);

	@Authorized(PrivilegeConstants.MANAGE_BILL_DISCOUNTS)
	BillDiscount saveBillDiscount(BillDiscount billDiscount);
}
