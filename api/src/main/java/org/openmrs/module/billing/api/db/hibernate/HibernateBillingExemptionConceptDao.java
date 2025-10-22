package org.openmrs.module.billing.api.db.hibernate;

import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.Concept;
import org.openmrs.module.billing.api.db.BillingExemptionConceptDao;
import org.openmrs.module.billing.api.model.BillingExemptionCategory;
import org.openmrs.module.billing.api.model.BillingExemptionConcept;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class HibernateBillingExemptionConceptDao implements BillingExemptionConceptDao {
	
	private final SessionFactory sessionFactory;
	
	@Override
	public BillingExemptionConcept getExemptionConcept(Integer exemptionConceptId) {
		return sessionFactory.getCurrentSession().get(BillingExemptionConcept.class, exemptionConceptId);
	}
	
	@Override
	public BillingExemptionConcept getExemptionConceptByUuid(String uuid) {
		TypedQuery<BillingExemptionConcept> query = sessionFactory.getCurrentSession()
		        .createQuery("from BillingExemptionConcept ec where ec.uuid = :uuid", BillingExemptionConcept.class);
		query.setParameter("uuid", uuid);
		
		List<BillingExemptionConcept> results = query.getResultList();
		return results.isEmpty() ? null : results.get(0);
	}
	
	@Override
	public List<BillingExemptionConcept> getExemptionConceptsByCategory(BillingExemptionCategory category) {
		return getExemptionConceptsByCategory(category, false);
	}
	
	@Override
	public List<BillingExemptionConcept> getExemptionConceptsByCategory(BillingExemptionCategory category,
	        boolean includeVoided) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<BillingExemptionConcept> cq = cb.createQuery(BillingExemptionConcept.class);
		Root<BillingExemptionConcept> root = cq.from(BillingExemptionConcept.class);
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get("category"), category));
		
		if (!includeVoided) {
			predicates.add(cb.isFalse(root.get("voided")));
		}
		
		cq.where(predicates.toArray(new Predicate[0]));
		cq.orderBy(cb.asc(root.get("dateCreated")));
		
		return session.createQuery(cq).getResultList();
	}
	
	@Override
	public BillingExemptionConcept getExemptionConceptByCategoryAndConcept(BillingExemptionCategory category,
	        Concept concept) {
		TypedQuery<BillingExemptionConcept> query = sessionFactory.getCurrentSession().createQuery(
		    "from BillingExemptionConcept ec "
		            + "where ec.category = :category and ec.concept = :concept and ec.voided = false",
		    BillingExemptionConcept.class);
		query.setParameter("category", category);
		query.setParameter("concept", concept);
		
		List<BillingExemptionConcept> results = query.getResultList();
		return results.isEmpty() ? null : results.get(0);
	}
	
	@Override
	public BillingExemptionConcept saveExemptionConcept(BillingExemptionConcept exemptionConcept) {
		sessionFactory.getCurrentSession().saveOrUpdate(exemptionConcept);
		return exemptionConcept;
	}
	
	@Override
	public void deleteExemptionConcept(BillingExemptionConcept exemptionConcept) {
		sessionFactory.getCurrentSession().delete(exemptionConcept);
	}
}
