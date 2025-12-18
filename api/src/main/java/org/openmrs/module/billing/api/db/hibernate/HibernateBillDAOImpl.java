package org.openmrs.module.billing.api.db.hibernate;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.HibernatePatientDAO;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.db.BillDAO;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.search.BillSearch;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * Hibernate implementation of {@link BillDAO}.
 *
 * @see BillDAO
 * @see Bill
 */
public class HibernateBillDAOImpl implements BillDAO {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bill getBill(@Nonnull Integer id) {
		return entityManager.find(Bill.class, id);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bill getBillByUuid(@Nonnull String uuid) {
		TypedQuery<Bill> query = entityManager.createQuery("select b from Bill b where b.uuid = :uuid", Bill.class);
		query.setParameter("uuid", uuid);
		return query.getResultStream().findFirst().orElse(null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bill saveBill(@Nonnull Bill bill) {
		if (bill.getId() == null) {
			entityManager.persist(bill);
			return bill;
		}
		return entityManager.merge(bill);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bill getBillByReceiptNumber(@Nonnull String receiptNumber) {
		TypedQuery<Bill> query = entityManager.createQuery("select b from Bill b where b.receiptNumber = :receiptNumber",
		    Bill.class);
		query.setParameter("receiptNumber", receiptNumber);
		return query.getResultStream().findFirst().orElse(null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Bill> getBillsByPatientUuid(@Nonnull String patientUuid, PagingInfo pagingInfo) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Bill> cq = cb.createQuery(Bill.class);
		Root<Bill> root = cq.from(Bill.class);
		
		Predicate predicate = cb.equal(root.get("patient").get("uuid"), patientUuid);
		cq.where(predicate);
		
		TypedQuery<Bill> query = entityManager.createQuery(cq);
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(predicate);
		applyPaging(query, pagingInfo, predicates);
		
		return query.getResultList();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Bill> getBills(@Nonnull BillSearch billSearch, PagingInfo pagingInfo) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Bill> cq = cb.createQuery(Bill.class);
		Root<Bill> root = cq.from(Bill.class);
		
		List<Predicate> predicates = buildBillSearchPredicate(cb, root, billSearch);
		
		if (!predicates.isEmpty()) {
			cq.where(predicates.toArray(new Predicate[0]));
		}
		
		TypedQuery<Bill> query = entityManager.createQuery(cq);
		
		applyPaging(query, pagingInfo, predicates);
		
		return query.getResultList();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void purgeBill(@Nonnull Bill bill) {
		entityManager.remove(bill);
	}
	
	private List<Predicate> buildBillSearchPredicate(CriteriaBuilder cb, Root<Bill> root, BillSearch billSearch) {
		List<Predicate> predicates = new ArrayList<>();
		
		if (billSearch.getPatientUuid() != null) {
			predicates.add(cb.equal(root.get("patient").get("uuid"), billSearch.getPatientUuid()));
		}
		
		if (billSearch.getPatientName() != null && !billSearch.getPatientName().trim().isEmpty()) {
			List<Patient> matchingPatients = Context.getRegisteredComponent("patientDAO", HibernatePatientDAO.class)
			        .getPatients(billSearch.getPatientName(), 0, null);
			if (matchingPatients != null && !matchingPatients.isEmpty()) {
				predicates.add(root.get("patient").in(matchingPatients));
			} else {
				predicates.add(cb.disjunction());
			}
		}
		
		if (StringUtils.isNotEmpty(billSearch.getCashierUuid())) {
			predicates.add(cb.equal(root.get("cashier").get("uuid"), billSearch.getCashierUuid()));
		}
		
		if (billSearch.getCashPointUuid() != null) {
			predicates.add(cb.equal(root.get("cashPoint").get("uuid"), billSearch.getCashPointUuid()));
		}
		
		if (billSearch.getStatuses() != null && !billSearch.getStatuses().isEmpty()) {
			predicates.add(root.get("status").in(billSearch.getStatuses()));
		}
		
		if (!Boolean.TRUE.equals(billSearch.getIncludeVoided())) {
			predicates.add(cb.equal(root.get("voided"), false));
		}
		
		return predicates;
	}
	
	/**
	 * Applies paging to a query and optionally loads total record count.
	 *
	 * @param query The typed query to apply paging to
	 * @param pagingInfo The paging information (null to skip paging)
	 * @param predicates The predicates used for filtering (needed for count query)
	 */
	private void applyPaging(TypedQuery<Bill> query, PagingInfo pagingInfo, List<Predicate> predicates) {
		if (pagingInfo != null && pagingInfo.getPage() > 0 && pagingInfo.getPageSize() > 0) {
			int offset = (pagingInfo.getPage() - 1) * pagingInfo.getPageSize();
			query.setFirstResult(offset);
			query.setMaxResults(pagingInfo.getPageSize());
			
			if (pagingInfo.getLoadRecordCount()) {
				CriteriaBuilder cb = entityManager.getCriteriaBuilder();
				CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
				Root<Bill> countRoot = countQuery.from(Bill.class);
				countQuery.select(cb.count(countRoot));
				
				if (predicates != null && !predicates.isEmpty()) {
					countQuery.where(predicates.toArray(new Predicate[0]));
				}
				
				Long totalCount = entityManager.createQuery(countQuery).getSingleResult();
				pagingInfo.setTotalRecordCount(totalCount);
			}
		}
	}
	
}
