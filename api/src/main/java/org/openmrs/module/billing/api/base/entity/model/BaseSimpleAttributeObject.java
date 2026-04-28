/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.base.entity.model;

// @formatter:off
/**
 * Base class for {@link org.openmrs.OpenmrsObject} simple attribute models.
 * @param <TAttributeType> The class of the attribute type.
 */
public abstract class BaseSimpleAttributeObject<
			TOwner extends ICustomizable<?>,
			TAttributeType extends ISimpleAttributeType>
		extends BaseAttributeObject<TOwner, TAttributeType>
		implements ISimpleAttribute<TOwner, TAttributeType> {
// @formatter:on
	private static final long serialVersionUID = 0L;
}
