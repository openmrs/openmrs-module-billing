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
import org.openmrs.module.billing.api.PaymentModeService;
import org.openmrs.module.billing.api.db.PaymentModeDAO;
import org.openmrs.module.billing.api.model.PaymentMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default implementation of {@link PaymentModeService}.
 */
@Transactional
public class PaymentModeServiceImpl extends BaseOpenmrsService implements PaymentModeService {
	
	@Setter(onMethod_ = { @Autowired })
	private PaymentModeDAO paymentModeDAO;
	
	/**
	 * @inheritDoc
	 */
	@Override
	@Transactional(readOnly = true)
	public PaymentMode getPaymentMode(Integer id) {
		if (id == null) {
			return null;
		}
		return paymentModeDAO.getPaymentMode(id);
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	@Transactional(readOnly = true)
	public PaymentMode getPaymentModeByUuid(String uuid) {
		if (StringUtils.isEmpty(uuid)) {
			return null;
		}
		return paymentModeDAO.getPaymentModeByUuid(uuid);
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	@Transactional(readOnly = true)
	public List<PaymentMode> getPaymentModes(boolean includeRetired) {
		return paymentModeDAO.getPaymentModes(includeRetired);
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	@Transactional
	public PaymentMode savePaymentMode(PaymentMode paymentMode) {
		if (paymentMode == null) {
			throw new NullPointerException("Payment mode cannot be null");
		}
		return paymentModeDAO.savePaymentMode(paymentMode);
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	@Transactional
	public PaymentMode retirePaymentMode(PaymentMode paymentMode, String reason) {
		if (StringUtils.isEmpty(reason)) {
			throw new IllegalArgumentException("Retire reason cannot be null");
		}
		return paymentModeDAO.savePaymentMode(paymentMode);
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	@Transactional
	public PaymentMode unretirePaymentMode(PaymentMode paymentMode) {
		return paymentModeDAO.savePaymentMode(paymentMode);
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	@Transactional
	public void purgePaymentMode(PaymentMode paymentMode) {
		if (paymentMode == null) {
			throw new NullPointerException("Payment mode cannot be null");
		}
		paymentModeDAO.purgePaymentMode(paymentMode);
	}
}
