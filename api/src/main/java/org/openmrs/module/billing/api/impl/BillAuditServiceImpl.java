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

import lombok.Setter;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.billing.api.BillAuditService;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.db.BillAuditDAO;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillAudit;
import org.openmrs.module.billing.api.model.BillAuditAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Default implementation of {@link BillAuditService}.
 */
@Transactional
public class BillAuditServiceImpl extends BaseOpenmrsService implements BillAuditService {
	
	@Autowired
	@Setter
	private BillAuditDAO billAuditDAO;
	
	@Override
	@Transactional
	public BillAudit saveBillAudit(BillAudit audit) {
		if (audit == null) {
			throw new NullPointerException("The audit entry must be defined.");
		}
		if (audit.getBill() == null) {
			throw new IllegalArgumentException("The audit entry must be associated with a bill.");
		}
		if (audit.getAction() == null) {
			throw new IllegalArgumentException("The audit entry must have an action type.");
		}
		
		if (audit.getUser() == null) {
			audit.setUser(Context.getAuthenticatedUser());
		}
		if (audit.getAuditDate() == null) {
			audit.setAuditDate(new Date());
		}
		
		return billAuditDAO.saveBillAudit(audit);
	}
	
	@Override
	@Transactional(readOnly = true)
	public BillAudit getBillAudit(Integer id) {
		if (id == null) {
			return null;
		}
		return billAuditDAO.getBillAudit(id);
	}
	
	@Override
	@Transactional(readOnly = true)
	public BillAudit getBillAuditByUuid(String uuid) {
		if (uuid == null) {
			return null;
		}
		return billAuditDAO.getBillAuditByUuid(uuid);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<BillAudit> getBillAuditHistory(Bill bill, PagingInfo pagingInfo) {
		if (bill == null) {
			return Collections.emptyList();
		}
		return billAuditDAO.getBillAuditHistory(bill, pagingInfo);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<BillAudit> getBillAuditsByAction(Bill bill, BillAuditAction action, PagingInfo pagingInfo) {
		if (bill == null || action == null) {
			return Collections.emptyList();
		}
		return billAuditDAO.getBillAuditsByAction(bill, action, pagingInfo);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<BillAudit> getBillAuditsByDateRange(Bill bill, Date startDate, Date endDate, PagingInfo pagingInfo) {
		if (bill == null) {
			return Collections.emptyList();
		}
		return billAuditDAO.getBillAuditsByDateRange(bill, startDate, endDate, pagingInfo);
	}
	
	@Override
	@Transactional
	public BillAudit createBillAudit(Bill bill, BillAuditAction action, String fieldName, String oldValue, String newValue,
	        String reason) {
		if (bill == null) {
			throw new IllegalArgumentException("Bill cannot be null");
		}
		if (action == null) {
			throw new IllegalArgumentException("Action cannot be null");
		}
		
		BillAudit audit = new BillAudit();
		audit.setBill(bill);
		audit.setAction(action);
		audit.setFieldName(fieldName);
		audit.setOldValue(oldValue);
		audit.setNewValue(newValue);
		audit.setReason(reason);
		audit.setUser(Context.getAuthenticatedUser());
		audit.setAuditDate(new Date());
		
		return saveBillAudit(audit);
	}
	
	@Override
	@Transactional
	public void purgeBillAudit(BillAudit audit) {
		if (audit == null) {
			throw new NullPointerException("The audit entry must be defined.");
		}
		billAuditDAO.purgeBillAudit(audit);
	}
}
