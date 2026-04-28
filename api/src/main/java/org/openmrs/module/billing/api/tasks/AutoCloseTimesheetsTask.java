/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
