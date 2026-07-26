/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api;

import java.util.List;

import org.openmrs.module.billing.api.base.entity.IObjectDataService;
import org.openmrs.module.billing.api.model.GroupSequence;
import org.openmrs.module.billing.api.model.SequentialReceiptNumberGeneratorModel;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Represents classes that provide data access services to model types that implement
 * {@link SequentialReceiptNumberGeneratorModel}. The {@link SequentialReceiptNumberGeneratorModel}
 * model class.
 */
public interface ISequentialReceiptNumberGeneratorService extends IObjectDataService<SequentialReceiptNumberGeneratorModel> {
	
	/**
	 * Gets the first {@link SequentialReceiptNumberGeneratorModel} or creates a new model if none have
	 * been defined.
	 *
	 * @return The first or new model.
	 * @should return the first model.
	 * @should return a new model if none has been defined.
	 */
	SequentialReceiptNumberGeneratorModel getOnly();
	
	/**
	 * Reserves the next sequence value for the specified group. Values are handed out from an in-memory
	 * pool backed by blocks reserved via {@link #reserveSequenceBlock(String, int)}, so the persisted
	 * sequence value advances a block at a time. The block size is configured by the
	 * {@code billing.sequenceBlockSize} global property (default 100).
	 * <p>
	 * Values may be skipped: a consumer whose transaction rolls back burns its value, and a restart or
	 * module reload discards the unused remainder of the current block. Values are unique but not
	 * gapless, and interleave (without colliding) across application servers.
	 * </p>
	 *
	 * @param group The grouping value.
	 * @return The next sequence value.
	 * @should Return one and persist a full block for a new group
	 * @should Hand out consecutive values from the pool
	 * @should Continue from the persisted value for an existing group
	 * @should Reserve a new block when the pool is drained
	 * @should Use the block size from the global property
	 * @should Throw IllegalArgumentException if the group is null
	 */
	@Transactional
	int reserveNextSequence(String group);
	
	/**
	 * Atomically reserves a contiguous block of sequence values for the specified group in its own
	 * transaction, committed immediately and independently of any enclosing transaction. The reserved
	 * block is {@code [first, first + blockSize - 1]} where {@code first} is the returned value.
	 *
	 * @param group The grouping value.
	 * @param blockSize The number of values to reserve.
	 * @return The first value of the reserved block.
	 * @should Reserve non overlapping blocks
	 * @should Commit independently of an enclosing transaction
	 * @should Throw IllegalArgumentException if the group is null
	 * @should Throw IllegalArgumentException if blockSize is less than one
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	int reserveSequenceBlock(String group, int blockSize);
	
	/**
	 * Returns all sequences.
	 *
	 * @return All sequences in the database.
	 * @should return all sequences
	 * @should return an empty list if no sequences have been defined
	 */
	@Transactional(readOnly = true)
	List<GroupSequence> getSequences();
	
	/**
	 * Returns the sequence for the specified group.
	 *
	 * @param group The group sequence to find.
	 * @return The group sequence
	 * @should Throw an IllegalArgumentException if group is null
	 * @should return the specified sequence
	 * @should return null if the sequence cannot be found
	 * @should return the sequence if group is empty
	 */
	@Transactional(readOnly = true)
	GroupSequence getSequence(String group);
	
	/**
	 * Saves the sequence, creating a new sequences or updating an existing one. Once the transaction
	 * commits, the in-memory reservation pool for the sequence's group is invalidated so future
	 * reservations continue from the saved value. The invalidation is JVM-local: other nodes in a
	 * cluster keep serving their already-reserved blocks, so manual sequence edits are not
	 * cluster-safe.
	 *
	 * @param sequence The sequence to save.
	 * @return The saved sequence.
	 * @should Throw a NullPointerException if sequence is null
	 * @should return the saved sequence
	 * @should update the sequence successfully
	 * @should create the sequence successfully
	 * @should Invalidate the pool for the group
	 * @should Not invalidate the pool when the transaction rolls back
	 */
	@Transactional
	GroupSequence saveSequence(GroupSequence sequence);
	
	/**
	 * Complete removes the specified sequence from the database.
	 *
	 * @param sequence The sequence to remove.
	 * @should Throw a NullPointerException if the sequence is null
	 * @should delete the sequence from the database
	 * @should not throw an exception if the sequence is not in the database
	 * @should Invalidate the pool for the group
	 */
	@Transactional
	void purgeSequence(GroupSequence sequence);
}
