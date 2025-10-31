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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.IBillService;
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
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<byte[]> get(@RequestParam(value = "billId", required = false) Integer billId) throws IOException {
		
		IBillService service = Context.getService(IBillService.class);
		Bill bill = service.getById(billId);
		
		if (bill == null) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		File file = service.downloadBillReceipt(bill);
		if (file != null && file.exists()) {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentDispositionFormData("attachment", file.getName());
			headers.add("Access-Control-Allow-Origin", "*");
			
			try {
				byte[] fileContent = Files.readAllBytes(file.toPath());
				return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
			}
			catch (IOException e) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
}
