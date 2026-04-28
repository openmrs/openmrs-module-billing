/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.web.rest.search;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.openmrs.Provider;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.ITimesheetService;
import org.openmrs.module.billing.api.base.ProviderUtil;
import org.openmrs.module.billing.api.model.Timesheet;
import org.openmrs.module.billing.web.base.resource.AlreadyPagedWithLength;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.springframework.stereotype.Component;

/**
 * Search handler for {@link Timesheet}s.
 */
@Component
public class TimesheetSearchHandler implements SearchHandler {
	
	private final SearchConfig searchConfig = new SearchConfig("default", RestConstants.VERSION_2 + "/billing/timesheet",
	        Collections.singletonList("*"),
	        new SearchQuery.Builder("Find a timesheet by date").withRequiredParameters("date").build());
	
	@Override
	public PageableResult search(RequestContext context) {
		ITimesheetService service = Context.getService(ITimesheetService.class);
		Provider provider = ProviderUtil.getCurrentProvider();
		Date date;
		if (provider == null) {
			return null;
		}
		try {
			date = new SimpleDateFormat("MM/dd/yyyy").parse(context.getParameter("date"));
		}
		catch (ParseException e) {
			throw new APIException("Invalid date parameter: " + context.getParameter("date"));
		}
		List<Timesheet> timesheets = service.getTimesheetsByDate(provider, date);
		return new AlreadyPagedWithLength<>(context, timesheets, false, timesheets.size());
	}
	
	@Override
	public SearchConfig getSearchConfig() {
		return searchConfig;
	}
}
