/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
