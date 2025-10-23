package org.openmrs.module.billing.translators;

import org.hl7.fhir.r4.model.Invoice;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.fhir2.api.translators.OpenmrsFhirTranslator;

public interface InvoiceTranslator extends OpenmrsFhirTranslator<Bill, Invoice> {
}
