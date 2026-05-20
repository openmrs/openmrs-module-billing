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

import org.openmrs.BaseOpenmrsData;
import org.openmrs.module.querystore.model.QueryDocument;

// OpenMRS BaseOpenmrsData audit columns shared by Bill / BillRefund / BillDiscount / Timesheet.
// Centralised because all four resource types want the same six audit fields (creator, changedBy,
// dateChanged, voidedBy, dateVoided, voidReason) plus the time-precise createdAt; emitting them
// inline would invite drift — a future refactor adds the field to one serializer and forgets the
// others, and audit queries silently miss the new resource type.
final class BillingAuditFields {
	
	private BillingAuditFields() {
	}
	
	static void populate(QueryDocument doc, BaseOpenmrsData entity) {
		if (entity.getDateCreated() != null) {
			doc.putMetadata(BillingQueryStoreConstants.FIELD_CREATED_AT, entity.getDateCreated());
		}
		if (entity.getDateChanged() != null) {
			doc.putMetadata(BillingQueryStoreConstants.FIELD_DATE_CHANGED, entity.getDateChanged());
		}
		if (entity.getDateVoided() != null) {
			doc.putMetadata(BillingQueryStoreConstants.FIELD_DATE_VOIDED, entity.getDateVoided());
		}
		if (entity.getCreator() != null) {
			doc.putMetadata(BillingQueryStoreConstants.FIELD_CREATOR_UUID, entity.getCreator().getUuid());
		}
		if (entity.getChangedBy() != null) {
			doc.putMetadata(BillingQueryStoreConstants.FIELD_CHANGED_BY_UUID, entity.getChangedBy().getUuid());
		}
		if (entity.getVoidedBy() != null) {
			doc.putMetadata(BillingQueryStoreConstants.FIELD_VOIDED_BY_UUID, entity.getVoidedBy().getUuid());
		}
		if (entity.getVoidReason() != null && !entity.getVoidReason().trim().isEmpty()) {
			doc.putMetadata(BillingQueryStoreConstants.FIELD_VOID_REASON, entity.getVoidReason());
		}
	}
}
