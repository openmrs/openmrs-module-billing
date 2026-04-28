package org.openmrs.module.billing.web.rest.resource;

import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillDiscountService;
import org.openmrs.module.billing.api.BillLineItemService;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillDiscount;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.DiscountType;
import org.openmrs.module.billing.web.rest.controller.base.CashierResourceController;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * REST resource representing a {@link BillDiscount}.
 */
@Resource(name = RestConstants.VERSION_1 + CashierResourceController.BILLING_NAMESPACE
        + "/billDiscount", supportedClass = BillDiscount.class, supportedOpenmrsVersions = { "2.0 - 2.*" })
public class BillDiscountResource extends DataDelegatingCrudResource<BillDiscount> {
	
	@Override
	public BillDiscount newDelegate() {
		return new BillDiscount();
	}
	
	@Override
	public BillDiscount save(BillDiscount delegate) {
		if (delegate.getId() == null) {
			if (delegate.getInitiator() == null) {
				delegate.setInitiator(Context.getAuthenticatedUser());
			}
			computeDiscountAmount(delegate);
		}
		return Context.getService(BillDiscountService.class).saveBillDiscount(delegate);
	}
	
	@Override
	public BillDiscount getByUniqueId(String uniqueId) {
		return Context.getService(BillDiscountService.class).getBillDiscountByUuid(uniqueId);
	}
	
	@Override
	protected void delete(BillDiscount delegate, String reason, RequestContext context) throws ResponseException {
		if (delegate.getVoided()) {
			return;
		}
		delegate.setVoided(true);
		delegate.setVoidReason(reason);
		Context.getService(BillDiscountService.class).saveBillDiscount(delegate);
	}
	
	@Override
	public void purge(BillDiscount delegate, RequestContext context) throws ResponseException {
		throw new UnsupportedOperationException("Purge is not supported for BillDiscount");
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		
		if (rep instanceof RefRepresentation) {
			description.addProperty("uuid");
			description.addProperty("discountType");
			description.addProperty("discountAmount");
			description.addProperty("voided");
		} else if (rep instanceof DefaultRepresentation) {
			description.addProperty("uuid");
			description.addProperty("bill", Representation.REF);
			description.addProperty("lineItem", Representation.REF);
			description.addProperty("discountType");
			description.addProperty("discountValue");
			description.addProperty("discountAmount");
			description.addProperty("justification");
			description.addProperty("initiator", Representation.REF);
			description.addProperty("approver", Representation.REF);
			description.addProperty("dateCreated");
			description.addProperty("voided");
		} else if (rep instanceof FullRepresentation) {
			description.addProperty("uuid");
			description.addProperty("bill", Representation.DEFAULT);
			description.addProperty("lineItem", Representation.DEFAULT);
			description.addProperty("discountType");
			description.addProperty("discountValue");
			description.addProperty("discountAmount");
			description.addProperty("justification");
			description.addProperty("initiator", Representation.DEFAULT);
			description.addProperty("approver", Representation.DEFAULT);
			description.addProperty("dateCreated");
			description.addProperty("voided");
			description.addProperty("auditInfo");
		}
		
		return description;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("bill");
		description.addProperty("lineItem");
		description.addProperty("discountType");
		description.addProperty("discountValue");
		description.addProperty("justification");
		description.addProperty("approver");
		return description;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("approver");
		return description;
	}
	
	@PropertySetter("discountType")
	public void setDiscountType(BillDiscount instance, String discountType) {
		if (discountType != null) {
			instance.setDiscountType(DiscountType.valueOf(discountType));
		}
	}
	
	@PropertySetter("bill")
	public void setBill(BillDiscount instance, String billUuid) {
		if (billUuid != null) {
			Bill bill = Context.getService(BillService.class).getBillByUuid(billUuid);
			instance.setBill(bill);
		}
	}
	
	@PropertySetter("lineItem")
	public void setLineItem(BillDiscount instance, String lineItemUuid) {
		if (lineItemUuid != null) {
			BillLineItem lineItem = Context.getService(BillLineItemService.class).getBillLineItemByUuid(lineItemUuid);
			instance.setLineItem(lineItem);
		}
	}
	
	@PropertySetter("approver")
	public void setApprover(BillDiscount instance, String approverUuid) {
		if (approverUuid != null) {
			User approver = Context.getUserService().getUserByUuid(approverUuid);
			instance.setApprover(approver);
		}
	}
	
	private void computeDiscountAmount(BillDiscount discount) {
		if (discount.getBill() == null || discount.getDiscountType() == null || discount.getDiscountValue() == null) {
			return;
		}
		
		BigDecimal base = discount.getLineItem() != null ? discount.getLineItem().getTotal() : discount.getBill().getTotal();
		BigDecimal amount;
		
		if (discount.getDiscountType() == DiscountType.PERCENTAGE) {
			amount = base.multiply(discount.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
		} else {
			amount = discount.getDiscountValue();
		}
		
		discount.setDiscountAmount(amount);
	}
}
