/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.billing.web.rest.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.ModuleSettings;
import org.openmrs.module.billing.api.IBillService;
import org.openmrs.module.billing.api.ICashPointService;
import org.openmrs.module.billing.api.ITimesheetService;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.base.entity.IEntityDataService;
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
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.springframework.web.client.RestClientException;

/**
 * REST resource representing a {@link Bill}.
 */
@Resource(name = RestConstants.VERSION_1 + CashierResourceController.BILLING_NAMESPACE + "/bill", supportedClass = Bill.class,
        supportedOpenmrsVersions = {"2.0 - 2.*"})
public class BillResource extends BaseRestDataResource<Bill> {
    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        DelegatingResourceDescription description = super.getRepresentationDescription(rep);
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
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
            description.addProperty("id");
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
        // For existing bills (not new), compare incoming line items with original database state
        // Clear cache and fetch fresh to get unmodified bill from database
        if (instance.getId() != null && !instance.isPending()) {
            // Get DbSessionFactory using the correct bean name
            DbSessionFactory sessionFactory = Context.getRegisteredComponent("dbSessionFactory", DbSessionFactory.class);
            
            if (sessionFactory != null) {
                DbSession session = sessionFactory.getCurrentSession();
                
                // Evict the instance and its line items from session cache to force fresh fetch
                if (instance.getLineItems() != null) {
                    for (BillLineItem item : instance.getLineItems()) {
                        session.evict(item);
                    }
                }
                session.evict(instance);
                
                // Now fetch fresh from database (will bypass cache since we evicted it)
                IBillService billService = Context.getService(IBillService.class);
                Bill originalBill = billService.getByUuid(instance.getUuid(), false);
                
                if (originalBill != null && originalBill.getLineItems() != null) {
                    // Compare line items using equals() method in BillLineItem
                    // Use Set comparison to ignore order - only check if same items exist
                    Set<BillLineItem> originalSet = new HashSet<>(originalBill.getLineItems());
                    Set<BillLineItem> incomingSet = new HashSet<>((lineItems != null) ? lineItems : new ArrayList<>());
                    
                    // Check if sets contain the same items (ignoring order)
                    if (!originalSet.equals(incomingSet)) {
                        throw new IllegalStateException(
                                "Line items can only be modified when the bill is in PENDING state. Current status: "
                                        + instance.getStatus());
                    }
                    
                    if (originalBill.getLineItems() != null) {
                        for (BillLineItem item : originalBill.getLineItems()) {
                            session.evict(item);
                        }
                    }
                    session.evict(originalBill);
                }
            }
        }
        
        if (instance.getLineItems() == null) {
            int size = (lineItems != null) ? lineItems.size() : 0;
            instance.setLineItems(new ArrayList<BillLineItem>(size));
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
        for (Payment payment : instance.getPayments()) {
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
            if (bill.getCashier() == null) {
                Provider cashier = getCurrentCashier(bill);
                if (cashier == null) {
                    throw new RestClientException("Couldn't find Provider for the current user ("
                            + Context.getAuthenticatedUser().getUsername() + ")");
                }

                bill.setCashier(cashier);
            }

            if (bill.getCashPoint() == null) {
                loadBillCashPoint(bill);
            }

            // Now that all all attributes have been set (i.e., payments and bill status) we can check to see if the bill
            // is fully paid.
            bill.synchronizeBillStatus();
            if (bill.getStatus() == null) {
                bill.setStatus(BillStatus.PENDING);
            }
        }

        return super.save(bill);
    }

