package org.openmrs.module.billing.providers;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Invoice;
import org.openmrs.module.billing.FhirInvoiceService;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component("InvoiceFhirR4ResourceProvider")
@R4Provider
@Setter
@RequiredArgsConstructor
public class InvoiceFhirResourceProvider implements IResourceProvider {
	
	private final FhirInvoiceService fhirInvoiceService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Invoice.class;
	}
	
	@Read
	public Invoice getInvoiceByUuid(@IdParam @Nonnull IdType id) {
		Invoice invoice = fhirInvoiceService.get(id.getIdPart());
		if (invoice == null) {
			throw new ResourceNotFoundException("Could not find an invoice with Id: " + id.getIdPart());
		}
		return invoice;
	}
}
