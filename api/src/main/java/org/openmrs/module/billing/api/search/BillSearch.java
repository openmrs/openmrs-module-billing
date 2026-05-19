/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.search;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.DiscountStatus;

/**
 * A search criteria holder for {@link org.openmrs.module.billing.api.model.Bill} queries. This
 * class holds search parameters that are used by the DAO layer to build queries. Uses Lombok's
 * builder pattern for fluent API.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillSearch {
	
	private String patientUuid;
	
	private String cashierUuid;
	
	private String cashPointUuid;
	
	private List<BillStatus> statuses;
	
	private List<DiscountStatus> discountStatuses;
	
	private String patientName;
	
	private Boolean includeVoided = false;
	
	private Boolean includeVoidedLineItems = false;
}
