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

import org.openmrs.Role;

/**
 * An {@link org.openmrs.Role} which can be configured to be lazy-loaded from the database.
 */
public class LazyRole extends Role {
	
	private static final long serialVersionUID = 0L;
	
	public LazyRole() {
		super();
	}
	
	public LazyRole(Role role) {
		setName(role.getName());
		setChangedBy(role.getChangedBy());
		setChildRoles(role.getChildRoles());
		setCreator(role.getCreator());
		setDateChanged(role.getDateChanged());
		setDateCreated(role.getDateCreated());
		setDateRetired(role.getDateRetired());
		setDescription(role.getDescription());
		setInheritedRoles(role.getInheritedRoles());
		setPrivileges(role.getPrivileges());
		setRetired(role.getRetired());
		setRetiredBy(role.getRetiredBy());
		setRetireReason(role.getRetireReason());
		setRole(role.getRole());
		setUuid(role.getUuid());
	}
}
