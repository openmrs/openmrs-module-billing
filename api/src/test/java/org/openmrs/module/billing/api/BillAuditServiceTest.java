package org.openmrs.module.billing.api;

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
import org.openmrs.module.billing.api.BillAuditService;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.CashPointService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillAudit;
import org.openmrs.module.billing.api.model.BillAuditAction;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.List;

/**
 * Tests for {@link BillAuditService}.
 */
public class BillAuditServiceTest extends BaseModuleContextSensitiveTest {
	
	private BillAuditService billAuditService;
	
	private BillService billService;
	
	private Bill testBill;
	
	@Before
	public void before() throws Exception {
		billAuditService = Context.getService(BillAuditService.class);
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
	public void saveBillAudit_shouldSaveNewAuditEntry() {
		BillAudit audit = new BillAudit();
		audit.setBill(testBill);
		audit.setAction(BillAuditAction.BILL_CREATED);
		audit.setReason("Test audit entry");
		
		BillAudit savedAudit = billAuditService.saveBillAudit(audit);
		
		Assert.assertNotNull(savedAudit);
		Assert.assertNotNull(savedAudit.getId());
		Assert.assertNotNull(savedAudit.getUuid());
		Assert.assertEquals(BillAuditAction.BILL_CREATED, savedAudit.getAction());
		Assert.assertEquals("Test audit entry", savedAudit.getReason());
	}
	
	@Test
	public void getBillAuditHistory_shouldReturnAllAuditsForBill() {
		// Create multiple audit entries
		createAudit(BillAuditAction.BILL_CREATED, "Created");
		createAudit(BillAuditAction.LINE_ITEM_ADDED, "Added item");
		createAudit(BillAuditAction.STATUS_CHANGED, "Status changed");
		
		List<BillAudit> audits = billAuditService.getBillAuditHistory(testBill, null);
		
		Assert.assertNotNull(audits);
		Assert.assertTrue(audits.size() >= 3);
	}
	
	private BillAudit createAudit(BillAuditAction action, String reason) {
		BillAudit audit = new BillAudit();
		audit.setBill(testBill);
		audit.setAction(action);
		audit.setReason(reason);
		return billAuditService.saveBillAudit(audit);
	}
}
