/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.impl;

import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.billing.api.BillableServiceService;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.db.BillableServiceDAO;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.search.BillableServiceSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

public class BillableServiceServiceImpl extends BaseOpenmrsService implements BillableServiceService {
	
	@Setter(onMethod_ = { @Autowired })
	private BillableServiceDAO billableServiceDAO;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public BillableService getBillableService(Integer id) {
		if (id == null) {
			return null;
		}
		return billableServiceDAO.getBillableService(id);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public BillableService getBillableServiceByUuid(String uuid) {
		if (StringUtils.isEmpty(uuid)) {
			return null;
		}
		return billableServiceDAO.getBillableServiceByUuid(uuid);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<BillableService> getBillableServices(BillableServiceSearch billableServiceSearch, PagingInfo pagingInfo) {
		if (billableServiceSearch == null) {
			return Collections.emptyList();
		}
		return billableServiceDAO.getBillableServices(billableServiceSearch, pagingInfo);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public BillableService saveBillableService(BillableService billableService) {
		if (billableService == null) {
			throw new NullPointerException("The billableService must be defined.");
		}
		return billableServiceDAO.saveBillableService(billableService);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public void purgeBillableService(BillableService billableService) {
		if (billableService == null) {
			throw new NullPointerException("The billableService must be defined.");
		}
		billableServiceDAO.purgeBillableService(billableService);
	}
	
	@Override
	@Transactional
	public BillableService retireBillableService(BillableService billableService, String reason) {
		if (StringUtils.isEmpty(reason)) {
			throw new IllegalArgumentException("Retire reason cannot be empty or null");
		}
		return billableServiceDAO.saveBillableService(billableService);
	}
	
	@Override
	@Transactional
	public BillableService unretireBillableService(BillableService billableService) {
		return billableServiceDAO.saveBillableService(billableService);
	}
}
