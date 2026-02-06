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
package org.openmrs.module.billing.api.base.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.base.exception.PrivilegeException;
import org.openmrs.module.billing.api.base.f.Func1;

/**
 * Helper class for working with {@link org.openmrs.Privilege}s.
 */
@Slf4j
public class PrivilegeUtil {
	
	private PrivilegeUtil() {
	}
	
	/**
	 * Checks if the specified user has all of the comma separated privileges.
	 *
	 * @param user The user to check
	 * @param privileges The privilege or comma separated list of privileges
	 * @return {@code true} if the user has all the privileges; otherwise, {@code false}.
	 */
	public static boolean hasPrivileges(User user, String privileges) {
		if (StringUtils.isEmpty(privileges)) {
			return true;
		}
		
		String[] privs = StringUtils.split(privileges, ',');
		
		return hasPrivileges(user, privs);
	}
	
	/**
	 * Checks if the specified user has all of the specified privileges.
	 *
	 * @param user The user to check
	 * @param privileges The privileges
	 * @return {@code true} if the user has all the privileges; otherwise, {@code false}.
	 */
	public static boolean hasPrivileges(final User user, String... privileges) {
		if (user == null) {
			throw new IllegalArgumentException("The user to check must be defined.");
		}
		
		if (privileges == null || privileges.length == 0) {
			return true;
		}
		
		Func1<String, Boolean> hasPrivFunc;
		User currentUser = Context.getAuthenticatedUser();
		if (user == currentUser) {
			hasPrivFunc = new Func1<String, Boolean>() {
				
				@Override
				public Boolean apply(String priv) {
					return Context.hasPrivilege(priv);
				}
			};
		} else {
			hasPrivFunc = new Func1<String, Boolean>() {
				
				@Override
				public Boolean apply(String priv) {
					return user.hasPrivilege(priv);
				}
			};
		}
		
		boolean result = true;
		for (String priv : privileges) {
			String trimmed = priv.trim();
			if (!hasPrivFunc.apply(trimmed)) {
				result = false;
				break;
			}
		}
		
		return result;
	}
	
	public static void requirePrivileges(User user, String privileges) {
		boolean hasPrivileges = hasPrivileges(user, privileges);
		if (!hasPrivileges) {
			log.error("Privileges are missing. The required privilege is <" + privileges + ">");
			throw new PrivilegeException();
		}
	}
	
}
