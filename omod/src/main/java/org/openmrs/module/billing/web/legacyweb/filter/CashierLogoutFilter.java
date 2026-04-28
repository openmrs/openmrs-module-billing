/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.web.legacyweb.filter;

import java.io.IOException;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.ITimesheetService;
import org.openmrs.module.billing.api.base.ProviderUtil;
import org.openmrs.module.billing.api.model.Timesheet;
import org.openmrs.module.billing.api.util.PrivilegeConstants;
import org.springframework.stereotype.Component;

/**
 * Performs the neccessary filters when a cashier logs out. Implements {@link Filter}
 */
@Component
@Slf4j
public class CashierLogoutFilter implements Filter {
	
	private static final String PROVIDER_ERROR_LOG_MESSAGE = "Could not locate the Provider";
	
	private static final String TIMESHEET_ERROR_LOG_MESSAGE = "Could not locate Timesheet";
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	        throws IOException, ServletException {
		log.debug("doCashierLogoutFilter");
		clockOutCashier();
		chain.doFilter(request, response);
	}
	
	@Override
	public void init(FilterConfig filterConfig) {
		
	}
	
	@Override
	public void destroy() {
		
	}
	
	private void clockOutCashier() {
		if (!userIsCashier()) {
			return;
		}
		
		Provider provider = ProviderUtil.getCurrentProvider(Context.getProviderService());
		if (provider == null) {
			log.error(PROVIDER_ERROR_LOG_MESSAGE);
			return;
		}
		
		ITimesheetService timesheetService = Context.getService(ITimesheetService.class);
		Timesheet timesheet = timesheetService.getCurrentTimesheet(provider);
		if (timesheet == null) {
			log.error(TIMESHEET_ERROR_LOG_MESSAGE);
			return;
		}
		
		if (cashierIsClockedIn(timesheet)) {
			timesheet.setClockOut(new Date());
			timesheetService.save(timesheet);
		}
	}
	
	private boolean userIsCashier() {
		boolean result = false;
		User authenticatedUser = Context.getAuthenticatedUser();
		if (authenticatedUser != null) {
			result = authenticatedUser.hasPrivilege(PrivilegeConstants.MANAGE_TIMESHEETS);
		}
		
		return result;
	}
	
	private boolean cashierIsClockedIn(Timesheet timesheet) {
		return timesheet != null && timesheet.getClockIn() != null;
	}
	
}
