/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
