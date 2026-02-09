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
package org.openmrs.module.billing.api.impl;

import java.util.Collections;
import java.util.List;

import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.billing.api.BillLineItemService;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.db.BillLineItemDAO;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.validator.BillLineItemValidator;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

public class BillLineItemServiceImpl extends BaseOpenmrsService implements BillLineItemService {
	
	@Setter
	private BillLineItemDAO billLineItemDAO;
	
	@Setter
	private BillLineItemValidator billLineItemValidator;
	
	@Override
	@Transactional(readOnly = true)
	public List<Integer> getPersistedLineItemIds(Integer billId) {
		if (billId == null) {
			return Collections.emptyList();
		}
		return billLineItemDAO.getLineItemIdsByBillId(billId);
	}
	
	@Override
	@Transactional(readOnly = true)
	public BillLineItem getBillLineItemByUuid(String uuid) {
		if (StringUtils.isEmpty(uuid)) {
			return null;
		}
		return billLineItemDAO.getBillLineItemByUuid(uuid);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public Bill voidBillLineItem(String lineItemUuid, String voidReason) {
		BillLineItemValidator.VoidRequest request = new BillLineItemValidator.VoidRequest(lineItemUuid, voidReason);
		BindingResult errors = new BeanPropertyBindingResult(request, "request");
		billLineItemValidator.validate(request, errors);
		if (errors.hasErrors()) {
			String message = errors.getFieldError() != null ? errors.getFieldError().getDefaultMessage()
			        : (errors.getGlobalError() != null ? errors.getGlobalError().getDefaultMessage() : "Validation failed");
			throw new IllegalArgumentException(message);
		}
		
		BillLineItem lineItem = getBillLineItemByUuid(lineItemUuid);
		if (lineItem == null) {
			throw new IllegalArgumentException("Line item with UUID " + lineItemUuid + " not found");
		}
		
		Bill bill = lineItem.getBill();
		if (bill == null) {
			throw new IllegalStateException("Cannot void a line item without an associated bill.");
		}
		
		BillService billService = Context.getService(BillService.class);
		
		lineItem.setVoided(true);
		lineItem.setVoidReason(voidReason);
		
		return billService.saveBill(bill);
	}
}
