/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.db;

import org.openmrs.module.billing.api.model.PaymentModeAttributeType;

import javax.annotation.Nonnull;
import java.util.List;

public interface PaymentModeAttributeTypeDAO {
	
	PaymentModeAttributeType getPaymentModeAttributeType(@Nonnull Integer id);
	
	PaymentModeAttributeType getPaymentModeAttributeTypeByUuid(@Nonnull String uuid);
	
	List<PaymentModeAttributeType> getAllPaymentModeAttributeTypes(boolean includeRetired);
	
	PaymentModeAttributeType savePaymentModeAttributeType(@Nonnull PaymentModeAttributeType attributeType);
	
	void purgePaymentModeAttributeType(@Nonnull PaymentModeAttributeType attributeType);
}
