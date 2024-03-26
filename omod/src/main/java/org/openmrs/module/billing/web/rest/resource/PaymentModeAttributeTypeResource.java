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
package org.openmrs.module.billing.web.rest.resource;

import org.openmrs.module.billing.web.base.resource.BaseRestAttributeTypeResource;
import org.openmrs.module.billing.web.rest.controller.base.CashierResourceController;
import org.openmrs.module.billing.api.IPaymentModeAttributeTypeService;
import org.openmrs.module.billing.api.base.entity.IMetadataDataService;
import org.openmrs.module.billing.api.model.PaymentModeAttributeType;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;

/**
 * REST resource representing a {@link PaymentModeAttributeType}.
 */
@Resource(name = RestConstants.VERSION_1 + CashierResourceController.KENYAEMR_CASHIER_NAMESPACE + "/paymentModeAttributeType",
        supportedClass = PaymentModeAttributeType.class,
        supportedOpenmrsVersions = { "2.0 - 2.*" })
public class PaymentModeAttributeTypeResource extends BaseRestAttributeTypeResource<PaymentModeAttributeType> {
	@Override
	public PaymentModeAttributeType newDelegate() {
		return new PaymentModeAttributeType();
	}

	@Override
	public Class<? extends IMetadataDataService<PaymentModeAttributeType>> getServiceClass() {
		return IPaymentModeAttributeTypeService.class;
	}
}
