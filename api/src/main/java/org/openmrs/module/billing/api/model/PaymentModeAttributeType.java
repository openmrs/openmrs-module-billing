/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.model;

import org.openmrs.module.billing.api.base.entity.model.BaseInstanceAttributeType;

/**
 * Model class to describe an attribute of a payment mode. For example, a credit card mode of
 * payment may require a transaction number as an attribute.
 */
public class PaymentModeAttributeType extends BaseInstanceAttributeType<PaymentMode> {
	
	private static final long serialVersionUID = 0L;
}
