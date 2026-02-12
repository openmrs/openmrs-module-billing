package org.openmrs.module.billing.api.db.hibernate;

import lombok.AllArgsConstructor;
import org.hibernate.SessionFactory;
import org.openmrs.module.billing.api.db.BillLineItemDAO;
import org.openmrs.module.billing.api.model.BillLineItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.TypedQuery;
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
	@Nullable
	public BillLineItem getBillLineItemByUuid(@Nonnull String uuid) {
		TypedQuery<BillLineItem> query = sessionFactory.getCurrentSession()
		        .createQuery("select b from BillLineItem b where b.uuid = :uuid", BillLineItem.class);
		query.setParameter("uuid", uuid);
		return query.getResultStream().findFirst().orElse(null);
	}
	
}
