package org.openmrs.module.billing.api.db;

import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.search.BillableServiceSearch;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Data Access Object (DAO) interface for {@link BillableService} persistence operations.
 *
 * @see BillableService
 * @see BillableServiceSearch
 */
public interface BillableServiceDAO {
	
	/**
	 * Retrieves a billable service by its database ID.
	 * <p>
	 * Note: This method may return retired billable services. Consider filtering retired records at the
	 * service layer if needed.
	 * </p>
	 *
	 * @param id the database ID of the billable service (must not be null)
	 * @return the billable service with the specified ID, or null if not found
	 */
	BillableService getBillableService(@Nonnull Integer id);
	
	/**
	 * Retrieves a billable service by its UUID.
	 * <p>
	 * Note: This method may return retired billable services. Consider filtering retired records at the
	 * service layer if needed.
	 * </p>
	 *
	 * @param uuid the UUID of the billable service (must not be null)
	 * @return the billable service with the specified UUID, or null if not found
	 */
	BillableService getBillableServiceByUuid(@Nonnull String uuid);
	
	/**
	 * Searches for billable services using the specified search criteria.
	 * <p>
	 * By default, retired billable services are excluded from results unless
	 * {@link BillableServiceSearch#setIncludeRetired(Boolean)} is set to true. The search criteria
	 * support filtering by service status, category, type, concept, and name (partial match).
	 * </p>
	 *
	 * @param billableServiceSearch the search criteria (must not be null)
	 * @param pagingInfo optional paging information (can be null for no paging). When provided with
	 *            {@code loadRecordCount=true}, the total count will be populated in the pagingInfo
	 * @return a list of billable services matching the search criteria, or an empty list if none found
	 * @see BillableServiceSearch
	 */
	List<BillableService> getBillableServices(@Nonnull BillableServiceSearch billableServiceSearch, PagingInfo pagingInfo);
	
	/**
	 * Persists a billable service to the database.
	 * <p>
	 * If the billable service has no ID, it will be created as a new record. If it has an ID, the
	 * existing record will be updated.
	 * </p>
	 *
	 * @param billableService the billable service to save (must not be null)
	 * @return the saved billable service with updated metadata (timestamps, IDs, etc.)
	 */
	BillableService saveBillableService(@Nonnull BillableService billableService);
	
	/**
	 * Permanently deletes a billable service from the database.
	 * <p>
	 * <strong>Warning:</strong> This operation cannot be undone. All associated data (service prices,
	 * attributes, etc.) will also be removed due to cascade delete rules.
	 * </p>
	 *
	 * @param billableService the billable service to permanently delete
	 */
	void purgeBillableService(BillableService billableService);
	
}
