package org.openmrs.module.billing.web.rest.resource;

import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillLineItemService;
import org.openmrs.module.billing.api.IBillService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;

import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.webservices.rest.web.RequestContext;
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
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.math.BigDecimal;
import java.util.ArrayList;

@SubResource(parent = BillResource.class, path = "lineItem", supportedClass = BillLineItem.class,
        supportedOpenmrsVersions = {"2.0 - 2.*"})
public class BillLineItemNestedResource extends DelegatingSubResource<BillLineItem, Bill, BillResource> {

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            description.addProperty("uuid");
            description.addProperty("quantity");
            description.addProperty("price");
            description.addProperty("lineItemOrder");
            description.addProperty("paymentStatus");
            description.addProperty("item");
            description.addProperty("billableService", Representation.REF);
            description.addProperty("discount");
            description.addProperty("discountReason");
        }
        return description;
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("quantity");
        description.addProperty("price");
        description.addProperty("lineItemOrder");
        description.addProperty("paymentStatus");
        description.addProperty("discount");
        description.addProperty("discountReason");
        return description;
    }

    @Override
    public BillLineItem save(BillLineItem lineItem) {
        IBillService billService = Context.getService(IBillService.class);
        Bill bill = lineItem.getBill();

        // Validate bill is editable only if bill is PENDING or POSTED
        if (bill == null || !bill.editable()) {
            throw new IllegalArgumentException("Bill is not editable");
        }

        // Save the line item
        BillLineItemService service = Context.getService(BillLineItemService.class);
        return service.save(lineItem);
    }

    @Override
    public void delete(String parentUniqueId, final String uuid, String reason, RequestContext context) {
        IBillService billService = Context.getService(IBillService.class);
        Bill bill = findBill(billService, parentUniqueId);
        BillLineItem lineItem = findLineItem(bill, uuid);

        // Void the line item (soft delete)
        lineItem.setVoided(true);
        lineItem.setVoidReason(reason);
        lineItem.setVoidedBy(Context.getAuthenticatedUser());

        // Save the bill to persist the voided status
        billService.save(bill);
    }

    @PropertySetter(value = "quantity")
    public void setQuantity(BillLineItem instance, Integer quantity) {
        instance.setQuantity(quantity);
    }

    @PropertySetter(value = "price")
    public void setPrice(BillLineItem instance, Object price) {
        if (price instanceof Double || price instanceof Integer) {
            double priceValue = ((Number) price).doubleValue();
            instance.setPrice(BigDecimal.valueOf(priceValue));
        } else {
            throw new IllegalArgumentException("Unsupported price type: " + price.getClass().getName());
        }
    }

    @PropertySetter(value = "lineItemOrder")
    public void setLineItemOrder(BillLineItem instance, Integer order) {
        instance.setLineItemOrder(order);
    }

    @PropertySetter(value = "paymentStatus")
    public void setPaymentStatus(BillLineItem instance, BillStatus status) {
        instance.setPaymentStatus(status);
    }

    @PropertySetter(value = "discount")
    public void setDiscount(BillLineItem instance, Object discount) {
        if (discount == null) {
            instance.setDiscount(null);
        } else if (discount instanceof Double || discount instanceof Integer) {
            double discountValue = ((Number) discount).doubleValue();
            instance.setDiscount(BigDecimal.valueOf(discountValue));
        } else {
            throw new IllegalArgumentException("Unsupported discount type: " + discount.getClass().getName());
        }
    }

    @Override
    public PageableResult doGetAll(Bill parent, RequestContext context) {
        return new AlreadyPaged<BillLineItem>(context,
                new ArrayList<BillLineItem>(parent.getLineItems()), false);
    }

    @Override
    public BillLineItem getByUniqueId(String uuid) {
        return Context.getService(BillLineItemService.class).getByUuid(uuid);
    }

    @Override
    protected void delete(BillLineItem billLineItem, String s, RequestContext requestContext) throws ResponseException {

    }

    @Override
    public void purge(BillLineItem billLineItem, RequestContext requestContext) throws ResponseException {

    }

    @Override
    public Bill getParent(BillLineItem instance) {
        return instance.getBill();
    }

    @Override
    public void setParent(BillLineItem instance, Bill parent) {
        instance.setBill(parent);
    }

    @Override
    public BillLineItem newDelegate() {
        return new BillLineItem();
    }

    private Bill findBill(IBillService service, String billUUID) {
        Bill bill = service.getByUuid(billUUID);
        if (bill == null) {
            throw new ObjectNotFoundException();
        }
        return bill;
    }

    private BillLineItem findLineItem(Bill bill, final String lineItemUUID) {
        for (BillLineItem item : bill.getLineItems()) {
            if (item != null && item.getUuid().equals(lineItemUUID)) {
                return item;
            }
        }
        throw new ObjectNotFoundException();
    }
}