    @Override
    protected AlreadyPaged<Bill> doSearch(RequestContext context) {
        String patientUuid = context.getRequest().getParameter("patientUuid");
        String status = context.getRequest().getParameter("status");
        String cashPointUuid = context.getRequest().getParameter("cashPointUuid");
        String includeVoidedLineItemsParam = context.getRequest().getParameter("includeAll");
        String patientName = context.getRequest().getParameter("patientName");

        Patient patient = StringUtils.isNotBlank(patientUuid) ? Context.getPatientService().getPatientByUuid(patientUuid) : null;
        BillStatus billStatus = null;
        List<BillStatus> statusList = null;
        if (StringUtils.isNotBlank(status)) {
            // Support comma-separated statuses: status=PENDING,POSTED
            String[] statusArray = status.split(",");
            if (statusArray.length > 1) {
                // Multiple statuses provided
                statusList = Arrays.stream(statusArray)
                    .map(s -> BillStatus.valueOf(s.trim().toUpperCase()))
                    .collect(Collectors.toList());
            } else {
                // Single status
                billStatus = BillStatus.valueOf(status.trim().toUpperCase());
            }
        }
        CashPoint cashPoint = StringUtils.isNotBlank(cashPointUuid) ? Context.getService(ICashPointService.class).getByUuid(cashPointUuid) : null;

        Bill searchTemplate = new Bill();
        searchTemplate.setPatient(patient);
        searchTemplate.setStatus(billStatus);
        searchTemplate.setCashPoint(cashPoint);
        IBillService service = Context.getService(IBillService.class);

        BillSearch billSearch = new BillSearch(searchTemplate, false);

        if (StringUtils.isNotBlank(patientName)) {
            billSearch.setPatientName(patientName);
        }

        // Set multiple statuses if provided, otherwise single status from template will be used
        if (statusList != null && !statusList.isEmpty()) {
            billSearch.setStatuses(statusList);
        }
        // Default to false (exclude voided line items) unless explicitly set to true
        boolean includeVoidedLineItems = false;
        if (StringUtils.isNotBlank(includeVoidedLineItemsParam)) {
            includeVoidedLineItems = Boolean.parseBoolean(includeVoidedLineItemsParam);
        }
        billSearch.includeVoidedLineItems(includeVoidedLineItems);
        PagingInfo pagingInfo = PagingUtil.getPagingInfoFromContext(context);

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

        return Context.getService(IBillService.class).getByUuid(uniqueId, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<IEntityDataService<Bill>> getServiceClass() {
        return (Class<IEntityDataService<Bill>>) (Object) IBillService.class;
    }

    public String getDisplayString(Bill instance) {
        return instance.getReceiptNumber();
    }

    @Override
    public Bill newDelegate() {
        return new Bill();
    }

    private Provider getCurrentCashier(Bill bill) {
        User currentUser = Context.getAuthenticatedUser();
        ProviderService service = Context.getProviderService();
        Collection<Provider> providers = service.getProvidersByPerson(currentUser.getPerson());
        if (!providers.isEmpty()) {
            return providers.iterator().next();
        }
        return null;
    }

    private void loadBillCashPoint(Bill bill) {
        ITimesheetService service = Context.getService(ITimesheetService.class);
        Timesheet timesheet = service.getCurrentTimesheet(bill.getCashier());
        if (timesheet == null) {
            AdministrationService adminService = Context.getAdministrationService();
            boolean timesheetRequired;
            try {
                timesheetRequired =
                        Boolean.parseBoolean(adminService.getGlobalProperty(ModuleSettings.TIMESHEET_REQUIRED_PROPERTY));
            } catch (Exception e) {
                timesheetRequired = false;
            }

            if (timesheetRequired) {
                throw new RestClientException("A current timesheet does not exist for cashier " + bill.getCashier());
            } else if (bill.getBillAdjusted() != null) {
                // If this is an adjusting bill, copy cash point from billAdjusted
                bill.setCashPoint(bill.getBillAdjusted().getCashPoint());
            } else {
                throw new RestClientException("Cash point cannot be null!");
            }
        } else {
            CashPoint cashPoint = timesheet.getCashPoint();
            if (cashPoint == null) {
                throw new RestClientException("No cash points defined for the current timesheet!");
            }
            bill.setCashPoint(cashPoint);
        }
    }
}
