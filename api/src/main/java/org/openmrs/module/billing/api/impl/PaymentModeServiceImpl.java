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
