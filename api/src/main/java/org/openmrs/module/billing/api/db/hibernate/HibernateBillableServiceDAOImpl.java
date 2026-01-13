package org.openmrs.module.billing.api.db.hibernate;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.db.BillableServiceDAO;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.search.BillableServiceSearch;

import javax.annotation.Nonnull;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

import static org.openmrs.module.billing.api.db.hibernate.PagingUtil.applyPaging;

@RequiredArgsConstructor
public class HibernateBillableServiceDAOImpl implements BillableServiceDAO {
	
	private final SessionFactory sessionFactory;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public BillableService getBillableService(@Nonnull Integer id) {
		return sessionFactory.getCurrentSession().find(BillableService.class, id);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public BillableService getBillableServiceByUuid(@Nonnull String uuid) {
		TypedQuery<BillableService> query = sessionFactory.getCurrentSession()
		        .createQuery("select b from BillableService b where b.uuid = :uuid", BillableService.class);
		query.setParameter("uuid", uuid);
		return query.getResultStream().findFirst().orElse(null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<BillableService> getBillableServices(@Nonnull BillableServiceSearch billableServiceSearch,
	        PagingInfo pagingInfo) {
		Session session = sessionFactory.getCurrentSession();
		
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<BillableService> cq = cb.createQuery(BillableService.class);
		Root<BillableService> root = cq.from(BillableService.class);
		
		List<Predicate> predicates = buildBillServicesSearchPredicate(cb, root, billableServiceSearch);
		
		if (!predicates.isEmpty()) {
			cq.where(predicates.toArray(new Predicate[0]));
		}
		
		TypedQuery<BillableService> query = session.createQuery(cq);
		
		applyPaging(query, pagingInfo, predicates, sessionFactory, BillableService.class);
		
		return query.getResultList();
	}
	
	private List<Predicate> buildBillServicesSearchPredicate(CriteriaBuilder cb, Root<BillableService> root,
	        BillableServiceSearch billableServiceSearch) {
		List<Predicate> predicates = new ArrayList<>();
		
		if (billableServiceSearch.getServiceStatus() != null) {
			predicates.add(cb.equal(root.get("serviceStatus"), billableServiceSearch.getServiceStatus()));
		}
		
		if (StringUtils.isNotEmpty(billableServiceSearch.getServiceCategoryUuid())) {
			predicates
			        .add(cb.equal(root.get("serviceCategory").get("uuid"), billableServiceSearch.getServiceCategoryUuid()));
		}
		
		if (StringUtils.isNotEmpty(billableServiceSearch.getServiceTypeUuid())) {
			predicates.add(cb.equal(root.get("serviceType").get("uuid"), billableServiceSearch.getServiceTypeUuid()));
		}
		
		if (StringUtils.isNotEmpty(billableServiceSearch.getConceptUuid())) {
			predicates.add(cb.equal(root.get("concept").get("uuid"), billableServiceSearch.getConceptUuid()));
		}
		
		if (StringUtils.isNotEmpty(billableServiceSearch.getName())) {
			predicates.add(cb.like(cb.lower(root.get("name")), "%" + billableServiceSearch.getName().toLowerCase() + "%"));
		}
		
		if (!billableServiceSearch.getIncludeRetired()) {
			predicates.add(cb.equal(root.get("retired"), false));
		}
		
		return predicates;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public BillableService saveBillableService(@Nonnull BillableService billableService) {
		sessionFactory.getCurrentSession().saveOrUpdate(billableService);
		return billableService;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void purgeBillableService(BillableService billableService) {
		sessionFactory.getCurrentSession().delete(billableService);
	}
}
