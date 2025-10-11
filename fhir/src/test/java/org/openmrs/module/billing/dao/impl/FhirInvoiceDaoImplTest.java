package org.openmrs.module.billing.dao.impl;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.fhir2.TestFhirSpringConfiguration;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;


@ContextConfiguration(classes = TestFhirSpringConfiguration.class, inheritLocations = false)
public class FhirInvoiceDaoImplTest extends BaseModuleContextSensitiveTest {

    private final String TEST_DATASET_ROOT = "org/openmrs/module/billing/include/";

    private final String BILL_UUID = "4028814B39B565A20139B95D74360004";

    private FhirInvoiceDaoImpl fhirInvoiceDao;

    @Autowired
    @Qualifier("sessionFactory")
    private SessionFactory sessionFactory;

    @BeforeEach
    public void setup() {
        fhirInvoiceDao = new FhirInvoiceDaoImpl();
        fhirInvoiceDao.setSessionFactory(sessionFactory);
        executeDataSet(TEST_DATASET_ROOT + "CoreTest-2.0.xml");
        executeDataSet(TEST_DATASET_ROOT + "StockOperationType.xml");
        executeDataSet(TEST_DATASET_ROOT + "PaymentModeTest.xml");
        executeDataSet(TEST_DATASET_ROOT + "CashPointTest.xml");
        executeDataSet(TEST_DATASET_ROOT + "BillTest.xml");
    }

    @Test
    public void getByUuid_shouldReturnCorrectBill() {
        Bill bill = fhirInvoiceDao.get(BILL_UUID);
        assertThat(bill, notNullValue());
        assertThat(bill.getUuid(), equalTo(BILL_UUID));
    }
}
