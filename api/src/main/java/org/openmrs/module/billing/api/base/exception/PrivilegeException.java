/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.base.exception;

import org.openmrs.api.APIException;

/**
 * Exception thrown when there are problems with privileges
 */
public class PrivilegeException extends APIException {
	
	private static final long serialVersionUID = 22323L;
	
	public PrivilegeException() {
		super();
	}
	
	public PrivilegeException(String message) {
		super(message);
	}
	
	public PrivilegeException(Throwable cause) {
		super(cause);
	}
	
	public PrivilegeException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
