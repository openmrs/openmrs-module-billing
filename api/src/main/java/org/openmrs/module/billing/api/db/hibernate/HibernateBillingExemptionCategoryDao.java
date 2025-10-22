package org.openmrs.module.billing.api.db.hibernate;

import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.module.billing.api.db.BillingExemptionCategoryDao;
import org.openmrs.module.billing.api.model.BillingExemptionCategory;
import org.openmrs.module.billing.api.model.ExemptionCategoryType;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class HibernateBillingExemptionCategoryDao implements BillingExemptionCategoryDao {
	
	private final SessionFactory sessionFactory;
	
	@Override
	public BillingExemptionCategory getCategory(Integer id) {
		return sessionFactory.getCurrentSession().get(BillingExemptionCategory.class, id);
	}
	
	@Override
	public BillingExemptionCategory getCategoryByUuid(String uuid) {
		TypedQuery<BillingExemptionCategory> query = sessionFactory.getCurrentSession()
		        .createQuery("from BillingExemptionCategory ec where ec.uuid = :uuid", BillingExemptionCategory.class);
		query.setParameter("uuid", uuid);
		
		List<BillingExemptionCategory> results = query.getResultList();
		return results.isEmpty() ? null : results.get(0);
	}
	
	@Override
	public List<BillingExemptionCategory> getCategoriesByType(ExemptionCategoryType type) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<BillingExemptionCategory> cq = builder.createQuery(BillingExemptionCategory.class);
		Root<BillingExemptionCategory> root = cq.from(BillingExemptionCategory.class);
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(root.get("type"), type));
		
		cq.where(predicates.toArray(new Predicate[0]));
		return session.createQuery(cq).getResultList();
	}
	
	@Override
	public BillingExemptionCategory getCategoryByTypeAndKey(ExemptionCategoryType type, String exemptionKey) {
		TypedQuery<BillingExemptionCategory> query = sessionFactory.getCurrentSession().createQuery(
		    "from BillingExemptionCategory c " + "where c.type = :type and c.exemptionKey = :key and c.retired = false",
		    BillingExemptionCategory.class);
		query.setParameter("type", type);
		query.setParameter("key", exemptionKey);
		
		List<BillingExemptionCategory> results = query.getResultList();
		return results.isEmpty() ? null : results.get(0);
		
	}
	
	@Override
	public BillingExemptionCategory save(BillingExemptionCategory category) {
		sessionFactory.getCurrentSession().saveOrUpdate(category);
		return category;
		
	}
	
	@Override
	public List<BillingExemptionCategory> getAll(boolean includeRetired) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<BillingExemptionCategory> cq = builder.createQuery(BillingExemptionCategory.class);
		Root<BillingExemptionCategory> root = cq.from(BillingExemptionCategory.class);
		
		if (!includeRetired) {
			cq.where(builder.isFalse(root.get("retired")));
		}
		
		return session.createQuery(cq).getResultList();
	}
}
