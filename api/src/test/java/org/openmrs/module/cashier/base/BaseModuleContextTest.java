/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and
 * limitations under the License.
 *
 * Copyright (C) OpenHMIS.  All Rights Reserved.
 */
package org.openmrs.module.cashier.base;

import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.sql.Connection;
import java.sql.SQLException;

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
