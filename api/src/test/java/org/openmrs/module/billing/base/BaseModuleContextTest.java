/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.base;

import java.sql.Connection;
import java.sql.SQLException;

import org.openmrs.test.BaseModuleContextSensitiveTest;

/**
 * Base class for OpenHMIS tests
 */
public abstract class BaseModuleContextTest extends BaseModuleContextSensitiveTest {
	
	@Override
	public void executeDataSet(String datasetFilename) {
		Connection conn = super.getConnection();
		try {
			try {
				conn.prepareStatement("SET REFERENTIAL_INTEGRITY FALSE").execute();
			}
			catch (SQLException e) {
				throw new RuntimeException(e);
			}
			
			// If a versioned file exists and we need to be using a versioned file based on the OMRS version,
			// make sure we get the correct file name if it exists
			String datasetFilenameToUse = TestUtil.getVersionedFileIfExists(datasetFilename);
			
			super.executeDataSet(datasetFilenameToUse);
		}
		finally {
			try {
				conn.prepareStatement("SET REFERENTIAL_INTEGRITY TRUE").execute();
			}
			catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
