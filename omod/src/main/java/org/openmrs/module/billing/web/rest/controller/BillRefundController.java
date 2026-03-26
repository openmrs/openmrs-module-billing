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

import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.web.rest.controller.base.CashierResourceController;
import org.openmrs.module.billing.web.rest.resource.BillResource;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Controller for handling bill refund operations
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + CashierResourceController.BILLING_NAMESPACE + "/bill")
public class BillRefundController extends BaseRestController {
	
	@Autowired
	private BillResource billResource;
	
	@RequestMapping(value = "/{uuid}/requestRefund", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Bill> requestRefund(@PathVariable("uuid") String billUuid,
	        @RequestBody Map<String, String> payload) {
		String refundReason = payload.get("refundReason");
		Bill updatedBill = billResource.requestRefund(billUuid, refundReason);
		return new ResponseEntity<>(updatedBill, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/{uuid}/approveRefund", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Bill> approveRefund(@PathVariable("uuid") String billUuid) {
		Bill updatedBill = billResource.approveRefund(billUuid);
		return new ResponseEntity<>(updatedBill, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/{uuid}/rejectRefund", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Bill> rejectRefund(@PathVariable("uuid") String billUuid) {
		Bill updatedBill = billResource.rejectRefund(billUuid);
		return new ResponseEntity<>(updatedBill, HttpStatus.OK);
	}
}
