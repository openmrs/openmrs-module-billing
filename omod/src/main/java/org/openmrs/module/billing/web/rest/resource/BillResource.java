/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.web.rest.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Provider;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.base.ProviderUtil;
import org.openmrs.module.billing.ModuleSettings;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.ITimesheetService;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.api.model.Payment;
import org.openmrs.module.billing.api.model.Timesheet;
import org.openmrs.module.billing.api.search.BillSearch;
import org.openmrs.module.billing.api.util.RoundingUtil;
import org.openmrs.module.billing.web.base.resource.BaseRestDataResource;
import org.openmrs.module.billing.web.base.resource.PagingUtil;
import org.openmrs.module.billing.web.rest.controller.base.CashierResourceController;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.web.client.RestClientException;

/**
 * REST resource representing a {@link Bill}.
 */
@Resource(name = RestConstants.VERSION_1 + CashierResourceController.BILLING_NAMESPACE
        + "/bill", supportedClass = Bill.class, supportedOpenmrsVersions = { "2.0 - 2.*" })
public class BillResource extends DataDelegatingCrudResource<Bill> {
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("adjustedBy", Representation.REF);
			description.addProperty("billAdjusted", Representation.REF);
			description.addProperty("cashPoint", Representation.REF);
			description.addProperty("cashier", Representation.REF);
			description.addProperty("dateCreated");
			description.addProperty("lineItems");
			description.addProperty("patient", Representation.REF);
			description.addProperty("payments", Representation.FULL);
			description.addProperty("receiptNumber");
			description.addProperty("status");
			description.addProperty("adjustmentReason");
			description.addProperty("uuid");
			return description;
		}
		return null;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		return getRepresentationDescription(new DefaultRepresentation());
	}
	
	@PropertySetter("lineItems")
	public void setBillLineItems(Bill instance, List<BillLineItem> lineItems) {
		if (instance.getLineItems() == null) {
			instance.setLineItems(new ArrayList<>(lineItems.size()));
		}
		BaseRestDataResource.syncCollection(instance.getLineItems(), lineItems);
		for (BillLineItem item : instance.getLineItems()) {
			item.setBill(instance);
		}
	}
	
	@PropertySetter("payments")
	public void setBillPayments(Bill instance, Set<Payment> payments) {
		if (instance.getPayments() == null) {
			instance.setPayments(new HashSet<Payment>(payments.size()));
		}
		BaseRestDataResource.syncCollection(instance.getPayments(), payments);
		Provider cashier = null;
		for (Payment payment : instance.getPayments()) {
			if (payment.getId() == null && payment.getCashier() == null) {
				if (cashier == null) {
					cashier = getCurrentCashier();
					if (cashier == null) {
						throw new RestClientException(
						        "The current user (" + Context.getAuthenticatedUser().getUsername() + ") is not a provider");
					}
				}
				payment.setCashier(cashier);
			}
			instance.addPayment(payment);
		}
	}
	
	@PropertySetter("billAdjusted")
	public void setBillAdjusted(Bill instance, Bill billAdjusted) {
		billAdjusted.addAdjustedBy(instance);
		instance.setBillAdjusted(billAdjusted);
	}
	
	@PropertySetter("status")
	public void setBillStatus(Bill instance, BillStatus status) {
		if (instance.getStatus() == null) {
			instance.setStatus(status);
		} else if (instance.getStatus() == BillStatus.PENDING && status == BillStatus.POSTED) {
			instance.setStatus(status);
		}
		if (status == BillStatus.POSTED) {
			RoundingUtil.handleRoundingLineItem(instance);
		}
	}
	
	@PropertySetter("adjustmentReason")
	public void setAdjustReason(Bill instance, String adjustReason) {
		if (instance.getBillAdjusted().getUuid() != null) {
			instance.getBillAdjusted().setAdjustmentReason(adjustReason);
		}
	}
	
	@Override
	public Bill save(Bill bill) {
		//TODO: Test all the ways that this could fail
		
		if (bill.getId() == null) {
			if (bill.getCashPoint() == null) {
				loadBillCashPoint(bill);
			}
			
			// Now that all attributes have been set (i.e., payments and bill status) we can check to see if the bill
			// is fully paid.
			bill.synchronizeBillStatus();
			if (bill.getStatus() == null) {
				bill.setStatus(BillStatus.PENDING);
			}
		}
		
		return Context.getService(BillService.class).saveBill(bill);
	}
	
	@Override
	protected AlreadyPaged<Bill> doSearch(RequestContext context) {
		BillSearch billSearch = buildBillSearchFromRequest(context);
		PagingInfo pagingInfo = PagingUtil.getPagingInfoFromContext(context);
		
		BillService service = Context.getService(BillService.class);
		List<Bill> result = service.getBills(billSearch, pagingInfo);
		
		return new AlreadyPaged<>(context, result, pagingInfo.hasMoreResults(), pagingInfo.getTotalRecordCount());
	}
	
	/**
	 * Gets a bill by UUID
	 *
	 * @param uniqueId The bill UUID.
	 * @return The bill with the specified UUID without voided line items.
	 */
	@Override
	public Bill getByUniqueId(String uniqueId) {
		if (StringUtils.isBlank(uniqueId)) {
			return null;
		}
		
		return Context.getService(BillService.class).getBillByUuid(uniqueId);
	}
	
	@Override
	protected void delete(Bill bill, String s, RequestContext requestContext) throws ResponseException {
		Context.getService(BillService.class).voidBill(bill, s);
	}
	
	@Override
	public void purge(Bill bill, RequestContext requestContext) throws ResponseException {
		Context.getService(BillService.class).purgeBill(bill);
	}
	
	@Override
	public Bill newDelegate() {
		return new Bill();
	}
	
	private Provider getCurrentCashier() {
		return ProviderUtil.getCurrentProvider();
	}
	
	private void loadBillCashPoint(Bill bill) {
		if (bill.getCashier() != null) {
			ITimesheetService service = Context.getService(ITimesheetService.class);
			Timesheet timesheet = service.getCurrentTimesheet(bill.getCashier());
			if (timesheet != null) {
				CashPoint cashPoint = timesheet.getCashPoint();
				if (cashPoint == null) {
					throw new RestClientException("No cash points defined for the current timesheet!");
				}
				bill.setCashPoint(cashPoint);
				return;
			}
			// timesheet is null — check if a timesheet is required
			AdministrationService adminService = Context.getAdministrationService();
			boolean timesheetRequired;
			try {
				timesheetRequired = Boolean
				        .parseBoolean(adminService.getGlobalProperty(ModuleSettings.TIMESHEET_REQUIRED_PROPERTY));
			}
			catch (Exception e) {
				timesheetRequired = false;
			}
			if (timesheetRequired) {
				throw new RestClientException("A current timesheet does not exist for cashier " + bill.getCashier());
			}
		}
		// No cashier, or no timesheet and not required — copy from adjusted bill or fail
		if (bill.getBillAdjusted() != null) {
			// If this is an adjusting bill, copy cash point from billAdjusted
			bill.setCashPoint(bill.getBillAdjusted().getCashPoint());
		} else {
			throw new RestClientException("Cash point cannot be null!");
		}
	}
	
	private BillSearch buildBillSearchFromRequest(RequestContext context) {
		BillSearch billSearch = new BillSearch();
		
		String patientUuid = context.getRequest().getParameter("patientUuid");
		if (StringUtils.isNotBlank(patientUuid)) {
			billSearch.setPatientUuid(patientUuid);
		}
		
		String patientName = context.getRequest().getParameter("patientName");
		if (StringUtils.isNotBlank(patientName)) {
			billSearch.setPatientName(patientName);
		}
		
		String status = context.getRequest().getParameter("status");
		if (StringUtils.isNotBlank(status)) {
			List<BillStatus> statuses = Arrays.stream(status.split(",")).map(String::trim).filter(StringUtils::isNotBlank)
			        .map(s -> BillStatus.valueOf(s.toUpperCase())).collect(Collectors.toList());
			billSearch.setStatuses(statuses);
		}
		
		String cashPointUuid = context.getRequest().getParameter("cashPointUuid");
		if (StringUtils.isNotBlank(cashPointUuid)) {
			billSearch.setCashPointUuid(cashPointUuid);
		}
		
		String includeAll = context.getRequest().getParameter("includeAll");
		if (StringUtils.isNotBlank(includeAll)) {
			billSearch.setIncludeVoidedLineItems(Boolean.parseBoolean(includeAll));
		}
		
		return billSearch;
	}
}
