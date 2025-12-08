package org.openmrs.module.billing.api.db;

import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.search.BillSearch;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;

public interface BillDAO {
	
	@Transactional(readOnly = true)
	Bill getBill(@Nonnull Integer id);
	
	@Transactional(readOnly = true)
	Bill getBillByUuid(@Nonnull String uuid);
	
	@Transactional
	Bill saveBill(@Nonnull Bill bill);
	
	@Transactional(readOnly = true)
	Bill getBillByReceiptNumber(@Nonnull String receiptNumber);
	
	@Transactional(readOnly = true)
	List<Bill> getBillsByPatientUuid(@Nonnull String patientUuid, PagingInfo pagingInfo);
	
	@Transactional(readOnly = true)
	List<Bill> getBills(@Nonnull BillSearch billSearch, PagingInfo pagingInfo);
	
	@Transactional
	void purgeBill(@Nonnull Bill bill);
	
}
