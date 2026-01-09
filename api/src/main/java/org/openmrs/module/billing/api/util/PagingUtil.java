package org.openmrs.module.billing.api.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.module.billing.api.base.PagingInfo;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

public class PagingUtil {
	
	/**
	 * Applies paging to any entity type query and optionally loads total record count.
	 *
	 * @param query The typed query to apply paging to
	 * @param pagingInfo The paging information (null to skip paging)
	 * @param predicates The predicates used for filtering (needed for count query)
	 * @param sessionFactory The Hibernate session factory
	 * @param entityClass The entity class being queried
	 * @param <T> The entity type
	 */
	public static <T> void applyPaging(TypedQuery<T> query, PagingInfo pagingInfo, List<Predicate> predicates,
	        SessionFactory sessionFactory, Class<T> entityClass) {
		if (pagingInfo != null && pagingInfo.getPage() > 0 && pagingInfo.getPageSize() > 0) {
			int offset = (pagingInfo.getPage() - 1) * pagingInfo.getPageSize();
			query.setFirstResult(offset);
			query.setMaxResults(pagingInfo.getPageSize());
			
			if (pagingInfo.getLoadRecordCount()) {
				Session session = sessionFactory.getCurrentSession();
				
				CriteriaBuilder cb = session.getCriteriaBuilder();
				CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
				Root<T> countRoot = countQuery.from(entityClass);
				countQuery.select(cb.count(countRoot));
				
				if (predicates != null && !predicates.isEmpty()) {
					countQuery.where(predicates.toArray(new Predicate[0]));
				}
				
				Long totalCount = session.createQuery(countQuery).getSingleResult();
				pagingInfo.setTotalRecordCount(totalCount);
			}
		}
	}
}
