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

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Provider;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.base.ProviderUtil;
import org.openmrs.module.billing.api.PaymentModeService;
import org.openmrs.module.billing.web.base.resource.BaseRestDataResource;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.Payment;
import org.openmrs.module.billing.api.model.PaymentAttribute;
import org.openmrs.module.billing.api.model.PaymentMode;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * REST resource representing a {@link Payment}.
 */
@SubResource(parent = BillResource.class, path = "payment", supportedClass = Payment.class, supportedOpenmrsVersions = {
        "2.0 - 2.*" })
public class PaymentResource extends DelegatingSubResource<Payment, Bill, BillResource> {
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			description.addProperty("uuid");
			description.addProperty("instanceType", Representation.REF);
			description.addProperty("attributes");
			description.addProperty("amount");
			description.addProperty("amountTendered");
			description.addProperty("cashier", Representation.REF);
			description.addProperty("dateCreated");
			description.addProperty("voided");
			return description;
		}
		
		return null;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("instanceType");
		description.addProperty("attributes");
		description.addProperty("amount");
		description.addProperty("amountTendered");
		description.addProperty("cashier");
		
		return description;
	}
	
	@PropertySetter("cashier")
	public void setCashier(Payment instance, String uuid) {
		if (StringUtils.isBlank(uuid)) {
			throw new APIException("Cashier UUID must not be null or blank.");
		}
		Provider provider = Context.getProviderService().getProviderByUuid(uuid);
		if (provider == null) {
			throw new ObjectNotFoundException();
		}
		instance.setCashier(provider);
	}
	
	// Work around TypeVariable issue on base generic property (BaseCustomizableInstanceData.getInstanceType)
	@PropertySetter("instanceType")
	public void setPaymentMode(Payment instance, String uuid) {
		PaymentModeService service = Context.getService(PaymentModeService.class);
		
		PaymentMode mode = service.getPaymentModeByUuid(uuid);
		if (mode == null) {
			throw new ObjectNotFoundException();
		}
		
		instance.setInstanceType(mode);
	}
	
	@PropertySetter("attributes")
	public void setPaymentAttributes(Payment instance, Set<PaymentAttribute> attributes) {
		if (instance.getAttributes() == null) {
			instance.setAttributes(new HashSet<PaymentAttribute>());
		}
		
		BaseRestDataResource.syncCollection(instance.getAttributes(), attributes);
		for (PaymentAttribute attr : instance.getAttributes()) {
			attr.setOwner(instance);
		}
	}
	
	@PropertySetter("amount")
	public void setPaymentAmount(Payment instance, Object price) {
		// TODO Conversion logic
		double amount;
		if (price instanceof Integer) {
			int rawAmount = (Integer) price;
			amount = Double.valueOf(rawAmount);
			instance.setAmount(BigDecimal.valueOf(amount));
		} else {
			instance.setAmount(BigDecimal.valueOf((Double) price));
		}
	}
	
	@PropertySetter("amountTendered")
	public void setPaymentAmountTendered(Payment instance, Object price) {
		// TODO Conversion logic
		double amount;
		if (price instanceof Integer) {
			int rawAmount = (Integer) price;
			amount = Double.valueOf(rawAmount);
			instance.setAmountTendered(BigDecimal.valueOf(amount));
		} else {
			instance.setAmountTendered(BigDecimal.valueOf((Double) price));
		}
	}
	
	@PropertyGetter("dateCreated")
	public Long getPaymentDate(Payment instance) {
		return instance.getDateCreated().getTime();
	}
	
	@Override
	public Payment save(Payment delegate) {
		if (delegate.getCashier() == null) {
			Provider cashier = ProviderUtil.getCurrentProvider();
			if (cashier == null) {
				throw new APIException(
				        "The authenticated user is not associated with a Provider and cannot process payments.");
			}
			delegate.setCashier(cashier);
		}
		
		BillService service = Context.getService(BillService.class);
		Bill bill = delegate.getBill();
		bill.addPayment(delegate);
		service.saveBill(bill);
		
		return delegate;
	}
	
	@Override
	protected void delete(Payment delegate, String reason, RequestContext context) {
		delete(delegate.getBill().getUuid(), delegate.getUuid(), reason, context);
	}
	
	@Override
	public void delete(String parentUniqueId, final String uuid, String reason, RequestContext context) {
		BillService service = Context.getService(BillService.class);
		Bill bill = findBill(service, parentUniqueId);
		Payment payment = findPayment(bill, uuid);
		
		payment.setVoided(true);
		payment.setVoidReason(reason);
		payment.setVoidedBy(Context.getAuthenticatedUser());
		
		service.saveBill(bill);
	}
	
	@Override
	public void purge(Payment delegate, RequestContext context) {
		purge(delegate.getBill().getUuid(), delegate.getUuid(), context);
	}
	
	@Override
	public void purge(String parentUniqueId, String uuid, RequestContext context) {
		BillService service = Context.getService(BillService.class);
		Bill bill = findBill(service, parentUniqueId);
		Payment payment = findPayment(bill, uuid);
		
		bill.removePayment(payment);
		service.saveBill(bill);
	}
	
	@Override
	public PageableResult doGetAll(Bill parent, RequestContext context) {
		return new AlreadyPaged<Payment>(context, new ArrayList<Payment>(parent.getPayments()), false);
	}
	
	@Override
	public Payment getByUniqueId(String uniqueId) {
		return null;
	}
	
	@Override
	public Bill getParent(Payment instance) {
		return instance.getBill();
	}
	
	@Override
	public void setParent(Payment instance, Bill parent) {
		instance.setBill(parent);
	}
	
	@Override
	public Payment newDelegate() {
		return new Payment();
	}
	
	private Bill findBill(BillService service, String billUUID) {
		Bill bill = service.getBillByUuid(billUUID);
		if (bill == null) {
			throw new ObjectNotFoundException();
		}
		
		return bill;
	}
	
	private Payment findPayment(Bill bill, final String paymentUUID) {
		
		for (Payment payment : bill.getPayments()) {
			if (payment != null && payment.getUuid().equals(paymentUUID)) {
				return payment;
			}
		}
		throw new ObjectNotFoundException();
	}
}
