/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.db.hibernate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.HibernatePatientDAO;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.db.BillDAO;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillDiscount;
import org.openmrs.module.billing.api.model.BillRefund;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.PatientBillSummary;
import org.openmrs.module.billing.api.search.BillSearch;

import javax.annotation.Nonnull;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.openmrs.module.billing.api.db.hibernate.PagingUtil.applyPaging;

/**
 * Hibernate implementation of {@link BillDAO}.
 *
 * @see BillDAO
 * @see Bill
 */
@AllArgsConstructor
public class HibernateBillDAO implements BillDAO {
	
	private static final String FIELD_STATUS = "status";
	
	private static final String FIELD_VOIDED = "voided";
	
	private static final String FIELD_DATE_CREATED = "dateCreated";
	
	private static final String FIELD_PATIENT = "patient";
	
	private static final String FIELD_PATIENT_ID = "patientId";
	
	/**
	 * Bill statuses that can still have an unpaid amount. Keep this list the same as the unpaid check
	 * in DefaultPatientPaymentStatusResolver.
	 */
	private static final List<BillStatus> UNPAID_BILL_STATUSES = Arrays.asList(BillStatus.PENDING, BillStatus.POSTED);
	
	@Setter(AccessLevel.PROTECTED)
	private SessionFactory sessionFactory;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bill getBill(@Nonnull Integer id) {
		return sessionFactory.getCurrentSession().find(Bill.class, id);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bill getBillByUuid(@Nonnull String uuid) {
		TypedQuery<Bill> query = sessionFactory.getCurrentSession().createQuery("select b from Bill b where b.uuid = :uuid",
		    Bill.class);
		query.setParameter("uuid", uuid);
		return query.getResultStream().findFirst().orElse(null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bill getBillByReceiptNumber(@Nonnull String receiptNumber) {
		TypedQuery<Bill> query = sessionFactory.getCurrentSession()
		        .createQuery("select b from Bill b where b.receiptNumber = :receiptNumber", Bill.class);
		query.setParameter("receiptNumber", receiptNumber);
		return query.getResultStream().findFirst().orElse(null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Bill> getBillsByPatientUuid(@Nonnull String patientUuid, PagingInfo pagingInfo) {
		Session session = sessionFactory.getCurrentSession();
		
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Bill> cq = cb.createQuery(Bill.class);
		Root<Bill> root = cq.from(Bill.class);
		
		Predicate predicate = cb.equal(root.get("patient").get("uuid"), patientUuid);
		cq.where(predicate);
		cq.orderBy(cb.desc(root.get(FIELD_DATE_CREATED)));
		
		TypedQuery<Bill> query = session.createQuery(cq);
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(predicate);
		applyPaging(query, pagingInfo, predicates, sessionFactory, Bill.class);
		
		return query.getResultList();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Bill> getBills(@Nonnull BillSearch billSearch, PagingInfo pagingInfo) {
		Session session = sessionFactory.getCurrentSession();
		
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Bill> cq = cb.createQuery(Bill.class);
		Root<Bill> root = cq.from(Bill.class);
		
		List<Predicate> predicates = buildBillSearchPredicate(cb, cq, root, billSearch);
		
		if (!predicates.isEmpty()) {
			cq.where(predicates.toArray(new Predicate[0]));
		}
		cq.orderBy(cb.desc(root.get(FIELD_DATE_CREATED)));
		
		TypedQuery<Bill> query = session.createQuery(cq);
		
		applyPaging(query, pagingInfo, predicates, sessionFactory, Bill.class);
		
		return query.getResultList();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<PatientBillSummary> getPatientBillSummaries(String patientName, PagingInfo pagingInfo) {
		List<Patient> matchedPatients = null;
		if (StringUtils.isNotBlank(patientName)) {
			matchedPatients = findPatientsByName(patientName);
			if (matchedPatients.isEmpty()) {
				if (pagingInfo != null) {
					pagingInfo.setTotalRecordCount(0L);
				}
				return new ArrayList<>();
			}
		}
		
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		
		// One page of patients with their bill count and latest bill date. The same patient id
		// expression is used in SELECT, GROUP BY and ORDER BY so the query is valid on PostgreSQL.
		CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
		Root<Bill> root = cq.from(Bill.class);
		Join<Bill, Patient> patient = root.join(FIELD_PATIENT);
		Expression<Integer> patientId = patient.get(FIELD_PATIENT_ID);
		Expression<Date> latestBillDate = cb.greatest(root.<Date> get(FIELD_DATE_CREATED));
		cq.multiselect(patientId, cb.count(root), latestBillDate)
		        .where(buildPatientBillSummaryPredicates(cb, root, patient, matchedPatients)).groupBy(patientId)
		        .orderBy(cb.desc(latestBillDate), cb.asc(patientId));
		
		TypedQuery<Object[]> pageQuery = session.createQuery(cq);
		if (pagingInfo != null && pagingInfo.getPage() > 0 && pagingInfo.getPageSize() > 0) {
			pageQuery.setFirstResult((pagingInfo.getPage() - 1) * pagingInfo.getPageSize());
			pageQuery.setMaxResults(pagingInfo.getPageSize());
		}
		List<Object[]> page = pageQuery.getResultList();
		
		if (pagingInfo != null) {
			pagingInfo.setTotalRecordCount(countPatientsWithBills(session, cb, matchedPatients));
		}
		if (page.isEmpty()) {
			return new ArrayList<>();
		}
		
		List<Integer> patientIds = page.stream().map(row -> (Integer) row[0]).collect(Collectors.toList());
		
		// Load the page's patients for the result rows, with their identifiers, because the REST ref
		// representation builds the patient display from them. They are loaded before the bills, so
		// bill.getPatient() below returns these same instances and runs no extra query.
		Map<Integer, Patient> patientsById = session
		        .createQuery("select distinct p from Patient p left join fetch p.identifiers where p.patientId in (:ids)",
		            Patient.class)
		        .setParameter("ids", patientIds).getResultList().stream()
		        .collect(Collectors.toMap(Patient::getPatientId, p -> p));
		
		// Unpaid amounts from the open bills of these patients, computed with the Bill rules.
		List<Bill> openBills = session
		        .createQuery(
		            "select distinct b from Bill b left join fetch b.lineItems"
		                    + " left join fetch b.payments pay left join fetch pay.attributes left join fetch b.discounts"
		                    + " where b.patient.patientId in (:ids) and b.voided = false and b.status in (:statuses)",
		            Bill.class)
		        .setParameter("ids", patientIds).setParameter("statuses", UNPAID_BILL_STATUSES).getResultList();
		
		Map<Integer, BigDecimal> unpaidByPatientId = new HashMap<>();
		for (Bill bill : openBills) {
			BigDecimal unpaid = bill.getAmountAfterDiscount().subtract(bill.getTotalPayments()).max(BigDecimal.ZERO);
			unpaidByPatientId.merge(bill.getPatient().getPatientId(), unpaid, BigDecimal::add);
		}
		
		List<PatientBillSummary> result = new ArrayList<>(page.size());
		for (Object[] row : page) {
			Integer id = (Integer) row[0];
			result.add(new PatientBillSummary(patientsById.get(id), (Long) row[1],
			        unpaidByPatientId.getOrDefault(id, BigDecimal.ZERO), (Date) row[2]));
		}
		return result;
	}
	
	private Long countPatientsWithBills(Session session, CriteriaBuilder cb, List<Patient> matchedPatients) {
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Bill> root = cq.from(Bill.class);
		Join<Bill, Patient> patient = root.join(FIELD_PATIENT);
		cq.select(cb.countDistinct(patient.get(FIELD_PATIENT_ID)))
		        .where(buildPatientBillSummaryPredicates(cb, root, patient, matchedPatients));
		return session.createQuery(cq).getSingleResult();
	}
	
	private Predicate[] buildPatientBillSummaryPredicates(CriteriaBuilder cb, Root<Bill> root, Join<Bill, Patient> patient,
	        List<Patient> matchedPatients) {
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get(FIELD_VOIDED), false));
		predicates.add(cb.equal(patient.get(FIELD_VOIDED), false));
		if (matchedPatients != null) {
			predicates.add(patient.in(matchedPatients));
		}
		return predicates.toArray(new Predicate[0]);
	}
	
	private List<Patient> findPatientsByName(String patientName) {
		List<Patient> patients = Context.getRegisteredComponent("patientDAO", HibernatePatientDAO.class)
		        .getPatients(patientName, 0, null);
		return patients != null ? patients : new ArrayList<>();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bill saveBill(@Nonnull Bill bill) {
		sessionFactory.getCurrentSession().saveOrUpdate(bill);
		return bill;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void purgeBill(@Nonnull Bill bill) {
		sessionFactory.getCurrentSession().remove(bill);
	}
	
	private List<Predicate> buildBillSearchPredicate(CriteriaBuilder cb, CriteriaQuery<?> cq, Root<Bill> root,
	        BillSearch billSearch) {
		List<Predicate> predicates = new ArrayList<>();
		
		if (billSearch.getPatientUuid() != null) {
			predicates.add(cb.equal(root.get("patient").get("uuid"), billSearch.getPatientUuid()));
		}
		
		if (StringUtils.isNotBlank(billSearch.getPatientName())) {
			predicates.add(buildPatientNamePredicate(cb, root, billSearch.getPatientName()));
		}
		
		if (StringUtils.isNotEmpty(billSearch.getCashierUuid())) {
			predicates.add(cb.equal(root.get("cashier").get("uuid"), billSearch.getCashierUuid()));
		}
		
		if (billSearch.getCashPointUuid() != null) {
			predicates.add(cb.equal(root.get("cashPoint").get("uuid"), billSearch.getCashPointUuid()));
		}
		
		if (billSearch.getVisitUuid() != null) {
			predicates.add(cb.equal(root.get("visit").get("uuid"), billSearch.getVisitUuid()));
		}
		
		if (StringUtils.isNotBlank(billSearch.getLocationUuid())) {
			predicates.add(cb.equal(root.get("cashPoint").get("location").get("uuid"), billSearch.getLocationUuid()));
		}
		
		if (billSearch.getStartDate() != null) {
			predicates.add(cb.greaterThanOrEqualTo(root.get(FIELD_DATE_CREATED), billSearch.getStartDate()));
		}
		
		if (billSearch.getEndDate() != null) {
			predicates.add(cb.lessThanOrEqualTo(root.get(FIELD_DATE_CREATED), billSearch.getEndDate()));
		}
		
		if (billSearch.getStatuses() != null && !billSearch.getStatuses().isEmpty()) {
			predicates.add(root.get(FIELD_STATUS).in(billSearch.getStatuses()));
		}
		
		if (!Boolean.TRUE.equals(billSearch.getIncludeVoided())) {
			predicates.add(cb.equal(root.get(FIELD_VOIDED), false));
		}
		
		if (billSearch.getDiscountStatuses() != null && !billSearch.getDiscountStatuses().isEmpty()) {
			Subquery<Integer> sub = cq.subquery(Integer.class);
			Root<BillDiscount> discountRoot = sub.from(BillDiscount.class);
			sub.select(cb.literal(1)).where(cb.equal(discountRoot.get("bill"), root),
			    cb.equal(discountRoot.get(FIELD_VOIDED), false),
			    discountRoot.get(FIELD_STATUS).in(billSearch.getDiscountStatuses()));
			predicates.add(cb.exists(sub));
		}
		
		if (billSearch.getRefundStatuses() != null && !billSearch.getRefundStatuses().isEmpty()) {
			Subquery<Integer> sub = cq.subquery(Integer.class);
			Root<BillRefund> refundRoot = sub.from(BillRefund.class);
			sub.select(cb.literal(1)).where(cb.equal(refundRoot.get("bill"), root),
			    cb.equal(refundRoot.get(FIELD_VOIDED), false),
			    refundRoot.get(FIELD_STATUS).in(billSearch.getRefundStatuses()));
			predicates.add(cb.exists(sub));
		}
		
		return predicates;
	}
	
	private Predicate buildPatientNamePredicate(CriteriaBuilder cb, Root<Bill> root, String patientName) {
		List<Patient> matchingPatients = Context.getRegisteredComponent("patientDAO", HibernatePatientDAO.class)
		        .getPatients(patientName, 0, null);
		if (matchingPatients != null && !matchingPatients.isEmpty()) {
			return root.get("patient").in(matchingPatients);
		}
		return cb.disjunction();
	}
	
}
