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
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.db.BillDAO;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.search.BillSearch;
import org.openmrs.module.billing.util.ReceiptGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Data service implementation class for {@link Bill}s.
 */
@Transactional
public class BillServiceImpl extends BaseOpenmrsService implements BillService {
	
	@Setter(onMethod_ = { @Autowired })
	private BillDAO billDAO;
	
	@Override
	public Bill getBill(Integer id) {
		if (id == null) {
			return null;
		}
		return billDAO.getBill(id);
	}
	
	@Override
	public Bill getBillByUuid(String uuid) {
		if (uuid == null) {
			return null;
		}
		return billDAO.getBillByUuid(uuid);
	}
	
	@Override
	public Bill saveBill(Bill bill) {
		if (bill == null) {
			throw new NullPointerException("The bill must be defined.");
		}
		return billDAO.saveBill(bill);
	}
	
	@Override
	public Bill getBillByReceiptNumber(String receiptNumber) {
		if (receiptNumber == null) {
			return null;
		}
		return billDAO.getBillByReceiptNumber(receiptNumber);
	}
	
	@Override
	public List<Bill> getBillsByPatientId(Integer patientId, PagingInfo pagingInfo) {
		if (patientId == null) {
			return Collections.emptyList();
		}
		return billDAO.getBillsByPatientId(patientId, pagingInfo);
	}
	
	@Override
	public List<Bill> getBills(BillSearch billSearch, PagingInfo pagingInfo) {
		if (billSearch == null) {
			return Collections.emptyList();
		}
		return billDAO.getBills(billSearch, pagingInfo);
	}
	
	@Override
	public byte[] downloadBillReceipt(Bill bill) {
		if (bill == null) {
			throw new NullPointerException("The bill must be defined.");
		}
		return ReceiptGenerator.createBillReceipt(bill);
	}
	
	@Override
	public void purgeBill(Bill bill) {
		if (bill == null) {
			throw new NullPointerException("The bill must be defined.");
		}
		billDAO.purgeBill(bill);
	}
	
	@Override
	public Bill voidBill(Bill bill, String voidReason) {
		if (StringUtils.isBlank(voidReason)) {
			throw new IllegalArgumentException("voidReason cannot be null or empty");
		}
		
		bill.setVoided(true);
		bill.setVoidReason(voidReason);
		return billDAO.saveBill(bill);
	}
	
	@Override
	public Bill unvoidBill(Bill bill) {
		Date voidDate = bill.getDateVoided();
		bill.setVoided(false);
		
		bill.setVoidedBy(null);
		bill.setVoidReason(null);
		bill.setDateVoided(voidDate);
		return billDAO.saveBill(bill);
	}
	
}
