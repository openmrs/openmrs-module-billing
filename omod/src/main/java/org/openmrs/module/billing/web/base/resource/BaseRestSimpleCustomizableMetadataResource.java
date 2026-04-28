/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.web.base.resource;

import org.openmrs.OpenmrsMetadata;
import org.openmrs.module.billing.api.base.entity.model.ISimpleAttribute;
import org.openmrs.module.billing.api.base.entity.model.ISimpleCustomizable;

// @formatter:off
/**
 * REST resource for {@link org.openmrs.OpenmrsMetadata}
 * {@link org.openmrs.module.openhmis.commons.api.entity.model.ISimpleCustomizable}s.
 * @param <E> The simple customizable model class
 * @param <TAttribute> The model attribute class
 */
public abstract class BaseRestSimpleCustomizableMetadataResource<
		E extends ISimpleCustomizable<TAttribute> & OpenmrsMetadata,
		TAttribute extends ISimpleAttribute<E, ?>>
		extends BaseRestCustomizableMetadataResource<E, TAttribute> {
// @formatter:on
}
