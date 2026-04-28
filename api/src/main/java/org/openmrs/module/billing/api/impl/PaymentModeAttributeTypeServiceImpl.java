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

import org.openmrs.module.billing.api.IPaymentModeAttributeTypeService;
import org.openmrs.module.billing.api.base.entity.impl.BaseMetadataDataServiceImpl;
import org.openmrs.module.billing.api.base.entity.security.IMetadataAuthorizationPrivileges;
import org.openmrs.module.billing.api.model.PaymentModeAttributeType;
import org.openmrs.module.billing.api.security.BasicMetadataAuthorizationPrivileges;

/**
 * Data service implementation class for {@link PaymentModeAttributeType}s.
 */
public class PaymentModeAttributeTypeServiceImpl extends BaseMetadataDataServiceImpl<PaymentModeAttributeType> implements IPaymentModeAttributeTypeService {
	
	@Override
	protected IMetadataAuthorizationPrivileges getPrivileges() {
		return new BasicMetadataAuthorizationPrivileges();
	}
	
	@Override
	protected void validate(PaymentModeAttributeType entity) {
	}
}
