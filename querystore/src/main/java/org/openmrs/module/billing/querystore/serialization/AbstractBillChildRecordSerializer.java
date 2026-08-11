/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.querystore.serialization;

import java.time.LocalDate;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.querystore.model.QueryDocument;
import org.openmrs.module.querystore.serialization.AbstractRecordSerializer;
import org.openmrs.module.querystore.util.DateFormatUtil;

/**
 * Base for the billing record types that hang off a parent {@link Bill} (discounts, refunds). It
 * centralizes the "child-of-bill" cross-cutting fields those types share: patient scope via the
 * parent bill, the resource uuid, the record date, and the parent-bill reference metadata
 * ({@code bill_uuid} + {@code receipt_number}). Subclasses supply {@link #billOf} and their
 * type-specific {@link #populate} text/metadata.
 * <p>
 * <b>Flush-thread constraint.</b> These types live-sync from a Hibernate flush (via
 * {@code BillChildDbEventListener} consuming core's {@code SaveDbEvent}), so {@link #populate} may
 * run on the flush thread. It must therefore navigate only id-loadable to-one proxies (as
 * {@link #getPatientUuid} / {@link #receiptOf} do: bill → patient / receipt number) and must not
 * issue a query or force a lazy collection, which could trigger a flush-inside-flush.
 */
abstract class AbstractBillChildRecordSerializer<T extends BaseOpenmrsData> extends AbstractRecordSerializer<T> {
	
	/** The parent bill this entity hangs off (its patient is the document's scope). */
	protected abstract Bill billOf(T entity);
	
	@Override
	protected String getPatientUuid(T entity) {
		Bill bill = billOf(entity);
		return bill != null && bill.getPatient() != null ? bill.getPatient().getUuid() : null;
	}
	
	@Override
	protected String getResourceUuid(T entity) {
		return entity.getUuid();
	}
	
	@Override
	protected LocalDate getDate(T entity) {
		return DateFormatUtil.toLocalDate(entity.getDateCreated());
	}
	
	/** Trimmed receipt number of the parent bill (used in both text and metadata), or null. */
	protected final String receiptOf(Bill bill) {
		return bill != null ? trimToNull(bill.getReceiptNumber()) : null;
	}
	
	/**
	 * Writes the shared parent-bill reference metadata: {@code bill_uuid} and {@code receipt_number}.
	 */
	protected final void putBillReference(QueryDocument doc, Bill bill, String receipt) {
		if (bill != null && bill.getUuid() != null) {
			doc.putMetadata(BillingQueryFields.BILL_UUID, bill.getUuid());
		}
		if (receipt != null) {
			doc.putMetadata(BillingQueryFields.RECEIPT_NUMBER, receipt);
		}
	}
}
