package org.openmrs.module.billing.api.impl;

import lombok.RequiredArgsConstructor;
import org.openmrs.module.billing.api.BillDiscountService;
import org.openmrs.module.billing.api.db.BillDiscountDAO;
import org.openmrs.module.billing.api.model.BillDiscount;
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
	@Transactional
	public BillDiscount saveBillDiscount(BillDiscount billDiscount) {
		return billDiscountDAO.saveBillDiscount(billDiscount);
	}
}
