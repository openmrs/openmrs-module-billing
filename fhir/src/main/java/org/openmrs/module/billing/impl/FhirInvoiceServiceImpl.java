package org.openmrs.module.billing.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Invoice;
import org.openmrs.module.billing.FhirInvoiceService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.dao.FhirInvoiceDao;
import org.openmrs.module.billing.translators.InvoiceTranslator;
import org.openmrs.module.fhir2.api.impl.BaseFhirService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
@Getter
public class FhirInvoiceServiceImpl extends BaseFhirService<Invoice, Bill> implements FhirInvoiceService {

    private final FhirInvoiceDao dao;

    private final InvoiceTranslator translator;

}
