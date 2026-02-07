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
package org.openmrs.module.billing.api.tasks;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.ITimesheetService;
import org.openmrs.scheduler.tasks.AbstractTask;

/**
 * A scheduled task that automatically closes all open timesheets
 */
@Slf4j
public class AutoCloseTimesheetsTask extends AbstractTask {
	
	@Override
	public void execute() {
		if (!isExecuting) {
			if (log.isDebugEnabled()) {
				log.debug("Starting Auto Close Timesheets Task...");
			}
			
			startExecuting();
			
			try {
				ITimesheetService timesheetService = Context.getService(ITimesheetService.class);
				
				timesheetService.closeOpenTimesheets();
			}
			catch (Exception e) {
				log.error("Error while auto closing open timesheets:", e);
			}
			finally {
				stopExecuting();
			}
		}
	}
}
