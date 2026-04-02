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
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.db.BillDAO;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.search.BillSearch;
import org.openmrs.module.billing.util.ReceiptGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
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
	@Transactional(readOnly = true)
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
	@Transactional(readOnly = true)
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
	@Transactional
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
	@Transactional(readOnly = true)
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
	@Transactional(readOnly = true)
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
	@Transactional(readOnly = true)
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
	@Transactional(readOnly = true)
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
	@Transactional
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
	@Transactional
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
	@Transactional
	public Bill unvoidBill(Bill bill) {
		return billDAO.saveBill(bill);
	}
	
	@Override
	@Transactional(readOnly = true)
	public boolean isBillEditable(Bill bill) {
		if (bill == null) {
			throw new IllegalArgumentException("Bill cannot be null");
		}
		if (bill.getId() != null) {
			Bill existingBill = Context.getService(BillService.class).getBill(bill.getBillId());
			return existingBill == null || existingBill.editable();
		}
		return true;
	}
	
	@Override
	@Transactional
	public Bill requestRefund(Bill bill, String refundReason) {
		if (bill == null) {
			throw new IllegalArgumentException("The bill must be defined.");
		}
		if (StringUtils.isBlank(refundReason)) {
			throw new IllegalArgumentException("refundReason cannot be null or empty");
		}
		if (bill.getStatus() != BillStatus.PAID) {
			throw new IllegalArgumentException(
			        "Only PAID bills can have a refund requested. Current status: " + bill.getStatus());
		}
		bill.setRefundReason(refundReason);
		bill.setRefundRequestedBy(Context.getAuthenticatedUser());
		bill.setDateRefundRequested(new Date());
		bill.setStatus(BillStatus.REFUND_REQUESTED);
		return billDAO.saveBill(bill);
	}
	
	@Override
	@Transactional
	public Bill approveRefund(Bill bill) {
		if (bill == null) {
			throw new IllegalArgumentException("The bill must be defined.");
		}
		if (bill.getStatus() != BillStatus.REFUND_REQUESTED) {
			throw new IllegalArgumentException(
			        "Only bills with REFUND_REQUESTED status can be approved. Current status: " + bill.getStatus());
		}
		bill.setRefundApprovedBy(Context.getAuthenticatedUser());
		bill.setDateRefundApproved(new Date());
		bill.setStatus(BillStatus.REFUNDED);
		return billDAO.saveBill(bill);
	}
	
	@Override
	@Transactional
	public Bill rejectRefund(Bill bill, String denialReason) {
		if (bill == null) {
			throw new IllegalArgumentException("The bill must be defined.");
		}
		if (StringUtils.isBlank(denialReason)) {
			throw new IllegalArgumentException("denialReason cannot be null or empty");
		}
		if (bill.getStatus() != BillStatus.REFUND_REQUESTED) {
			throw new IllegalArgumentException(
			        "Only bills with REFUND_REQUESTED status can be rejected. Current status: " + bill.getStatus());
		}
		bill.setRefundDenialReason(denialReason);
		bill.setRefundRejectedBy(Context.getAuthenticatedUser());
		bill.setDateRefundRejected(new Date());
		bill.setStatus(BillStatus.REFUND_DENIED);
		return billDAO.saveBill(bill);
	}
	
}
