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

import org.openmrs.Concept;
import org.openmrs.module.billing.api.BillExemptionService;
import org.openmrs.module.billing.api.db.BillExemptionDAO;
import org.openmrs.module.billing.api.model.BillExemption;
import org.openmrs.module.billing.api.model.ExemptionType;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class BillExemptionServiceImpl implements BillExemptionService {
	
	private final BillExemptionDAO billExemptionDAO;
	
	public BillExemptionServiceImpl(BillExemptionDAO billExemptionDAO) {
		this.billExemptionDAO = billExemptionDAO;
	}
	
	@Override
	@Transactional
	public BillExemption save(BillExemption billExemption) {
		return billExemptionDAO.save(billExemption);
	}
	
	@Override
	@Transactional(readOnly = true)
	public BillExemption getBillingExemptionById(Integer id) {
		return billExemptionDAO.getBillingExemptionById(id);
	}
	
	@Override
	@Transactional(readOnly = true)
	public BillExemption getBillingExemptionByUuid(String uuid) {
		return billExemptionDAO.getBillingExemptionByUuid(uuid);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<BillExemption> getExemptionsByConcept(Concept concept, ExemptionType itemType, boolean includeRetired) {
		return billExemptionDAO.getExemptionsByConcept(concept, itemType, includeRetired);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<BillExemption> getExemptionsByItemType(ExemptionType itemType, boolean includeRetired) {
		return billExemptionDAO.getExemptionsByItemType(itemType, includeRetired);
	}
	
}
