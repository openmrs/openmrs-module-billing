package org.openmrs.module.billing.api.db.hibernate;

import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.module.billing.api.db.BillDiscountDAO;
import org.openmrs.module.billing.api.model.BillDiscount;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@RequiredArgsConstructor
public class HibernateBillDiscountDAO implements BillDiscountDAO {
	
	private final SessionFactory sessionFactory;
	

	@Override
	public BillDiscount getBillDiscountById(Integer id) {
		return sessionFactory.getCurrentSession().get(BillDiscount.class, id);
	}
	
	@Override
	public BillDiscount getBillDiscountByUuid(String uuid) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<BillDiscount> query = cb.createQuery(BillDiscount.class);
		Root<BillDiscount> root = query.from(BillDiscount.class);
		
		query.select(root).where(cb.equal(root.get("uuid"), uuid));
		return session.createQuery(query).uniqueResult();
	}
	
	@Override
	public BillDiscount getBillDiscountByBillId(Integer billId) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<BillDiscount> query = cb.createQuery(BillDiscount.class);
		Root<BillDiscount> root = query.from(BillDiscount.class);
		
		query.select(root).where(cb.equal(root.get("bill").get("id"), billId), cb.isFalse(root.get("voided")));
		return session.createQuery(query).uniqueResult();
	}

	@Override
	public BillDiscount saveBillDiscount(BillDiscount billDiscount) {
		sessionFactory.getCurrentSession().saveOrUpdate(billDiscount);
		return billDiscount;
	}
}
