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
package org.openmrs.module.cashier.web.base.resource;

import org.openmrs.OpenmrsData;
import org.openmrs.module.cashier.api.base.entity.model.ISimpleAttribute;
import org.openmrs.module.cashier.api.base.entity.model.ISimpleCustomizable;

// @formatter:off
/**
 * REST resource for {@link org.openmrs.OpenmrsData}
 * {@link org.openmrs.module.openhmis.commons.api.entity.model.ISimpleCustomizable}s.
 * @param <E> The simple customizable model class
 * @param <TAttribute> The model attribute class
 */
public abstract class BaseRestSimpleCustomizableDataResource<
			E extends ISimpleCustomizable<TAttribute> & OpenmrsData,
			TAttribute extends ISimpleAttribute<E, ?>>
		extends BaseRestCustomizableDataResource<E, TAttribute> {
// @formatter:on
}
