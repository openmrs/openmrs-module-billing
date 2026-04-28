/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
@Resource(name = RestConstants.VERSION_1 + CashierResourceController.BILLING_NAMESPACE
        + "/paymentModeAttributeType", supportedClass = PaymentModeAttributeType.class, supportedOpenmrsVersions = {
                "2.0 - 2.*" })
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
