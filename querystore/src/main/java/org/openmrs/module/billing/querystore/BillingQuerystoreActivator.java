/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.querystore;

import org.openmrs.module.BaseModuleActivator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lifecycle hooks for the billing &lt;-&gt; query store integration.
 * <p>
 * No wiring happens here: the {@code ResourceTypeProvider}, serializer, bootstrapper and
 * {@link BillChildDbEventListener} beans are all registered in {@code moduleApplicationContext.xml}
 * and discovered by querystore / Spring. Steady-state indexing therefore needs nothing started
 * here, and it flows through two paths: {@code Bill} rides core #6084's {@code *ServiceEvent}s (its
 * {@code BillService} is an {@code OpenmrsService}, which querystore's own consumer handles), while
 * {@code BillDiscount} / {@code BillRefund} are projected by {@link BillChildDbEventListener} from
 * core's Hibernate {@code SaveDbEvent}s (their services are not {@code OpenmrsService}s).
 * <p>
 * Initial backfill of pre-existing records is deliberately <em>not</em> auto-run from here (a full
 * scan should not block module startup). Trigger it once, admin-side, via querystore's reindex
 * endpoint ({@code POST /ws/rest/v1/querystore/reindex {"scope":"all"}}) or
 * {@code BootstrapService.bootstrap(...)}.
 */
public class BillingQuerystoreActivator extends BaseModuleActivator {
	
	private static final Logger log = LoggerFactory.getLogger(BillingQuerystoreActivator.class);
	
	@Override
	public void started() {
		log.info("Billing QueryStore integration started - billing_bill, billing_discount and "
		        + "billing_refund resource types registered with the query store");
	}
	
	@Override
	public void stopped() {
		log.info("Billing QueryStore integration stopped");
	}
}
