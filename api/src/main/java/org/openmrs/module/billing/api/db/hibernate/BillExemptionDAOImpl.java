package org.openmrs.module.billing.api.db.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.Concept;
import org.openmrs.module.billing.api.db.BillExemptionDAO;
import org.openmrs.module.billing.api.model.BillExemption;
import org.openmrs.module.billing.api.model.ExemptionType;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class BillExemptionDAOImpl implements BillExemptionDAO {
	
	private final SessionFactory sessionFactory;
	
	public BillExemptionDAOImpl(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public BillExemption save(BillExemption billExemption) {
		sessionFactory.getCurrentSession().saveOrUpdate(billExemption);
		return billExemption;
	}
	
	@Override
	public BillExemption getBillingExemptionById(Integer id) {
		return sessionFactory.getCurrentSession().get(BillExemption.class, id);
	}
	
	@Override
	public BillExemption getBillingExemptionByUuid(String uuid) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<BillExemption> query = cb.createQuery(BillExemption.class);
		Root<BillExemption> root = query.from(BillExemption.class);
		
		query.select(root).where(cb.equal(root.get("uuid"), uuid));
		return session.createQuery(query).getSingleResult();
	}
	
	@Override
	public List<BillExemption> getExemptionsByConcept(Concept concept, ExemptionType itemType, boolean includeRetired) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<BillExemption> query = cb.createQuery(BillExemption.class);
		Root<BillExemption> root = query.from(BillExemption.class);
		
		List<Predicate> predicates = new ArrayList<>();
		
		if (concept != null) {
			predicates.add(cb.equal(root.get("concept"), concept));
		}
		
		if (itemType != null) {
			predicates.add(cb.equal(root.get("exemptionType"), itemType));
		}
		
		if (!includeRetired) {
			predicates.add(cb.isFalse(root.get("retired")));
		}
		
		query.where(predicates.toArray(new Predicate[0]));
		
		return session.createQuery(query).getResultList();
	}
	
	@Override
	public List<BillExemption> getExemptionsByItemType(ExemptionType itemType, boolean includeRetired) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<BillExemption> query = cb.createQuery(BillExemption.class);
		Root<BillExemption> root = query.from(BillExemption.class);
		
		List<Predicate> predicates = new ArrayList<>();
		if (itemType != null) {
			predicates.add(cb.equal(root.get("exemptionType"), itemType));
		}
		
		if (!includeRetired) {
			predicates.add(cb.isFalse(root.get("retired")));
		}
		
		query.where(predicates.toArray(new Predicate[0]));
		
		return session.createQuery(query).getResultList();
	}
	
}
