package org.openmrs.module.billing.api;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.search.BillSearch;
import org.openmrs.module.billing.api.util.PrivilegeConstants;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BillService extends OpenmrsService {
	
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_BILLS)
	Bill getBill(Integer id);
	
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_BILLS)
	Bill getBillByUuid(String uuid);
	
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_BILLS)
	Bill getBillByReceiptNumber(String receiptNumber);
	
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_BILLS)
	List<Bill> getBillsByPatientUuid(String patientUuid, PagingInfo pagingInfo);
	
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_BILLS)
	List<Bill> getBills(BillSearch billSearch, PagingInfo pagingInfo);
	
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.VIEW_BILLS)
	byte[] downloadBillReceipt(Bill bill);
	
	@Transactional
	@Authorized(PrivilegeConstants.MANAGE_BILLS)
	Bill saveBill(Bill bill);
	
	@Authorized(PrivilegeConstants.PURGE_BILLS)
	void purgeBill(Bill bill);
	
	@Authorized(PrivilegeConstants.DELETE_BILLS)
	Bill voidBill(Bill bill, String voidReason);
	
	@Authorized(PrivilegeConstants.DELETE_BILLS)
	Bill unvoidBill(Bill bill);
	
}
