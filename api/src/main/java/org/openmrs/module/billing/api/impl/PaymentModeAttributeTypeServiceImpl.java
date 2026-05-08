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

import java.util.List;

import lombok.Setter;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.billing.api.PaymentModeAttributeTypeService;
import org.openmrs.module.billing.api.db.PaymentModeAttributeTypeDAO;
import org.openmrs.module.billing.api.model.PaymentModeAttributeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data service implementation class for {@link PaymentModeAttributeType}s.
 */
@Transactional
public class PaymentModeAttributeTypeServiceImpl extends BaseOpenmrsService
        implements PaymentModeAttributeTypeService {

    @Setter(onMethod_ = { @Autowired })
    private PaymentModeAttributeTypeDAO paymentModeAttributeTypeDAO;

    @Override
    @Transactional(readOnly = true)
    public PaymentModeAttributeType getPaymentModeAttributeType(Integer id) {
        if (id == null) {
            return null;
        }
        return paymentModeAttributeTypeDAO.getPaymentModeAttributeType(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentModeAttributeType getPaymentModeAttributeTypeByUuid(String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            return null;
        }
        return paymentModeAttributeTypeDAO.getPaymentModeAttributeTypeByUuid(uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentModeAttributeType> getAllPaymentModeAttributeTypes(boolean includeRetired) {
        return paymentModeAttributeTypeDAO.getAllPaymentModeAttributeTypes(includeRetired);
    }

    @Override
    public PaymentModeAttributeType savePaymentModeAttributeType(PaymentModeAttributeType attributeType) {
        if (attributeType == null) {
            throw new IllegalArgumentException("PaymentModeAttributeType cannot be null");
        }
        return paymentModeAttributeTypeDAO.savePaymentModeAttributeType(attributeType);
    }

    @Override
    public void purgePaymentModeAttributeType(PaymentModeAttributeType attributeType) {
        if (attributeType == null) {
            throw new IllegalArgumentException("PaymentModeAttributeType cannot be null");
        }
        paymentModeAttributeTypeDAO.purgePaymentModeAttributeType(attributeType);
    }
}
