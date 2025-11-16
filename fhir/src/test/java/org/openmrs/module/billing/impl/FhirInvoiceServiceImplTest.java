package org.openmrs.module.billing.impl;

import org.hl7.fhir.r4.model.Invoice;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.dao.FhirInvoiceDao;
import org.openmrs.module.billing.translators.InvoiceTranslator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FhirInvoiceServiceImplTest {

	private static final String BILL_UUID = "4028814B39B565A20139B95D74360004";

	private static final String INVALID_UUID = "invalid-uuid-12345";

	@Mock
	private FhirInvoiceDao dao;

	@Mock
	private InvoiceTranslator translator;

	@InjectMocks
	private FhirInvoiceServiceImpl invoiceService;

	private Bill bill;

	private Invoice invoice;

	@Before
	public void setUp() {
		bill = new Bill();
		bill.setUuid(BILL_UUID);
		bill.setReceiptNumber("TEST-001");
		bill.setStatus(BillStatus.POSTED);

		invoice = new Invoice();
		invoice.setId(BILL_UUID);
		invoice.setStatus(Invoice.InvoiceStatus.ISSUED);
	}

	@Test
	public void get_shouldReturnInvoiceWhenBillExists() {
		when(dao.get(BILL_UUID)).thenReturn(bill);
		when(translator.toFhirResource(bill)).thenReturn(invoice);

		Invoice result = invoiceService.get(BILL_UUID);

		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(BILL_UUID));
		assertThat(result.getStatus(), equalTo(Invoice.InvoiceStatus.ISSUED));
	}

}
