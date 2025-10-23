package org.openmrs.module.billing;

import lombok.NonNull;
import org.hl7.fhir.r4.model.Invoice;
import org.openmrs.module.fhir2.api.FhirService;

public interface FhirInvoiceService extends FhirService<Invoice> {

    @Override
    Invoice get(@NonNull String uuid);

}
