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
package org.openmrs.module.billing.web.rest.search;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.CashPointService;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.api.search.CashPointSearch;
import org.openmrs.module.billing.web.base.resource.AlreadyPagedWithLength;
import org.openmrs.module.billing.web.base.resource.PagingUtil;
import org.openmrs.module.billing.web.legacyweb.CashierRestConstants;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.springframework.stereotype.Component;

/**
 * Search handler for {@link CashPoint}s.
 */
@Component
public class CashPointSearchHandler implements SearchHandler {
    private final SearchConfig searchConfig = new SearchConfig("default", CashierRestConstants.CASH_POINT_RESOURCE,
            Collections.singletonList("*"), Collections.singletonList(new SearchQuery.Builder(
            "Find a cashpoint by its name, optionally filtering by location").withRequiredParameters("q")
            .withOptionalParameters("location_uuid").build()));

    @Override
    public PageableResult search(RequestContext context) {
        String query = context.getParameter("q");
        String locationUuid = context.getParameter("location_uuid");
        boolean includeRetired = BooleanUtils.toBoolean(context.getParameter("includeAll"));
        query = query.isEmpty() ? null : query;
        locationUuid = StringUtils.isEmpty(locationUuid) ? null : locationUuid;

        CashPointSearch cashPointSearch = CashPointSearch.builder()
                .locationUuid(locationUuid)
                .name(query)
                .includeRetired(includeRetired).build();

        PagingInfo pagingInfo = PagingUtil.getPagingInfoFromContext(context);

        List<CashPoint> cashPoints = Context.getService(CashPointService.class).getCashPoints(cashPointSearch, pagingInfo);
        return new AlreadyPagedWithLength<>(context, cashPoints, pagingInfo.hasMoreResults(), pagingInfo.getTotalRecordCount());
    }

    @Override
    public SearchConfig getSearchConfig() {
        return searchConfig;
    }
}
