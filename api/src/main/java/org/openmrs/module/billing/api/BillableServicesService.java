package org.openmrs.module.billing.api;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.search.BillableServiceSearch;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service interface for managing billable services in the billing system.
 * <p>
 * Billable services represent items or services that can be added to bills, such as procedures,
 * consultations, medications, or other medical services that have associated prices.
 * </p>
 *
 * @see BillableService
 * @see BillableServiceSearch
 */
public interface BillableServicesService extends OpenmrsService {
	
	/**
	 * Retrieves a billable service by its database ID.
	 *
	 * @param id the database ID of the billable service
	 * @return the billable service with the specified ID, or null if not found
	 */
	@Transactional(readOnly = true)
	BillableService getBillableService(Integer id);
	
	/**
	 * Retrieves a billable service by its UUID.
	 *
	 * @param uuid the UUID of the billable service
	 * @return the billable service with the specified UUID, or null if not found
	 */
	@Transactional(readOnly = true)
	BillableService getBillableServiceByUuid(String uuid);
	
	/**
	 * Searches for billable services using the specified search criteria.
	 * <p>
	 * By default, retired billable services are excluded from search results unless explicitly included
	 * via {@link BillableServiceSearch#setIncludeRetired(Boolean)}. The search supports filtering by
	 * service status, category, type, concept, and name.
	 * </p>
	 *
	 * @param billableServiceSearch the search criteria
	 * @param pagingInfo optional paging information (can be null for no paging)
	 * @return a list of billable services matching the search criteria, or an empty list if none found
	 * @see BillableServiceSearch
	 */
	@Transactional(readOnly = true)
	List<BillableService> getBillableServices(BillableServiceSearch billableServiceSearch, PagingInfo pagingInfo);
	
	/**
	 * Saves a billable service to the database.
	 * <p>
	 * If the billable service is new (no ID), it will be created. If it already exists, it will be
	 * updated.
	 * </p>
	 *
	 * @param billableService the billable service to save
	 * @return the saved billable service with updated metadata
	 * @throws IllegalArgumentException if the billable service is null or invalid
	 */
	@Transactional
	BillableService saveBillableService(BillableService billableService);
	
	/**
	 * Permanently deletes a billable service from the database.
	 * <p>
	 * <strong>Warning:</strong> This operation cannot be undone. All associated data (service prices,
	 * attributes, etc.) will also be removed due to cascade delete rules. Consider retiring the service
	 * instead for soft deletion.
	 * </p>
	 *
	 * @param billableService the billable service to permanently delete
	 */
	@Transactional
	void purgeBillableService(BillableService billableService);
}
