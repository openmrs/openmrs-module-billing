/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.querystore;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.model.Timesheet;
import org.openmrs.module.querystore.bridge.AbstractIndexingAdvice;

public class TimesheetIndexingAdvice extends AbstractIndexingAdvice<Timesheet> {
	
	// ITimesheetService exposes the generic IEntityDataService surface: save, purge, voidEntity,
	// unvoidEntity. AOP only intercepts outgoing calls, so voidEntity's internal save() does NOT
	// fire the save trigger (self-call) — we must list voidEntity/unvoidEntity explicitly. The
	// service's domain method closeOpenTimesheets internally calls save on each open row, but
	// because the close goes through the proxy boundary back into save on the same proxy, the
	// per-row save fires the advice on its own; closeOpenTimesheets is therefore omitted.
	static final Set<String> TRIGGER_METHODS = new HashSet<>(Arrays.asList("save", "voidEntity", "unvoidEntity", "purge"));
	
	static final Set<String> PURGE_METHODS = Collections.singleton("purge");
	
	@Override
	protected Class<Timesheet> getSupportedType() {
		return Timesheet.class;
	}
	
	@Override
	protected TimesheetRecordSerializer serializer() {
		return Context.getRegisteredComponent("billing.querystore.serializer.timesheet", TimesheetRecordSerializer.class);
	}
	
	@Override
	protected Set<String> triggerMethods() {
		return TRIGGER_METHODS;
	}
	
	@Override
	protected Set<String> purgeMethods() {
		return PURGE_METHODS;
	}
}
