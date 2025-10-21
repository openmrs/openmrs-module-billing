package org.openmrs.module.billing.translators.impl;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Invoice;
import org.hl7.fhir.r4.model.Money;
import org.openmrs.Provider;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.springframework.stereotype.Component;
import org.openmrs.module.billing.translators.InvoiceTranslator;

import javax.annotation.Nonnull;

@Slf4j
@Component
@Setter
@RequiredArgsConstructor
public class InvoiceTranslatorImpl implements InvoiceTranslator {


    private final PractitionerReferenceTranslator<Provider> practitionerReferenceTranslator;

    private final PatientReferenceTranslator patientReferenceTranslator;

    @Override
    public Invoice toFhirResource(@Nonnull Bill bill) {
        Validate.notNull(bill, "Bill cannot be null");

        Invoice invoice = new Invoice();
        invoice.setId(bill.getUuid());
        invoice.setStatus(mapStatus(bill.getStatus()));

        if (bill.getCashier() != null) {
            invoice.addParticipant().setActor(practitionerReferenceTranslator.toFhirResource(bill.getCashier()));
        }
        if (bill.getPatient() != null) {
            invoice.setSubject(patientReferenceTranslator.toFhirResource(bill.getPatient()));
            invoice.setRecipient(patientReferenceTranslator.toFhirResource(bill.getPatient()));
        }

        if (bill.getLineItems() != null) {
            for (BillLineItem billLineItem : bill.getLineItems()) {
                Invoice.InvoiceLineItemComponent invoiceLineItemComponent = new Invoice.InvoiceLineItemComponent();
                invoiceLineItemComponent.setSequence(billLineItem.getLineItemOrder());
                if (billLineItem.getItem() != null) {
                    CodeableConcept codeableConcept = new CodeableConcept();
                    Coding coding  = new Coding().setCode(billLineItem.getUuid());
                    if (billLineItem.getItem().getConcept() != null) {
                        coding.setDisplay(billLineItem.getItem().getCommonName());
                    }
                    codeableConcept.addCoding(coding);
                    invoiceLineItemComponent.setChargeItem(codeableConcept);
                }
                Invoice.InvoiceLineItemPriceComponentComponent priceComponent = new Invoice.InvoiceLineItemPriceComponentComponent();
                priceComponent.setCode(new CodeableConcept().addCoding(new Coding().setCode(billLineItem.getUuid())));

                if (billLineItem.getPrice() != null) {
                    priceComponent.setFactor(billLineItem.getPrice());
                    invoiceLineItemComponent.addPriceComponent(priceComponent);
                }
                invoice.addLineItem(invoiceLineItemComponent);
            }
        }


        if (bill.getTotal() != null) {
            Money totalNet = new Money();
            totalNet.setValue(bill.getTotal());
            invoice.setTotalNet(totalNet);
            invoice.setTotalGross(totalNet);
        }

        invoice.setDate(bill.getDateCreated());
        return invoice;
    }

    private Invoice.InvoiceStatus mapStatus(BillStatus status) {
        if (status == null) {
            return Invoice.InvoiceStatus.DRAFT;
        }
        switch (status) {
            case POSTED:
            case ADJUSTED:
                return Invoice.InvoiceStatus.ISSUED;
            case PAID:
            case EXEMPTED:
                return Invoice.InvoiceStatus.BALANCED;
            case CANCELLED:
                return Invoice.InvoiceStatus.CANCELLED;
            default:
                return Invoice.InvoiceStatus.DRAFT;
        }
    }

    @Override
    public Bill toOpenmrsType(@Nonnull Invoice invoice) {
        return null;
    }
}
