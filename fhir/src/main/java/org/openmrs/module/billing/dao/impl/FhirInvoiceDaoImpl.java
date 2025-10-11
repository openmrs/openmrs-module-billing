package org.openmrs.module.billing.dao.impl;

import org.openmrs.module.billing.dao.FhirInvoiceDao;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.fhir2.api.dao.impl.BaseFhirDao;
import org.springframework.stereotype.Component;

@Component
public class FhirInvoiceDaoImpl extends BaseFhirDao<Bill> implements FhirInvoiceDao {

}
