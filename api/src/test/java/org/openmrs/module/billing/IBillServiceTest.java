/*
* The contents of this file are subject to the OpenMRS Public License
* Version 1.1 (the "License"); you may not use this file except in
* compliance with the License. You may obtain a copy of the License at
* http://license.openmrs.org
*
* Software distributed under the License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
* License for the specific language governing rights and limitations
* under the License.
*
* Copyright (C) OpenMRS, LLC.  All Rights Reserved.
*/
package org.openmrs.module.billing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.Patient;
import org.openmrs.module.billing.api.IBillService;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.search.BillSearch;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IBillServiceTest {

    @Mock
    private IBillService billService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Define mock behavior for throwing IllegalArgumentException and NullPointerException
        lenient().doThrow(new IllegalArgumentException("Receipt number cannot be null"))
            .when(billService).getBillByReceiptNumber(null);
        lenient().doThrow(new IllegalArgumentException("Receipt number cannot be empty"))
            .when(billService).getBillByReceiptNumber("");
        lenient().doThrow(new IllegalArgumentException("Receipt number cannot be longer than 255 characters"))
            .when(billService).getBillByReceiptNumber(argThat(receiptNumber -> receiptNumber != null && receiptNumber.length() > 255));
        lenient().doThrow(new NullPointerException("Patient ID cannot be Null"))
            .when(billService).getBillsByPatient(isNull(), any(PagingInfo.class));
        lenient().doThrow(new IllegalArgumentException("Patient ID cannot be negative"))
            .when(billService).getBillsByPatientId(-1, new PagingInfo());
        lenient().doThrow(new NullPointerException("Bill search cannot be null"))
            .when(billService).getBills(isNull(), any(PagingInfo.class));
        lenient().doThrow(new NullPointerException("Bill search template cannot be null"))
            .when(billService).getBills(argThat(billSearch -> billSearch != null && billSearch.getTemplate() == null), any(PagingInfo.class));
    }

    @Test
    public void getBillByReceiptNumber_shouldThrowExceptionIfReceiptNumberIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            billService.getBillByReceiptNumber(null);
        });
    }

    @Test
    public void getBillByReceiptNumber_shouldThrowExceptionIfReceiptNumberIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            billService.getBillByReceiptNumber("");
        });
    }

    @Test
    public void getBillByReceiptNumber_shouldThrowExceptionIfReceiptNumberIsTooLong() {
        String longReceiptNumber = new String(new char[256]).replace('\0', 'a');
        assertThrows(IllegalArgumentException.class, () -> {
            billService.getBillByReceiptNumber(longReceiptNumber);
        });
    }

    @Test
    public void getBillByReceiptNumber_shouldReturnBillIfFound() {
        String receiptNumber = "123456";
        Bill bill = new Bill();
        bill.setReceiptNumber(receiptNumber);
        when(billService.getBillByReceiptNumber(receiptNumber)).thenReturn(bill);

        Bill result = billService.getBillByReceiptNumber(receiptNumber);
        assertNotNull(result);
        assertEquals(receiptNumber, result.getReceiptNumber());
    }

    @Test
    public void getBillByReceiptNumber_shouldReturnNullIfNotFound() {
        String receiptNumber = "123456";
        when(billService.getBillByReceiptNumber(receiptNumber)).thenReturn(null);

        Bill result = billService.getBillByReceiptNumber(receiptNumber);
        assertNull(result);
    }

    @Test
    public void getBillsByPatient_shouldThrowExceptionIfPatientIsNull() {
        assertThrows(NullPointerException.class, () -> {
            billService.getBillsByPatient(null, new PagingInfo());
        });
    }

    @Test
    public void getBillsByPatient_shouldReturnBillsIfPatientHasBills() {
        Patient patient = new Patient();
        PagingInfo paging = new PagingInfo();
        List<Bill> bills = Collections.singletonList(new Bill());
        when(billService.getBillsByPatient(patient, paging)).thenReturn(bills);

        List<Bill> result = billService.getBillsByPatient(patient, paging);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void getBillsByPatient_shouldReturnEmptyListIfPatientHasNoBills() {
        Patient patient = new Patient();
        PagingInfo paging = new PagingInfo();
        when(billService.getBillsByPatient(patient, paging)).thenReturn(Collections.emptyList());

        List<Bill> result = billService.getBillsByPatient(patient, paging);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getBillsByPatientId_shouldThrowExceptionIfPatientIdIsNegative() {
            billService.getBillsByPatientId(-1, new PagingInfo());
    }

    @Test
    public void getBillsByPatientId_shouldReturnBillsIfPatientHasBills() {
        int patientId = 1;
        PagingInfo paging = new PagingInfo();
        List<Bill> bills = Collections.singletonList(new Bill());
        when(billService.getBillsByPatientId(patientId, paging)).thenReturn(bills);

        List<Bill> result = billService.getBillsByPatientId(patientId, paging);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void getBillsByPatientId_shouldReturnEmptyListIfPatientHasNoBills() {
        int patientId = 1;
        PagingInfo paging = new PagingInfo();
        when(billService.getBillsByPatientId(patientId, paging)).thenReturn(Collections.emptyList());

        List<Bill> result = billService.getBillsByPatientId(patientId, paging);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getBills_shouldThrowExceptionIfBillSearchIsNull() {
        assertThrows(NullPointerException.class, () -> {
            billService.getBills(null, new PagingInfo());
        });
    }

    @Test
    public void getBills_shouldThrowExceptionIfBillSearchTemplateIsNull() {
        BillSearch billSearch = new BillSearch();
        billSearch.setTemplate(null);
        assertThrows(NullPointerException.class, () -> {
            billService.getBills(billSearch, new PagingInfo());
        });
    }

    @Test
    public void getBills_shouldReturnBillsIfFound() {
        BillSearch billSearch = new BillSearch();
        PagingInfo paging = new PagingInfo();
        List<Bill> bills = Collections.singletonList(new Bill());
        when(billService.getBills(billSearch, paging)).thenReturn(bills);

        List<Bill> result = billService.getBills(billSearch, paging);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void getBills_shouldReturnEmptyListIfNoBillsFound() {
        BillSearch billSearch = new BillSearch();
        PagingInfo paging = new PagingInfo();
        when(billService.getBills(billSearch, paging)).thenReturn(Collections.emptyList());

        List<Bill> result = billService.getBills(billSearch, paging);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void downloadBillReceipt_shouldReturnReceiptFile() {
        Bill bill = new Bill();
        File receiptFile = new File("receipt.pdf");
        when(billService.downloadBillReceipt(bill)).thenReturn(receiptFile);

        File result = billService.downloadBillReceipt(bill);
        assertNotNull(result);
        assertEquals(receiptFile, result);
    }
}
