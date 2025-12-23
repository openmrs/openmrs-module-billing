package org.openmrs.module.billing.api.db.hibernate;

import lombok.AllArgsConstructor;
import org.hibernate.SessionFactory;
import org.openmrs.module.billing.api.db.BillLineItemDAO;

import javax.annotation.Nonnull;
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
		return (List<Integer>) sessionFactory.getCurrentSession()
		        .createNativeQuery("SELECT bill_line_item_id FROM cashier_bill_line_item WHERE bill_id = :billId")
		        .setParameter("billId", billId).getResultList();
	}
	
}
