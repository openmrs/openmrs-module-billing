package org.openmrs.module.billing.api.db.hibernate;

import lombok.AllArgsConstructor;
import org.hibernate.SessionFactory;
import org.openmrs.module.billing.api.db.BillLineItemDAO;
import org.openmrs.module.billing.api.model.BillLineItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Hibernate implementation of {@link BillLineItemDAO}.
 */
@AllArgsConstructor
public class HibernateBillLineItemDAO implements BillLineItemDAO {
	
	private final SessionFactory sessionFactory;
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Integer> getLineItemIdsByBillId(@Nonnull Integer billId) {
		return sessionFactory.getCurrentSession()
		        .createNativeQuery("SELECT bill_line_item_id FROM cashier_bill_line_item WHERE bill_id = :billId")
		        .setParameter("billId", billId).getResultList();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	@Nullable
	public BillLineItem getBillLineItemByUuid(@Nonnull String uuid) {
		// Use native query to bypass Hibernate's first-level cache, ensuring it returns
		// the
		// actual persisted state of line items rather than any in-memory modifications
		List<BillLineItem> results = sessionFactory.getCurrentSession()
		        .createNativeQuery("SELECT * FROM cashier_bill_line_item WHERE uuid = :uuid").addEntity(BillLineItem.class)
		        .setParameter("uuid", uuid).getResultList();
		
		return results.isEmpty() ? null : results.get(0);
	}
	
}
