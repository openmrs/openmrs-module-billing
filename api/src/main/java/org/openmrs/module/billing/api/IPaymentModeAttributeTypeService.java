/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api;

import org.openmrs.module.billing.api.base.entity.IMetadataDataService;
import org.openmrs.module.billing.api.model.PaymentModeAttributeType;
import org.springframework.transaction.annotation.Transactional;

/**
 * Interface that represents classes which perform data operations for
 * {@link PaymentModeAttributeType}s.
 */
@Transactional
public interface IPaymentModeAttributeTypeService extends IMetadataDataService<PaymentModeAttributeType> {}
