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

import java.math.BigDecimal;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillLineItemService;
import org.openmrs.module.billing.api.BillRefundService;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillRefund;
import org.openmrs.module.billing.api.model.RefundStatus;
import org.openmrs.module.billing.web.rest.controller.base.CashierResourceController;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.InvalidSearchException;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/** REST resource for {@link BillRefund}. */
@Resource(name = RestConstants.VERSION_1 + CashierResourceController.BILLING_NAMESPACE
        + "/billRefund", supportedClass = BillRefund.class, supportedOpenmrsVersions = { "2.0 - 2.*" })
public class BillRefundResource extends DataDelegatingCrudResource<BillRefund> {
	
	@Override
	public BillRefund newDelegate() {
		return new BillRefund();
	}
	
	@Override
	public BillRefund save(BillRefund delegate) {
		if (delegate.getId() == null && delegate.getInitiator() == null) {
			delegate.setInitiator(Context.getAuthenticatedUser());
		}
		return Context.getService(BillRefundService.class).saveBillRefund(delegate);
	}
	
	@Override
	public BillRefund getByUniqueId(String uniqueId) {
		return Context.getService(BillRefundService.class).getBillRefundByUuid(uniqueId);
	}
	
	@Override
	protected void delete(BillRefund delegate, String reason, RequestContext context) throws ResponseException {
		if (Boolean.TRUE.equals(delegate.getVoided())) {
			return;
		}
		delegate.setVoided(true);
		delegate.setVoidReason(reason);
		Context.getService(BillRefundService.class).saveBillRefund(delegate);
	}
	
	@Override
	public void purge(BillRefund delegate, RequestContext context) throws ResponseException {
		throw new UnsupportedOperationException("Purge is not supported for BillRefund");
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		
		if (rep instanceof RefRepresentation) {
			description.addProperty("uuid");
			description.addProperty("refundAmount");
			description.addProperty("status");
			description.addProperty("voided");
		} else if (rep instanceof DefaultRepresentation) {
			description.addProperty("uuid");
			description.addProperty("billUuid");
			description.addProperty("lineItemUuid");
			description.addProperty("refundAmount");
			description.addProperty("reason");
			description.addProperty("initiator", Representation.REF);
			description.addProperty("approver", Representation.REF);
			description.addProperty("completer", Representation.REF);
			description.addProperty("dateApproved");
			description.addProperty("dateCompleted");
			description.addProperty("dateCreated");
			description.addProperty("status");
			description.addProperty("voided");
		} else if (rep instanceof FullRepresentation) {
			description.addProperty("uuid");
			description.addProperty("billUuid");
			description.addProperty("lineItemUuid");
			description.addProperty("refundAmount");
			description.addProperty("reason");
			description.addProperty("initiator", Representation.DEFAULT);
			description.addProperty("approver", Representation.DEFAULT);
			description.addProperty("completer", Representation.DEFAULT);
			description.addProperty("dateApproved");
			description.addProperty("dateCompleted");
			description.addProperty("dateCreated");
			description.addProperty("status");
			description.addProperty("voided");
			description.addProperty("auditInfo");
		}
		
		return description;
	}
	
	@PropertyGetter("billUuid")
	public String getBillUuid(BillRefund instance) {
		return instance.getBill() == null ? null : instance.getBill().getUuid();
	}
	
	@PropertyGetter("lineItemUuid")
	public String getLineItemUuid(BillRefund instance) {
		return instance.getLineItem() == null ? null : instance.getLineItem().getUuid();
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("bill");
		description.addProperty("lineItem");
		description.addProperty("refundAmount");
		description.addProperty("reason");
		return description;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("status");
		description.addProperty("approver");
		description.addProperty("completer");
		return description;
	}
	
	@PropertySetter("status")
	public void setStatus(BillRefund instance, String status) {
		if (status == null) {
			return;
		}
		try {
			instance.setStatus(RefundStatus.valueOf(status));
		}
		catch (IllegalArgumentException e) {
			throw new ConversionException("Unknown refund status: '" + status + "'", e);
		}
	}
	
	@PropertySetter("bill")
	public void setBill(BillRefund instance, String billUuid) {
		if (billUuid != null) {
			Bill bill = Context.getService(BillService.class).getBillByUuid(billUuid);
			if (bill == null) {
				throw new ObjectNotFoundException("No bill found with uuid: " + billUuid);
			}
			instance.setBill(bill);
		}
	}
	
	@PropertySetter("lineItem")
	public void setLineItem(BillRefund instance, String lineItemUuid) {
		if (lineItemUuid != null) {
			BillLineItem lineItem = Context.getService(BillLineItemService.class).getBillLineItemByUuid(lineItemUuid);
			if (lineItem == null) {
				throw new ObjectNotFoundException("No bill line item found with uuid: " + lineItemUuid);
			}
			instance.setLineItem(lineItem);
		}
	}
	
	@PropertySetter("approver")
	public void setApprover(BillRefund instance, String approverUuid) {
		if (approverUuid != null) {
			User approver = Context.getUserService().getUserByUuid(approverUuid);
			if (approver == null) {
				throw new ObjectNotFoundException("No user found with uuid: " + approverUuid);
			}
			instance.setApprover(approver);
		}
	}
	
	@PropertySetter("completer")
	public void setCompleter(BillRefund instance, String completerUuid) {
		if (completerUuid != null) {
			User completer = Context.getUserService().getUserByUuid(completerUuid);
			if (completer == null) {
				throw new ObjectNotFoundException("No user found with uuid: " + completerUuid);
			}
			instance.setCompleter(completer);
		}
	}
	
	// Default REST converter can't turn JSON Integer/Double into BigDecimal.
	@PropertySetter("refundAmount")
	public void setRefundAmount(BillRefund instance, Object value) {
		instance.setRefundAmount(toBigDecimal(value));
	}
	
	private BigDecimal toBigDecimal(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof BigDecimal) {
			return (BigDecimal) value;
		}
		try {
			return new BigDecimal(value.toString());
		}
		catch (NumberFormatException e) {
			throw new ConversionException("Cannot convert '" + value + "' to BigDecimal", e);
		}
	}
	
	@Override
	protected AlreadyPaged<BillRefund> doSearch(RequestContext context) {
		String billUuid = context.getRequest().getParameter("bill");
		if (StringUtils.isBlank(billUuid)) {
			throw new InvalidSearchException("'bill' query parameter is required");
		}
		Bill bill = Context.getService(BillService.class).getBillByUuid(billUuid);
		if (bill == null) {
			throw new InvalidSearchException("No bill found with uuid: " + billUuid);
		}
		List<BillRefund> results = Context.getService(BillRefundService.class).getRefundsByBillId(bill.getId());
		return new AlreadyPaged<>(context, results, false);
	}
}
