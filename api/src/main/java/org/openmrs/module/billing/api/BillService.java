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

package org.openmrs.module.billing.api;

import java.util.List;

import org.openmrs.Patient;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.base.entity.IEntityDataService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.search.BillSearch;
import org.openmrs.module.billing.api.util.PrivilegeConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * Interface that represents classes which perform data operations for {@link Bill}s.
 */
@Transactional
public interface BillService extends OpenmrsService {

    /**
     * Gets a bill by id,
     *
     * @param id the bill id.
     * @return the bill with the specified id.
     */
    @Transactional(readOnly = true)
    @Authorized(PrivilegeConstants.VIEW_BILLS)
    Bill getBillById(Integer id);

    /**
     * Gets a bill by UUID,
     *
     * @param uuid The bill UUID.
     * @return The bill with the specified UUID.
     */
    @Transactional(readOnly = true)
    @Authorized(PrivilegeConstants.VIEW_BILLS)
    Bill getBillByUuid(String uuid);
	
	/**
	 * Gets the {@link Bill} with the specified receipt number
	 *
	 * @param receiptNumber The receipt number to search for.
	 * @return The {@link Bill} with the specified receipt number or {@code null}.
	 */
	@Transactional(readOnly = true)
    @Authorized(PrivilegeConstants.VIEW_BILLS)
	Bill getBillByReceiptNumber(String receiptNumber);
	
	/**
	 * Returns all {@link Bill}s for the specified patient with the specified paging.
	 *
	 * @param patient The {@link Patient}.
	 * @param paging The paging information.
	 * @return All of the bills for the specified patient.
	 */
    @Transactional(readOnly = true)
    @Authorized(PrivilegeConstants.VIEW_BILLS)
	List<Bill> getBillsByPatient(Patient patient, PagingInfo paging);
	
	/**
	 * Returns all {@link Bill}s for the specified patient with the specified paging.
	 *
	 * @param patientId The patient id.
	 * @param paging The paging information.
	 * @return All of the bills for the specified patient.
	 */
    @Transactional(readOnly = true)
    @Authorized(PrivilegeConstants.VIEW_BILLS)
	List<Bill> getBillsByPatientId(int patientId, PagingInfo paging);
	
	/**
	 * Gets all bills using the specified {@link BillSearch} settings.
	 *
	 * @param billSearch The bill search settings.
	 * @return The bills found or an empty list if no bills were found.
	 */
	@Transactional(readOnly = true)
    @Authorized(PrivilegeConstants.VIEW_BILLS)
	List<Bill> getBills(BillSearch billSearch);
	
	/**
	 * Gets all bills using the specified {@link BillSearch} settings.
	 *
	 * @param billSearch The bill search settings.
	 * @param pagingInfo The paging information.
	 * @return The bills found or an empty list if no bills were found.
	 */
	@Transactional(readOnly = true)
    @Authorized(PrivilegeConstants.VIEW_BILLS)
	List<Bill> getBills(BillSearch billSearch, PagingInfo pagingInfo);

    /**
     * Creates or updates a bill
     *
     * @param bill bill to be created or updated
     * @return the created or updated bill
     */
    @Authorized(PrivilegeConstants.MANAGE_BILLS)
    Bill saveBill(Bill bill);

    /**
     * Voids teh given bill, removing it from the system
     *
     * @param bill the bill to void
     * @param voidReason the reason for voiding the bill
     */
    @Authorized(PrivilegeConstants.DELETE_BILLS)
    void voidBill(Bill bill, String voidReason);

    /**
     * Unvoids a previously voided bill, making it accessible to the system again
     *
     * @param bill the bill to unvoid
     */
    @Authorized(PrivilegeConstants.DELETE_BILLS)
    void unvoidBill(Bill bill);

    /**
     * Purges a bill from the database (cannot be undone)
     *
     * @param bill the bill to purge
     */
    @Authorized(PrivilegeConstants.PURGE_BILLS)
    void purgeBill(Bill bill);
}
