package org.openmrs.module.billing.providers;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Invoice;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.billing.FhirInvoiceService;
import org.openmrs.module.fhir2.providers.BaseFhirProvenanceResourceTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InvoiceFhirResourceProviderTest extends BaseFhirProvenanceResourceTest<Invoice> {
    private static final String INVOICE_UUID = "4028814B39B565A20139B95D74360004";

    @Mock
    private FhirInvoiceService fhirInvoiceService;

    private InvoiceFhirResourceProvider invoiceFhirResourceProvider;

    private Invoice invoice;

    @Before
    public void setup() {
        invoiceFhirResourceProvider = new InvoiceFhirResourceProvider(fhirInvoiceService);
        invoice = new Invoice();
        invoice.setId(INVOICE_UUID);
    }

    @Test
    public void getResourceType_shouldReturnInvoiceClass() {
        assertThat(invoiceFhirResourceProvider.getResourceType(), equalTo(Invoice.class));
    }

    @Test
    public void getInvoiceByUuid_shouldReturnMatchingInvoice() {
        IdType idType = new IdType();
        idType.setValue(INVOICE_UUID);

        when(fhirInvoiceService.get(idType.getIdPart())).thenReturn(invoice);

        Invoice result = invoiceFhirResourceProvider.getInvoiceByUuid(idType);

        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(INVOICE_UUID));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void getInvoiceByUuid_shouldThrowResourceNotFoundExceptionIfNotFound() {
        IdType idType = new IdType();
        idType.setValue(INVOICE_UUID);

        when(fhirInvoiceService.get(idType.getIdPart())).thenReturn(null);

        invoiceFhirResourceProvider.getInvoiceByUuid(idType);
    }
}
