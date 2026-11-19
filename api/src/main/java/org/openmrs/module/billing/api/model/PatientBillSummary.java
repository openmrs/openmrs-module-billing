/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.model;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openmrs.Patient;

/**
 * Summary of the non-voided bills of one patient. Each instance is one patient, not one bill. This
 * class is not persisted and is read-only.
 */
@Getter
@AllArgsConstructor
public class PatientBillSummary {
	
	private Patient patient;
	
	/** Number of non-voided bills of the patient, in any status. */
	private Long billCount;
	
	/**
	 * Sum over the patient's non-voided {@link BillStatus#PENDING} and {@link BillStatus#POSTED} bills
	 * of {@code max(amountAfterDiscount - totalPayments, 0)}.
	 */
	private BigDecimal totalUnpaid;
	
	/** The latest {@code dateCreated} among the patient's non-voided bills. */
	private Date latestBillDate;
}
