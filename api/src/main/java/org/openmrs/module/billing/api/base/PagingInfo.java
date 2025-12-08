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
