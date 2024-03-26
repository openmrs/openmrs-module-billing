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
package org.openmrs.module.billing.web.base.controller;

import org.apache.commons.lang3.StringUtils;

/**
 * A view model used by role creation pages.
 */
public class RoleCreationViewModel {
	private String addToRole;
	private String removeFromRole;
	private String newRoleName;
	private String role;

	public String getAddToRole() {
		return addToRole;
	}

	public void setAddToRole(String addToRole) {
		this.addToRole = addToRole;
	}

	public String getRemoveFromRole() {
		return removeFromRole;
	}

	public void setRemoveFromRole(String removeFromRole) {
		this.removeFromRole = removeFromRole;
	}

	public String getNewRoleName() {
		return StringUtils.strip(newRoleName);
	}

	public void setNewRoleName(String newRoleName) {
		this.newRoleName = newRoleName;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}
