/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.web.rest.controller;

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
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<byte[]> get(@RequestParam(value = "billUuid", required = false) String billUuid) {
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
