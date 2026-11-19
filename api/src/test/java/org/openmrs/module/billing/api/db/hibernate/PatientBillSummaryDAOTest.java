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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.db.BillDAO;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.PatientBillSummary;
import org.openmrs.module.billing.api.model.Payment;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class PatientBillSummaryDAOTest extends BaseModuleContextSensitiveTest {
	
	private static final int ALICE = 9001;
	
	private static final int BOB = 9002;
	
	private static final int CAROL = 9003;
	
	private BillDAO dao;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@BeforeEach
	public void setup() throws Exception {
		dao = new HibernateBillDAO(sessionFactory);
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(TestConstants.BASE_DATASET_DIR + "StockOperationType.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "PaymentModeTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "CashPointTest.xml");
		executeDataSet(TestConstants.BASE_DATASET_DIR + "PatientBillSummaryTest.xml");
		updateSearchIndex();
	}
	
	@Test
	public void shouldReturnOneRowPerPatientSortedByLatestBillDate() {
		PagingInfo pagingInfo = new PagingInfo(1, 10);
		
		List<PatientBillSummary> result = dao.getPatientBillSummaries(null, pagingInfo);
		
		assertEquals(Arrays.asList(BOB, ALICE, CAROL), patientIds(result));
		assertEquals(Long.valueOf(3L), pagingInfo.getTotalRecordCount());
	}
	
	@Test
	public void shouldComputeBillCountAndLatestBillDate() throws ParseException {
		List<PatientBillSummary> result = allRows();
		
		PatientBillSummary alice = row(result, ALICE);
		assertEquals(Long.valueOf(3L), alice.getBillCount());
		assertEquals(date("2026-01-20"), alice.getLatestBillDate().getTime());
		
		PatientBillSummary bob = row(result, BOB);
		assertEquals(Long.valueOf(4L), bob.getBillCount());
		assertEquals(date("2026-02-01"), bob.getLatestBillDate().getTime());
		
		PatientBillSummary carol = row(result, CAROL);
		assertEquals(Long.valueOf(1L), carol.getBillCount());
		assertEquals(date("2026-01-20"), carol.getLatestBillDate().getTime());
	}
	
	@Test
	public void shouldComputeTotalUnpaidFromPendingAndPostedBills() {
		List<PatientBillSummary> result = allRows();
		
		assertEquals(0, new BigDecimal("225.00").compareTo(row(result, ALICE).getTotalUnpaid()));
	}
	
	@Test
	public void shouldClampEachBillAtZeroSeparately() {
		// Bob: 9106 is overpaid by 100 (counts 0) and 9112 has 100 due. The overpayment must not reduce
		// the 100. The ADJUSTED (300) and REFUNDED (50) bills count 0.
		List<PatientBillSummary> result = allRows();
		
		assertEquals(0, new BigDecimal("100.00").compareTo(row(result, BOB).getTotalUnpaid()));
	}
	
	@Test
	public void shouldReturnZeroUnpaidForPaidBills() {
		List<PatientBillSummary> result = allRows();
		
		assertEquals(0, BigDecimal.ZERO.compareTo(row(result, CAROL).getTotalUnpaid()));
	}
	
	@Test
	public void shouldPageByPatient() {
		PagingInfo firstPage = new PagingInfo(1, 2);
		assertEquals(Arrays.asList(BOB, ALICE), patientIds(dao.getPatientBillSummaries(null, firstPage)));
		assertEquals(Long.valueOf(3L), firstPage.getTotalRecordCount());
		
		PagingInfo secondPage = new PagingInfo(2, 2);
		assertEquals(Arrays.asList(CAROL), patientIds(dao.getPatientBillSummaries(null, secondPage)));
		assertEquals(Long.valueOf(3L), secondPage.getTotalRecordCount());
	}
	
	@Test
	public void shouldReturnEmptyPageAfterLastPatient() {
		PagingInfo pagingInfo = new PagingInfo(3, 2);
		
		assertTrue(dao.getPatientBillSummaries(null, pagingInfo).isEmpty());
		assertEquals(Long.valueOf(3L), pagingInfo.getTotalRecordCount());
	}
	
	@Test
	public void shouldFilterByPatientName() {
		PagingInfo pagingInfo = new PagingInfo(1, 10);
		
		assertEquals(Arrays.asList(ALICE), patientIds(dao.getPatientBillSummaries("Alice", pagingInfo)));
		assertEquals(Long.valueOf(1L), pagingInfo.getTotalRecordCount());
	}
	
	@Test
	public void shouldFilterByPatientNameAcrossSeveralPatients() {
		PagingInfo pagingInfo = new PagingInfo(1, 10);
		
		List<Integer> ids = patientIds(dao.getPatientBillSummaries("Summary", pagingInfo));
		
		// "Summary" also matches Vera, Dan, Erin and Gina, who must not be listed.
		assertEquals(Arrays.asList(BOB, ALICE, CAROL), ids);
		assertEquals(Long.valueOf(3L), pagingInfo.getTotalRecordCount());
	}
	
	@Test
	public void shouldReturnEmptyWhenNoPatientMatchesName() {
		PagingInfo pagingInfo = new PagingInfo(1, 10);
		
		assertTrue(dao.getPatientBillSummaries("Nobody", pagingInfo).isEmpty());
		assertEquals(Long.valueOf(0L), pagingInfo.getTotalRecordCount());
	}
	
	@Test
	public void shouldTreatBlankNameAsNoFilter() {
		assertEquals(3, dao.getPatientBillSummaries("  ", new PagingInfo(1, 10)).size());
	}
	
	@Test
	public void shouldReturnAllRowsWithoutPagingInfo() {
		assertEquals(3, dao.getPatientBillSummaries(null, null).size());
	}
	
	@Test
	public void shouldNotFetchBillCollectionsOneBillAtATime() {
		sessionFactory.getCurrentSession().clear();
		Statistics stats = sessionFactory.getStatistics();
		stats.clear();
		
		allRows();
		
		for (String role : Arrays.asList(Bill.class.getName() + ".lineItems", Bill.class.getName() + ".payments",
		    Bill.class.getName() + ".discounts", Payment.class.getName() + ".attributes")) {
			assertEquals(0L, stats.getCollectionStatistics(role).getFetchCount(), "Separate fetches of " + role);
		}
	}
	
	@Test
	public void shouldLoadPatientIdentifiersWithThePatients() {
		// The REST ref representation of a patient builds its display from the preferred identifier.
		sessionFactory.getCurrentSession().clear();
		Statistics stats = sessionFactory.getStatistics();
		stats.clear();
		
		allRows().forEach(r -> r.getPatient().getPatientIdentifier());
		
		assertEquals(0L, stats.getCollectionStatistics(Patient.class.getName() + ".identifiers").getFetchCount());
	}
	
	/** All rows of the dataset: one page that is larger than the number of patients. */
	private List<PatientBillSummary> allRows() {
		return dao.getPatientBillSummaries(null, new PagingInfo(1, 10));
	}
	
	private static List<Integer> patientIds(List<PatientBillSummary> rows) {
		return rows.stream().map(r -> r.getPatient().getPatientId()).collect(Collectors.toList());
	}
	
	private static PatientBillSummary row(List<PatientBillSummary> rows, int patientId) {
		PatientBillSummary match = rows.stream().filter(r -> r.getPatient().getPatientId() == patientId).findFirst()
		        .orElse(null);
		assertNotNull(match, "No row for patient " + patientId);
		return match;
	}
	
	private static long date(String isoDate) throws ParseException {
		return new SimpleDateFormat("yyyy-MM-dd").parse(isoDate).getTime();
	}
}
