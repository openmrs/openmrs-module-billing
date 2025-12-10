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

import lombok.RequiredArgsConstructor;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.billing.api.BillLineItemService;
import org.openmrs.module.billing.api.db.BillLineItemDAO;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.springframework.transaction.annotation.Transactional;

/**
 * Standard implementation of the {@link BillLineItemService} interface.
 * <p>
 * This implementation delegates data access operations to the {@link BillLineItemDAO} and provides
 * business logic for managing bill line items.
 * </p>
 *
 * @see BillLineItemService
 */
@Transactional
@RequiredArgsConstructor
public class BillLineItemServiceImpl extends BaseOpenmrsService implements BillLineItemService {
	
	private final BillLineItemDAO billLineItemDAO;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public BillLineItem getBillLineItem(Integer id) {
		if (id == null) {
			return null;
		}
		return billLineItemDAO.getBillLineItem(id);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public BillLineItem getBillLineItemByUuid(String uuid) {
		if (uuid == null) {
			return null;
		}
		return billLineItemDAO.getBillLineItemByUuid(uuid);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public BillLineItem saveBillLineItem(BillLineItem billLineItem) {
		if (billLineItem == null) {
			throw new NullPointerException("The bill must be defined.");
		}
		return billLineItemDAO.saveBillLineItem(billLineItem);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public BillLineItem voidBillLineItem(BillLineItem billLineItem, String voidReason) {
		if (voidReason == null) {
			throw new NullPointerException("The void reason must be defined.");
		}
		return billLineItemDAO.saveBillLineItem(billLineItem);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public BillLineItem unvoidBillLineItem(BillLineItem billLineItem) {
		return billLineItemDAO.saveBillLineItem(billLineItem);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void purgeBillLineItem(BillLineItem billLineItem) {
		billLineItemDAO.purgeBillLineItem(billLineItem);
	}
}
