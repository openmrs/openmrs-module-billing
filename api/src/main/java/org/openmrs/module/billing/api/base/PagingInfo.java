/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class contains the paging information used by the entity services to paginate results. Both
 * page and pageSize are 1-based, defining either as 0 will cause paging to be ignored.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagingInfo {
	
	private int page;
	
	private int pageSize;
	
	private Long totalRecordCount;
	
	private boolean loadRecordCount;
	
	/**
	 * Creates a new {@link PagingInfo} instance.
	 *
	 * @param page The 1-based number of the page being requested.
	 * @param pageSize The number of records to include on each page.
	 */
	public PagingInfo(int page, int pageSize) {
		this.page = page;
		this.pageSize = pageSize;
		this.loadRecordCount = true;
	}
	
	public void setTotalRecordCount(Long totalRecordCount) {
		this.totalRecordCount = totalRecordCount;
		// If the total records is set to anything other than null, than don't reload the count
		this.loadRecordCount = totalRecordCount == null;
	}
	
	public Boolean hasMoreResults() {
		return ((long) page * pageSize) < totalRecordCount;
	}
}
