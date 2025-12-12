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
package org.openmrs.module.billing.api.search;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openmrs.module.billing.api.model.BillStatus;

/**
 * A search criteria holder for {@link org.openmrs.module.billing.api.model.Bill} queries. This
 * class holds search parameters that are used by the DAO layer to build queries. Uses Lombok's
 * builder pattern for fluent API.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BillSearch {
	
	private String patientUuid;
	
	private String cashierUuid;
	
	private String cashPointUuid;
	
	private List<BillStatus> statuses;
	
	private String patientName;
	
	private Boolean includeVoided = false;
	
	private Boolean includeVoidedLineItems = false;
}
