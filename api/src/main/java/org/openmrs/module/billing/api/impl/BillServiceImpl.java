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
import java.util.List;

/**
 * Default implementation of {@link BillService}.
 * <p>
 * This class delegates to {@link BillDAO} for persistence operations. For detailed documentation of
 * each method, see the interface {@link BillService}.
 * </p>
 *
 * @see BillService
 * @see BillDAO
 */
@Transactional
public class BillServiceImpl extends BaseOpenmrsService implements BillService {
	
	@Setter(onMethod_ = { @Autowired })
	private BillDAO billDAO;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bill getBill(Integer id) {
		if (id == null) {
			return null;
		}
		return billDAO.getBill(id);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bill getBillByUuid(String uuid) {
		if (uuid == null) {
			return null;
		}
		return billDAO.getBillByUuid(uuid);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bill saveBill(Bill bill) {
		if (bill == null) {
			throw new NullPointerException("The bill must be defined.");
		}
		return billDAO.saveBill(bill);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bill getBillByReceiptNumber(String receiptNumber) {
		if (receiptNumber == null) {
			return null;
		}
		return billDAO.getBillByReceiptNumber(receiptNumber);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Bill> getBillsByPatientUuid(String patientUuid, PagingInfo pagingInfo) {
		if (StringUtils.isEmpty(patientUuid)) {
			return Collections.emptyList();
		}
		return billDAO.getBillsByPatientUuid(patientUuid, pagingInfo);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Bill> getBills(BillSearch billSearch, PagingInfo pagingInfo) {
		if (billSearch == null) {
			return Collections.emptyList();
		}
		return billDAO.getBills(billSearch, pagingInfo);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] downloadBillReceipt(Bill bill) {
		if (bill == null) {
			throw new NullPointerException("The bill must be defined.");
		}
		return ReceiptGenerator.createBillReceipt(bill);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void purgeBill(Bill bill) {
		if (bill == null) {
			throw new NullPointerException("The bill must be defined.");
		}
		billDAO.purgeBill(bill);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bill voidBill(Bill bill, String voidReason) {
		if (StringUtils.isBlank(voidReason)) {
			throw new IllegalArgumentException("voidReason cannot be null or empty");
		}
		return billDAO.saveBill(bill);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bill unvoidBill(Bill bill) {
		return billDAO.saveBill(bill);
	}
	
}
