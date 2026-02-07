package org.openmrs.module.billing.dao;

import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.fhir2.api.dao.FhirDao;

import javax.annotation.Nonnull;

public interface FhirInvoiceDao extends FhirDao<Bill> {
	
	@Override
	Bill get(@Nonnull String uuid);
	
}
