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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.billing.api.BillAuditService;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.db.BillDAO;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillAuditAction;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.Payment;
import org.openmrs.module.billing.api.search.BillSearch;
import org.openmrs.module.billing.util.ReceiptGenerator;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
	
	private BillDAO billDAO;
	
	private BillAuditService billAuditService;
	
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	public void setBillDAO(BillDAO billDAO) {
		this.billDAO = billDAO;
	}
	
	public void setBillAuditService(BillAuditService billAuditService) {
		this.billAuditService = billAuditService;
	}
	
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
		
		Bill existingBill = null;
		if (bill.getId() != null) {
			existingBill = billDAO.getBill(bill.getId());
		}
		
		Bill savedBill = billDAO.saveBill(bill);
		
		if (billAuditService != null) {
			if (existingBill == null) {
				createAuditForNewBill(savedBill);
			} else {
				createAuditsForBillModification(existingBill, savedBill);
			}
		}
		
		return savedBill;
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
		
		if (billAuditService != null) {
			billAuditService.createBillAudit(bill, BillAuditAction.BILL_VOIDED, null, null, null, voidReason);
		}
		
		return billDAO.saveBill(bill);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public Bill unvoidBill(Bill bill) {
		if (billAuditService != null) {
			billAuditService.createBillAudit(bill, BillAuditAction.BILL_UNVOIDED, null, null, null, null);
		}
		
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
	
	private void createAuditForNewBill(Bill bill) {
		billAuditService.createBillAudit(bill, BillAuditAction.BILL_CREATED, null, null, toJson(createBillSummary(bill)),
		    null);
	}
	
	private void createAuditsForBillModification(Bill oldBill, Bill newBill) {
		String reason = newBill.getAdjustmentReason();
		
		if (oldBill.getStatus() != newBill.getStatus()) {
			billAuditService.createBillAudit(newBill, BillAuditAction.STATUS_CHANGED, "status",
			    oldBill.getStatus() != null ? oldBill.getStatus().toString() : null,
			    newBill.getStatus() != null ? newBill.getStatus().toString() : null, reason);
		}
		
		detectLineItemChanges(oldBill, newBill, reason);
		detectPaymentChanges(oldBill, newBill, reason);
		
		if (!Objects.equals(oldBill.getAdjustmentReason(), newBill.getAdjustmentReason())) {
			billAuditService.createBillAudit(newBill, BillAuditAction.ADJUSTMENT_REASON_UPDATED, "adjustmentReason",
			    oldBill.getAdjustmentReason(), newBill.getAdjustmentReason(), reason);
		}
		
		if (newBill.getBillAdjusted() != null && oldBill.getBillAdjusted() == null) {
			billAuditService.createBillAudit(newBill, BillAuditAction.BILL_ADJUSTED, null, null,
			    "Adjusted bill: " + newBill.getBillAdjusted().getUuid(), reason);
		}
	}
	
	private void detectLineItemChanges(Bill oldBill, Bill newBill, String reason) {
		List<BillLineItem> oldItems = oldBill.getLineItems() != null ? oldBill.getLineItems() : Collections.emptyList();
		List<BillLineItem> newItems = newBill.getLineItems() != null ? newBill.getLineItems() : Collections.emptyList();
		
		Map<Integer, BillLineItem> oldItemsMap = createLineItemMap(oldItems);
		Map<Integer, BillLineItem> newItemsMap = createLineItemMap(newItems);
		
		for (BillLineItem newItem : newItems) {
			if (newItem.getVoided()) {
				continue;
			}
			if (newItem.getId() == null || !oldItemsMap.containsKey(newItem.getId())) {
				billAuditService.createBillAudit(newBill, BillAuditAction.LINE_ITEM_ADDED, "lineItem", null,
				    toJson(createLineItemSummary(newItem)), reason);
			} else {
				BillLineItem oldItem = oldItemsMap.get(newItem.getId());
				detectLineItemModifications(oldItem, newItem, newBill, reason);
			}
		}
		
		for (BillLineItem oldItem : oldItems) {
			if (oldItem.getVoided()) {
				continue;
			}
			if (oldItem.getId() != null && !newItemsMap.containsKey(oldItem.getId())) {
				billAuditService.createBillAudit(newBill, BillAuditAction.LINE_ITEM_REMOVED, "lineItem",
				    toJson(createLineItemSummary(oldItem)), null, reason);
			}
		}
	}
	
	private void detectLineItemModifications(BillLineItem oldItem, BillLineItem newItem, Bill bill, String reason) {
		if (!Objects.equals(oldItem.getQuantity(), newItem.getQuantity())) {
			billAuditService.createBillAudit(bill, BillAuditAction.QUANTITY_CHANGED,
			    "lineItem[" + newItem.getId() + "].quantity", String.valueOf(oldItem.getQuantity()),
			    String.valueOf(newItem.getQuantity()), reason);
		}
		
		if (oldItem.getPrice() != null && newItem.getPrice() != null
		        && oldItem.getPrice().compareTo(newItem.getPrice()) != 0) {
			billAuditService.createBillAudit(bill, BillAuditAction.PRICE_CHANGED, "lineItem[" + newItem.getId() + "].price",
			    oldItem.getPrice().toString(), newItem.getPrice().toString(), reason);
		}
	}
	
	private void detectPaymentChanges(Bill oldBill, Bill newBill, String reason) {
		Set<Payment> oldPayments = oldBill.getPayments() != null ? oldBill.getPayments() : Collections.emptySet();
		Set<Payment> newPayments = newBill.getPayments() != null ? newBill.getPayments() : Collections.emptySet();
		
		Set<String> oldPaymentUuids = oldPayments.stream().filter(p -> !p.getVoided()).map(Payment::getUuid)
		        .collect(Collectors.toSet());
		Set<String> newPaymentUuids = newPayments.stream().filter(p -> !p.getVoided()).map(Payment::getUuid)
		        .collect(Collectors.toSet());
		
		for (Payment payment : newPayments) {
			if (!payment.getVoided() && !oldPaymentUuids.contains(payment.getUuid())) {
				billAuditService.createBillAudit(newBill, BillAuditAction.PAYMENT_ADDED, "payment", null,
				    toJson(createPaymentSummary(payment)), reason);
			}
		}
		
		for (Payment payment : oldPayments) {
			if (!payment.getVoided() && !newPaymentUuids.contains(payment.getUuid())) {
				billAuditService.createBillAudit(newBill, BillAuditAction.PAYMENT_REMOVED, "payment",
				    toJson(createPaymentSummary(payment)), null, reason);
			}
		}
	}
	
	private Map<Integer, BillLineItem> createLineItemMap(List<BillLineItem> items) {
		if (items == null) {
			return Collections.emptyMap();
		}
		return items.stream().filter(item -> item.getId() != null && !item.getVoided())
		        .collect(Collectors.toMap(BillLineItem::getId, item -> item));
	}
	
	private Map<String, Object> createBillSummary(Bill bill) {
		Map<String, Object> summary = new HashMap<>();
		summary.put("uuid", bill.getUuid());
		summary.put("receiptNumber", bill.getReceiptNumber());
		summary.put("status", bill.getStatus() != null ? bill.getStatus().toString() : null);
		summary.put("total", bill.getTotal());
		return summary;
	}
	
	private Map<String, Object> createLineItemSummary(BillLineItem item) {
		Map<String, Object> summary = new HashMap<>();
		summary.put("uuid", item.getUuid());
		if (item.getItem() != null && item.getItem().getDrug() != null) {
			summary.put("itemName", item.getItem().getDrug().getName());
		} else if (item.getBillableService() != null) {
			summary.put("itemName", item.getBillableService().getName());
		} else {
			summary.put("itemName", "Unknown Item");
		}
		summary.put("quantity", item.getQuantity());
		summary.put("price", item.getPrice());
		summary.put("total", item.getTotal());
		return summary;
	}
	
	private Map<String, Object> createPaymentSummary(Payment payment) {
		Map<String, Object> summary = new HashMap<>();
		summary.put("uuid", payment.getUuid());
		summary.put("amount", payment.getAmountTendered());
		summary.put("paymentMode", payment.getInstanceType() != null ? payment.getInstanceType().getName() : "Unknown");
		return summary;
	}
	
	private String toJson(Object obj) {
		try {
			return objectMapper.writeValueAsString(obj);
		}
		catch (JsonProcessingException e) {
			return obj != null ? obj.toString() : null;
		}
	}
}
