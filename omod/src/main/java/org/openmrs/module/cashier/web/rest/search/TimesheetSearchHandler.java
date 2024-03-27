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
package org.openmrs.module.cashier.web.rest.search;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.openmrs.Provider;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.cashier.api.ITimesheetService;
import org.openmrs.module.cashier.api.base.ProviderUtil;
import org.openmrs.module.cashier.api.model.Timesheet;
import org.openmrs.module.cashier.web.base.resource.AlreadyPagedWithLength;
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
            Collections.singletonList("*"), new SearchQuery.Builder("Find a timesheet by date").withRequiredParameters("date")
            .build());

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
        } catch (ParseException e) {
            throw new APIException("Invalid date parameter: " + context.getParameter("date"));
        }
        List<Timesheet> timesheets = service.getTimesheetsByDate(provider, date);
        PageableResult results = new AlreadyPagedWithLength<>(context, timesheets, false, timesheets.size());
        return results;
    }

    @Override
    public SearchConfig getSearchConfig() {
        return searchConfig;
    }
}
