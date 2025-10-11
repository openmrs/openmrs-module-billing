package org.openmrs.module.billing.translator.impl;

import org.hl7.fhir.r4.model.Invoice;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.translators.impl.BillTranslatorImpl;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BillTranslatorImplTest {

    @Mock
    private PractitionerReferenceTranslator<Provider> practitionerTranslator;

    @Mock
    private PatientReferenceTranslator patientReferenceTranslator;

    private BillTranslatorImpl translator;

    private Bill bill;

    private Provider provider;

    private Patient patient;

    private static final String BILL_UUID = "4028814B39B565A20139B95D74360004";

    @Before
    public void setUp() {
        translator = new BillTranslatorImpl(practitionerTranslator, patientReferenceTranslator);
        provider = new Provider();
        patient = new Patient();
        bill = new Bill();
        bill.setUuid(BILL_UUID);
        bill.setReceiptNumber("TEST-001");
        bill.setStatus(BillStatus.POSTED);
        bill.setCashier(provider);
        bill.setPatient(patient);

    }

    @Test
    public void shouldTranslateBillToInvoice() {
        when(practitionerTranslator.toFhirResource(provider)).thenReturn(new Reference());
        when(patientReferenceTranslator.toFhirResource(patient)).thenReturn(new Reference());

        Invoice invoice = translator.toFhirResource(bill);
        assertThat(invoice, notNullValue());
        assertThat(invoice.getId(), equalTo(BILL_UUID));
    }

}
