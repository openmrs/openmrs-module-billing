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
package org.openmrs.module.cashier.api.base.exception;

import org.openmrs.api.APIException;

/**
 * Represents an exception that occurs when a report file cannot be found.
 */
public class ReportNotFoundException extends APIException {
	public static final long serialVersionUID = 22323L;

	public ReportNotFoundException() {
		super();
	}

	public ReportNotFoundException(String message) {
		super(message);
	}

	public ReportNotFoundException(Throwable cause) {
		super(cause);
	}

	public ReportNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
