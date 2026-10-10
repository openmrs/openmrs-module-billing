/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.ITimesheetService;
import org.openmrs.module.billing.api.base.entity.db.hibernate.EntityCriteria;
import org.openmrs.module.billing.api.base.entity.impl.BaseEntityDataServiceImpl;
import org.openmrs.module.billing.api.base.entity.security.IEntityAuthorizationPrivileges;
import org.openmrs.module.billing.api.model.Timesheet;
import org.openmrs.module.billing.api.util.PrivilegeConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data service implementation class for {@link Timesheet}s.
 */
@Transactional
public class TimesheetServiceImpl extends BaseEntityDataServiceImpl<Timesheet> implements ITimesheetService, IEntityAuthorizationPrivileges {
	
	private static final String CLOCK_IN = "clockIn";
	
	private static final String CLOCK_OUT = "clockOut";
	
	private static final Integer BATCH_SIZE = 50;
	
	private static final Integer END_DATE_HOUR_OF_DAY = 23;
	
	private static final Integer END_DATE_MINUTE = 59;
	
	private static final Integer END_DATE_SECOND = 59;
	
	@Override
	protected IEntityAuthorizationPrivileges getPrivileges() {
		return this;
	}
	
	@Override
	protected void validate(Timesheet entity) {
	}
	
	@Override
	public String getVoidPrivilege() {
		return PrivilegeConstants.MANAGE_TIMESHEETS;
	}
	
	@Override
	public String getSavePrivilege() {
		return PrivilegeConstants.MANAGE_TIMESHEETS;
	}
	
	@Override
	public String getPurgePrivilege() {
		return PrivilegeConstants.PURGE_TIMESHEETS;
	}
	
	@Override
	public String getGetPrivilege() {
		return PrivilegeConstants.VIEW_TIMESHEETS;
	}
	
	@Override
	public Timesheet getCurrentTimesheet(Provider cashier) {
		EntityCriteria<Timesheet> criteria = getRepository().createCriteria(Timesheet.class);
		criteria.add(EntityCriteria.and(EntityCriteria.eq("cashier", cashier), EntityCriteria.isNull(CLOCK_OUT)));
		criteria.addOrder(EntityCriteria.desc(CLOCK_IN));
		
		return getRepository().selectSingle(Timesheet.class, criteria);
	}
	
	@Override
	public void closeOpenTimesheets() {
		EntityCriteria<Timesheet> criteria = getRepository().createCriteria(Timesheet.class);
		criteria.add(EntityCriteria.isNull("clockOut"));
		criteria.addOrder(EntityCriteria.desc("clockIn"));
		
		List<Timesheet> timesheets = getRepository().select(Timesheet.class, criteria);
		
		Date clockOutDate = new Date();
		int counter = 0;
		for (Timesheet timesheet : timesheets) {
			timesheet.setClockOut(clockOutDate);
			
			if (counter++ > BATCH_SIZE) {
				//ensure changes are persisted to DB before reclaiming memory
				Context.flushSession();
				Context.clearSession();
				counter = 0;
			}
		}
	}
	
	@Override
	public List<Timesheet> getTimesheetsByDate(Provider cashier, Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		Date startDate = calendar.getTime();
		
		calendar.set(Calendar.HOUR_OF_DAY, END_DATE_HOUR_OF_DAY);
		calendar.set(Calendar.MINUTE, END_DATE_MINUTE);
		calendar.set(Calendar.SECOND, END_DATE_SECOND);
		Date endDate = calendar.getTime();
		
		EntityCriteria<Timesheet> criteria = getRepository().createCriteria(Timesheet.class);
		criteria.add(EntityCriteria.and(EntityCriteria.eq("cashier", cashier), EntityCriteria.or(
		    // Start or end on date
		    EntityCriteria.or(EntityCriteria.between(CLOCK_IN, startDate, endDate),
		        EntityCriteria.between(CLOCK_OUT, startDate, endDate)),
		    EntityCriteria.or(
		        // Start on or before date and have not ended
		        EntityCriteria.and(EntityCriteria.le(CLOCK_IN, endDate), EntityCriteria.isNull(CLOCK_OUT)),
		        // Start before and end after date
		        EntityCriteria.and(EntityCriteria.le(CLOCK_IN, startDate), EntityCriteria.ge(CLOCK_OUT, endDate))))));
		criteria.addOrder(EntityCriteria.desc(CLOCK_IN));
		
		return getRepository().select(Timesheet.class, criteria);
	}
}
