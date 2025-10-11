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
import org.openmrs.module.billing.translators.BillTranslator;

import javax.annotation.Nonnull;

@Slf4j
@Component
@Setter
@RequiredArgsConstructor
public class BillTranslatorImpl implements BillTranslator {


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
          for (int i = 0; i < bill.getLineItems().size(); i++) {
              BillLineItem billLineItem = bill.getLineItems().get(i);
              Invoice.InvoiceLineItemComponent invoiceLineItemComponent = new Invoice.InvoiceLineItemComponent();
              invoiceLineItemComponent.setSequence(i + 1);

              if (billLineItem.getItem() != null && billLineItem.getItem().getCommonName() != null) {
                  CodeableConcept codeableConcept = new CodeableConcept();
                  codeableConcept.addCoding(new Coding().setCode(billLineItem.getUuid()).setDisplay(billLineItem.getItem().getCommonName()));
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
            // Not sure if this is correct
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
                return Invoice.InvoiceStatus.ISSUED;
            case PAID:
                return Invoice.InvoiceStatus.BALANCED;
            case CANCELLED:
            case ADJUSTED:
                return Invoice.InvoiceStatus.CANCELLED;
            case EXEMPTED:
                return Invoice.InvoiceStatus.ENTEREDINERROR;
            default:
                return Invoice.InvoiceStatus.DRAFT;
        }
    }

    @Override
    public Bill toOpenmrsType(@Nonnull Invoice invoice) {
        return null;
    }
}
