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

import org.openmrs.BaseOpenmrsData;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.event.DeleteDbEvent;
import org.openmrs.api.db.event.SaveDbEvent;
import org.openmrs.module.billing.api.model.BillDiscount;
import org.openmrs.module.billing.api.model.BillRefund;
import org.openmrs.module.querystore.events.SerializerRegistry;
import org.openmrs.module.querystore.serialization.ClinicalRecordSerializer;
import org.openmrs.module.querystore.sync.AfterCommitDispatcher;
import org.openmrs.module.querystore.sync.RecordIndexer;
import org.openmrs.module.querystore.sync.RecordProjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

/**
 * Live-sync for {@link BillDiscount} and {@link BillRefund}, which querystore's own
 * {@code CoreServiceEventListener} does not pick up.
 * <p>
 * querystore's steady-state consumer subscribes to core's #6084 {@code *ServiceEvent}s, which are
 * produced by an AOP advice scoped to {@code target(org.openmrs.api.OpenmrsService)}. {@code Bill}
 * live-syncs that way because {@code BillService extends OpenmrsService}. But
 * {@code BillDiscountService} / {@code BillRefundService} are plain interfaces, so their
 * {@code saveBillDiscount} / {@code saveBillRefund} calls raise no {@code SaveServiceEvent} and
 * those two types would only be indexed by the initial backfill.
 * <p>
 * They are, however, still persisted through Hibernate, so core's <em>non-AOP</em> path fires:
 * {@code EventInterceptor} publishes a {@link SaveDbEvent} / {@link DeleteDbEvent} for every entity
 * change, on the flush thread inside the originating transaction (session open) - the same
 * conditions serialization needs. This listener consumes those DB events, filters to the two
 * billing child types, and drives the exact same projection pipeline querystore's service consumer
 * uses ({@link RecordProjector} + the reachable {@code querystore.sync.*} beans, per the SPI).
 * {@code Bill} is deliberately ignored here - it already live-syncs via its
 * {@code SaveServiceEvent}, so handling it again would be redundant (idempotent, but wasteful).
 */
public class BillChildDbEventListener {
	
	private static final Logger log = LoggerFactory.getLogger(BillChildDbEventListener.class);
	
	@EventListener
	public void onSave(SaveDbEvent<?> event) {
		project(event.getEntity(), false);
	}
	
	@EventListener
	public void onDelete(DeleteDbEvent<?> event) {
		project(event.getEntity(), true);
	}
	
	private void project(Object entity, boolean purge) {
		// Only the billing child types whose services are not OpenmrsServices; Bill live-syncs via
		// its own SaveServiceEvent and must not be double-projected here.
		if (!(entity instanceof BillDiscount) && !(entity instanceof BillRefund)) {
			return;
		}
		BaseOpenmrsData data = (BaseOpenmrsData) entity;
		ClinicalRecordSerializer<BaseOpenmrsData> serializer = registry().resolve(data);
		if (serializer == null) {
			return;
		}
		try {
			// Mirrors CoreServiceEventListener: serialize now (session open) and defer the
			// embed + write to after-commit; the entity's own voided flag routes index-vs-delete.
			RecordProjector.project(serializer, data, purge, indexer(), dispatcher());
		}
		catch (RuntimeException e) {
			// Best-effort: a projection failure must not break the clinical transaction that saved
			// the discount/refund (same contract as querystore's own consumer).
			log.warn("Failed to project {} into the query store; swallowing", data.getClass().getSimpleName(), e);
		}
	}
	
	private SerializerRegistry registry() {
		return Context.getRegisteredComponent("querystore.serializerRegistry", SerializerRegistry.class);
	}
	
	private RecordIndexer indexer() {
		return Context.getRegisteredComponent("querystore.sync.indexer", RecordIndexer.class);
	}
	
	private AfterCommitDispatcher dispatcher() {
		return Context.getRegisteredComponent("querystore.sync.dispatcher", AfterCommitDispatcher.class);
	}
}
