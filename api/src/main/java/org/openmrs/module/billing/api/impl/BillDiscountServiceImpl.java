package org.openmrs.module.billing.api.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.openmrs.module.billing.api.BillDiscountService;
import org.openmrs.module.billing.api.db.BillDiscountDAO;
import org.openmrs.module.billing.api.model.BillDiscount;
import org.openmrs.module.billing.api.model.DiscountType;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class BillDiscountServiceImpl implements BillDiscountService {
	
	private final BillDiscountDAO billDiscountDAO;
	
	@Override
	@Transactional(readOnly = true)
	public BillDiscount getBillDiscountById(Integer id) {
		return billDiscountDAO.getBillDiscountById(id);
	}
	
	@Override
	@Transactional(readOnly = true)
	public BillDiscount getBillDiscountByUuid(String uuid) {
		return billDiscountDAO.getBillDiscountByUuid(uuid);
	}
	
	@Override
	@Transactional(readOnly = true)
	public BillDiscount getBillDiscountByBillId(Integer billId) {
		return billDiscountDAO.getBillDiscountByBillId(billId);
	}
	
	@Override
	@Transactional(readOnly = true)
	public BillDiscount getActiveLineItemDiscount(Integer lineItemId) {
		return billDiscountDAO.getActiveLineItemDiscount(lineItemId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<BillDiscount> getDiscountsByBillId(Integer billId) {
		return billDiscountDAO.getDiscountsByBillId(billId);
	}
	
	@Override
	@Transactional
	public BillDiscount saveBillDiscount(BillDiscount billDiscount) {
		// Authoritatively (re)derive the discount amount from value + type + scope total.
		// The caller's discountAmount is never trusted: it's overwritten here before persist.
		computeDiscountAmount(billDiscount);
		return billDiscountDAO.saveBillDiscount(billDiscount);
	}

	private void computeDiscountAmount(BillDiscount discount) {
		if (discount.getBill() == null || discount.getDiscountType() == null || discount.getDiscountValue() == null) {
			return;
		}
		BigDecimal base = discount.getLineItem() != null ? discount.getLineItem().getTotal() : discount.getBill().getTotal();
		if (base == null) {
			return;
		}
		BigDecimal amount = discount.getDiscountType() == DiscountType.PERCENTAGE
		        ? base.multiply(discount.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
		        : discount.getDiscountValue();
		discount.setDiscountAmount(amount);
	}
}
