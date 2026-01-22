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
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.billing.api.CashierItemPriceService;
import org.openmrs.module.billing.api.db.CashierItemPriceDAO;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@RequiredArgsConstructor
public class CashierItemPriceServiceImpl extends BaseOpenmrsService implements CashierItemPriceService {
	
	@Setter(onMethod_ = { @Autowired })
	private CashierItemPriceDAO cashierItemPriceDAO;
	
	/** {@inheritDoc} */
	@Override
	public CashierItemPrice getCashierItemPrice(Integer id) {
		if (id == null) {
			return null;
		}
		return cashierItemPriceDAO.getCashierItemPrice(id);
	}
	
	/** {@inheritDoc} */
	@Override
	public CashierItemPrice getCashierItemPriceByUuid(String uuid) {
		if (uuid == null) {
			return null;
		}
		return cashierItemPriceDAO.getCashierItemPriceByUuid(uuid);
	}
	
	/** {@inheritDoc} */
	@Override
	public List<CashierItemPrice> getCashierItemPrices(boolean includeRetired) {
		return cashierItemPriceDAO.getCashierItemPrices(includeRetired);
	}
	
	/** {@inheritDoc} */
	@Override
	public CashierItemPrice saveCashierItemPrice(CashierItemPrice cashierItemPrice) {
		if (cashierItemPrice == null) {
			throw new IllegalArgumentException("CashierItemPrice cannot be null");
		}
		return cashierItemPriceDAO.saveCashierItemPrice(cashierItemPrice);
	}
	
	/** {@inheritDoc} */
	@Override
	public void retireCashierItemPrice(CashierItemPrice cashierItemPrice, String reason) {
		if (cashierItemPrice == null) {
			throw new IllegalArgumentException("CashierItemPrice cannot be null");
		}
		if (StringUtils.isEmpty(reason)) {
			throw new IllegalArgumentException("Reason cannot be empty");
		}
		cashierItemPriceDAO.saveCashierItemPrice(cashierItemPrice);
	}
	
	/** {@inheritDoc} */
	@Override
	public CashierItemPrice unretireCashierItemPrice(CashierItemPrice cashierItemPrice) {
		if (cashierItemPrice == null) {
			throw new IllegalArgumentException("CashierItemPrice cannot be null");
		}
		return cashierItemPriceDAO.saveCashierItemPrice(cashierItemPrice);
	}
	
	/** {@inheritDoc} */
	@Override
	public void purgeCashierItemPrice(CashierItemPrice cashierItemPrice) {
		if (cashierItemPrice == null) {
			throw new IllegalArgumentException("CashierItemPrice cannot be null");
		}
		cashierItemPriceDAO.purgeCashierItemPrice(cashierItemPrice);
	}
}
