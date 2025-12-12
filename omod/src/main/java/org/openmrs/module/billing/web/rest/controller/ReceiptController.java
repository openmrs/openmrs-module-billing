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
package org.openmrs.module.billing.web.rest.controller;

import java.io.IOException;

import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller to manage the Receipt Generation Page
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/billing/receipt")
public class ReceiptController extends BaseRestController {

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<byte[]> get(@RequestParam(value = "billUuid", required = false) String billUuid)
            throws IOException {
        BillService service = Context.getService(BillService.class);
        Bill bill = service.getBillByUuid(billUuid);

        if (bill == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        byte[] pdfFile = service.downloadBillReceipt(bill);
        if (pdfFile != null && pdfFile.length > 0) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentLength(pdfFile.length);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"receipt-" + bill.getId() + ".pdf\"");

            return new ResponseEntity<>(pdfFile, headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

}
