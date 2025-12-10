package org.openmrs.module.billing.api.db.hibernate;

import org.openmrs.module.billing.api.db.BillLineItemDAO;
import org.openmrs.module.billing.api.model.BillLineItem;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 * Hibernate implementation of the {@link BillLineItemDAO} interface.
 *
 * @see BillLineItemDAO
 */
public class HibernateBillLineItemDAO implements BillLineItemDAO {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public BillLineItem getBillLineItem(@Nonnull Integer id) {
		return entityManager.find(BillLineItem.class, id);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public BillLineItem getBillLineItemByUuid(@Nonnull String uuid) {
		TypedQuery<BillLineItem> query = entityManager.createQuery("select b from BillLineItem b where b.uuid = :uuid",
		    BillLineItem.class);
		query.setParameter("uuid", uuid);
		return query.getResultStream().findFirst().orElse(null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public BillLineItem saveBillLineItem(@Nonnull BillLineItem billLineItem) {
		if (billLineItem.getId() == null) {
			entityManager.persist(billLineItem);
			return billLineItem;
		}
		return entityManager.merge(billLineItem);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void purgeBillLineItem(@Nonnull BillLineItem billLineItem) {
		entityManager.remove(billLineItem);
	}
	
}
