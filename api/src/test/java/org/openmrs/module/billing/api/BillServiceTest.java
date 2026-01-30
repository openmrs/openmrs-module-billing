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
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillAudit;
import org.openmrs.module.billing.api.model.BillAuditAction;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.List;

/**
 * Tests for {@link BillService} with audit trail integration.
 */
public class BillServiceTest extends BaseModuleContextSensitiveTest {
	
	private BillService billService;
	
	private BillAuditService billAuditService;
	
	private Patient testPatient;
	
	private ProviderService providerService;
	
	private CashPointService cashPointService;
	
	private LocationService locationService;
	
	@Before
	public void before() throws Exception {
		billService = Context.getService(BillService.class);
		billAuditService = Context.getService(BillAuditService.class);
		
		// Use existing test patient from standard OpenMRS test data
		PatientService patientService = Context.getPatientService();
		testPatient = patientService.getPatient(2);
		
		// Initialize provider and location services
		this.providerService = Context.getProviderService();
		this.locationService = Context.getLocationService();
		this.cashPointService = Context.getService(CashPointService.class);
	}
	
	@Test
	public void saveBill_shouldCreateBillSuccessfully() {
		Bill bill = new Bill();
		bill.setPatient(testPatient);
		Provider cashier = providerService.getProvider(1);
		bill.setCashier(cashier);
		
		// Get or create cash point
		CashPoint cashPoint = null;
		List<CashPoint> cashPoints = cashPointService.getAllCashPoints(false);
		if (cashPoints != null && !cashPoints.isEmpty()) {
			cashPoint = cashPoints.get(0);
		} else {
			Location location = locationService.getLocation(1);
			cashPoint = new CashPoint();
			cashPoint.setName("Test Cash Point");
			cashPoint.setLocation(location);
			cashPoint = cashPointService.saveCashPoint(cashPoint);
		}
		bill.setCashPoint(cashPoint);
		bill.setStatus(BillStatus.PENDING);
		
		Bill savedBill = billService.saveBill(bill);
		
		Assert.assertNotNull(savedBill);
		Assert.assertNotNull(savedBill.getId());
		Assert.assertNotNull(savedBill.getUuid());
	}
	
	@Test
	public void saveBill_shouldCreateAuditEntryForNewBill() {
		Bill bill = new Bill();
		bill.setPatient(testPatient);
		Provider cashier = providerService.getProvider(1);
		bill.setCashier(cashier);
		
		// Get or create cash point
		CashPoint cashPoint = null;
		List<CashPoint> cashPoints = cashPointService.getAllCashPoints(false);
		if (cashPoints != null && !cashPoints.isEmpty()) {
			cashPoint = cashPoints.get(0);
		} else {
			Location location = locationService.getLocation(1);
			cashPoint = new CashPoint();
			cashPoint.setName("Test Cash Point");
			cashPoint.setLocation(location);
			cashPoint = cashPointService.saveCashPoint(cashPoint);
		}
		bill.setCashPoint(cashPoint);
		bill.setStatus(BillStatus.PENDING);
		
		Bill savedBill = billService.saveBill(bill);
		
		List<BillAudit> audits = billAuditService.getBillAuditHistory(savedBill, null);
		
		Assert.assertNotNull(audits);
		Assert.assertTrue("Bill creation should create audit entry", audits.size() > 0);
		
		boolean foundCreatedAction = audits.stream().anyMatch(audit -> audit.getAction() == BillAuditAction.BILL_CREATED);
		Assert.assertTrue("Should have BILL_CREATED audit entry", foundCreatedAction);
	}
	
	@Test
	public void saveBill_shouldCreateAuditEntryWhenStatusChanges() {
		Bill bill = new Bill();
		bill.setPatient(testPatient);
		Provider cashier = providerService.getProvider(1);
		bill.setCashier(cashier);
		
		// Get or create cash point
		CashPoint cashPoint = null;
		List<CashPoint> cashPoints = cashPointService.getAllCashPoints(false);
		if (cashPoints != null && !cashPoints.isEmpty()) {
			cashPoint = cashPoints.get(0);
		} else {
			Location location = locationService.getLocation(1);
			cashPoint = new CashPoint();
			cashPoint.setName("Test Cash Point");
			cashPoint.setLocation(location);
			cashPoint = cashPointService.saveCashPoint(cashPoint);
		}
		bill.setCashPoint(cashPoint);
		bill.setStatus(BillStatus.PENDING);
		bill = billService.saveBill(bill);
		
		bill.setStatus(BillStatus.POSTED);
		Bill updatedBill = billService.saveBill(bill);
		
		// Verify bill status was updated
		Assert.assertNotNull(updatedBill);
		Assert.assertEquals(BillStatus.POSTED, updatedBill.getStatus());
		
		// Verify audit history is maintained
		List<BillAudit> audits = billAuditService.getBillAuditHistory(updatedBill, null);
		Assert.assertNotNull(audits);
		Assert.assertTrue("Should have at least one audit entry", audits.size() > 0);
	}
}
