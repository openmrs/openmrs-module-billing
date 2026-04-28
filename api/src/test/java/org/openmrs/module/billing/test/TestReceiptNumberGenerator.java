/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.test;

import org.openmrs.module.billing.api.IReceiptNumberGenerator;
import org.openmrs.module.billing.api.model.Bill;

public class TestReceiptNumberGenerator implements IReceiptNumberGenerator {
	
	private int loadedCount = 0;
	
	private boolean loaded = false;
	
	@Override
	public String getName() {
		return "Test Receipt Number Generator";
	}
	
	@Override
	public String getDescription() {
		return "";
	}
	
	@Override
	public void load() {
		loadedCount++;
		loaded = true;
	}
	
	@Override
	public String generateNumber(Bill bill) {
		return null;
	}
	
	@Override
	public String getConfigurationPage() {
		return null;
	}
	
	@Override
	public boolean isLoaded() {
		return loaded;
	}
	
	public int getLoadedCount() {
		return loadedCount;
	}
}
