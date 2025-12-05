package org.openmrs.module.billing.api.db;

import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.search.BillSearch;

import javax.annotation.Nonnull;
import java.util.List;

public interface BillDAO {

    Bill getBill(@Nonnull Integer id);

    Bill getBillByUuid(@Nonnull String uuid);

    Bill getBillByReceiptNumber(@Nonnull String receiptNumber);

    List<Bill> getBillsByBillSearch(@Nonnull BillSearch billSearch, @Nonnull PagingInfo pagingInfo);
}
