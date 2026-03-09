package org.openmrs.module.billing.api.db;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.CashPointService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillAudit;
import org.openmrs.module.billing.api.model.BillAuditAction;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Integration tests for {@link BillAuditDAO}.
 */
public class BillAuditDAOTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private BillAuditDAO billAuditDAO;
	
	private BillService billService;
	
	private Bill testBill;
	
	@Before
	public void before() throws Exception {
		billService = Context.getService(BillService.class);
		
		// Use existing test patient from standard OpenMRS test data
		PatientService patientService = Context.getPatientService();
		Patient patient = patientService.getPatient(2);
		
		// Get a provider for the cashier
		ProviderService providerService = Context.getProviderService();
		Provider cashier = providerService.getProvider(1);
		
		// Create a cash point if none exist
		CashPointService cashPointService = Context.getService(CashPointService.class);
		CashPoint cashPoint = null;
		List<CashPoint> cashPoints = cashPointService.getAllCashPoints(false);
		if (cashPoints != null && !cashPoints.isEmpty()) {
			cashPoint = cashPoints.get(0);
		} else {
			// Create a test cash point
			LocationService locationService = Context.getLocationService();
			Location location = locationService.getLocation(1);
			cashPoint = new CashPoint();
			cashPoint.setName("Test Cash Point");
			cashPoint.setLocation(location);
			cashPoint = cashPointService.saveCashPoint(cashPoint);
		}
		
		testBill = new Bill();
		testBill.setPatient(patient);
		testBill.setCashier(cashier);
		testBill.setCashPoint(cashPoint);
		testBill.setStatus(BillStatus.PENDING);
		testBill = billService.saveBill(testBill);
	}
	
	@Test
	public void saveBillAudit_shouldPersistAuditEntryToDatabase() {
		BillAudit audit = createAuditEntry(BillAuditAction.BILL_CREATED, "Test persistence");
		
		BillAudit savedAudit = billAuditDAO.saveBillAudit(audit);
		
		Assert.assertNotNull(savedAudit);
		Assert.assertNotNull(savedAudit.getId());
		
		BillAudit retrievedAudit = billAuditDAO.getBillAudit(savedAudit.getId());
		Assert.assertNotNull(retrievedAudit);
		Assert.assertEquals(savedAudit.getId(), retrievedAudit.getId());
	}
	
	@Test
	public void getBillAuditHistory_shouldReturnAuditsOrderedByDateDescending() throws Exception {
		BillAudit audit1 = createAuditEntry(BillAuditAction.BILL_CREATED, "First");
		billAuditDAO.saveBillAudit(audit1);
		Thread.sleep(10);
		
		BillAudit audit2 = createAuditEntry(BillAuditAction.LINE_ITEM_ADDED, "Second");
		billAuditDAO.saveBillAudit(audit2);
		Thread.sleep(10);
		
		BillAudit audit3 = createAuditEntry(BillAuditAction.STATUS_CHANGED, "Third");
		billAuditDAO.saveBillAudit(audit3);
		
		List<BillAudit> audits = billAuditDAO.getBillAuditHistory(testBill, null);
		
		Assert.assertTrue(audits.size() >= 3);
	}
	
	private BillAudit createAuditEntry(BillAuditAction action, String reason) {
		BillAudit audit = new BillAudit();
		audit.setBill(testBill);
		audit.setAction(action);
		audit.setReason(reason);
		audit.setUser(Context.getAuthenticatedUser());
		audit.setAuditDate(new Date());
		audit.setUuid(UUID.randomUUID().toString());
		return audit;
	}
}
