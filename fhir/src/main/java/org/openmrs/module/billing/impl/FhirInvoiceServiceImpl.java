/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
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
