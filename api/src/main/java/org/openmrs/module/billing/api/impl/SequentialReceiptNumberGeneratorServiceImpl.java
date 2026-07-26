/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.impl;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.ModuleSettings;
import org.openmrs.module.billing.api.ISequentialReceiptNumberGeneratorService;
import org.openmrs.module.billing.api.base.entity.impl.BaseObjectDataServiceImpl;
import org.openmrs.module.billing.api.model.GroupSequence;
import org.openmrs.module.billing.api.model.SequentialReceiptNumberGeneratorModel;
import org.openmrs.module.billing.api.security.BasicEntityAuthorizationPrivileges;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Data service implementation class for {@link SequentialReceiptNumberGeneratorModel}s.
 * <p>
 * Sequence values are handed out from an in-memory per-group pool that is refilled by reserving
 * blocks of values (size configured by the {@code billing.sequenceBlockSize} global property,
 * default {@link #DEFAULT_SEQUENCE_BLOCK_SIZE}) in a separate, immediately-committed transaction
 * that pessimistically locks the sequence row. This keeps concurrent reservations unique across
 * threads and JVMs, at the cost of gaps in the sequence: a rolled-back consumer burns its value,
 * and a restart discards the unused remainder of the current block.
 * </p>
 * <p>
 * Pool invalidation on {@link #saveSequence} / {@link #purgeSequence} is JVM-local: other nodes in
 * a cluster keep serving their already-reserved blocks. Manually editing a sequence value is
 * therefore only safe when done on all nodes or while the other nodes are stopped.
 * </p>
 */
@Slf4j
@Transactional
public class SequentialReceiptNumberGeneratorServiceImpl extends BaseObjectDataServiceImpl<SequentialReceiptNumberGeneratorModel, BasicEntityAuthorizationPrivileges> implements ISequentialReceiptNumberGeneratorService {
	
	public static final int DEFAULT_SEQUENCE_BLOCK_SIZE = 100;
	
	private static final int MAX_CAUSE_CHAIN_DEPTH = 10;
	
	private final ConcurrentHashMap<String, SequencePool> pools = new ConcurrentHashMap<>();
	
	@Override
	protected BasicEntityAuthorizationPrivileges getPrivileges() {
		// No authorization required
		return null;
	}
	
	@Override
	protected void validate(SequentialReceiptNumberGeneratorModel entity) {
	}
	
	@Override
	@Transactional(readOnly = true)
	public SequentialReceiptNumberGeneratorModel getOnly() {
		List<SequentialReceiptNumberGeneratorModel> records = getAll();
		
		if (!records.isEmpty()) {
			return records.get(0);
		} else {
			return new SequentialReceiptNumberGeneratorModel();
		}
	}
	
	@Override
	@Transactional
	public int reserveNextSequence(String group) {
		if (group == null) {
			throw new IllegalArgumentException("The group must be defined.");
		}
		
		while (true) {
			SequencePool pool = pools.computeIfAbsent(group, g -> new SequencePool());
			if (pool.isInvalidated()) {
				pools.remove(group, pool);
				continue;
			}
			
			Integer value = pool.tryTake();
			if (value != null) {
				return value;
			}
			
			synchronized (pool.refillLock) {
				value = pool.tryTake();
				if (value != null) {
					return value;
				}
				if (pool.isInvalidated()) {
					continue;
				}
				
				int blockSize = getBlockSize();
				value = pool.refillAndTake(reserveBlockWithRetry(group, blockSize), blockSize);
				if (value != null) {
					return value;
				}
			}
		}
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int reserveSequenceBlock(String group, int blockSize) {
		if (group == null) {
			throw new IllegalArgumentException("The group must be defined.");
		}
		if (blockSize < 1) {
			throw new IllegalArgumentException("The block size must be at least one.");
		}
		
		Criteria criteria = getRepository().createCriteria(GroupSequence.class);
		criteria.add(Restrictions.eq("group", group));
		criteria.setLockMode(LockMode.PESSIMISTIC_WRITE);
		GroupSequence sequence = getRepository().selectSingle(GroupSequence.class, criteria);
		
		int first;
		if (sequence == null) {
			sequence = new GroupSequence();
			sequence.setGroup(group);
			sequence.setValue(blockSize);
			first = 1;
		} else {
			first = sequence.getValue() + 1;
			sequence.setValue(sequence.getValue() + blockSize);
		}
		
		getRepository().save(sequence);
		
		return first;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<GroupSequence> getSequences() {
		return getRepository().select(GroupSequence.class);
	}
	
	@Override
	@Transactional(readOnly = true)
	public GroupSequence getSequence(String group) {
		if (group == null) {
			throw new IllegalArgumentException("The group must be defined.");
		}
		
		Criteria criteria = getRepository().createCriteria(GroupSequence.class);
		criteria.add(Restrictions.eq("group", group));
		
		return getRepository().selectSingle(GroupSequence.class, criteria);
	}
	
	@Override
	@Transactional
	public GroupSequence saveSequence(GroupSequence sequence) {
		if (sequence == null) {
			throw new NullPointerException("The sequence to save must be defined.");
		}
		
		GroupSequence result = getRepository().save(sequence);
		invalidatePoolAfterCommit(sequence.getGroup());
		
		return result;
	}
	
	@Override
	@Transactional
	public void purgeSequence(GroupSequence sequence) {
		if (sequence == null) {
			throw new NullPointerException("The sequence to purge must be defined.");
		}
		
		getRepository().delete(sequence);
		invalidatePoolAfterCommit(sequence.getGroup());
	}
	
	private int reserveBlockWithRetry(String group, int blockSize) {
		try {
			return getProxy().reserveSequenceBlock(group, blockSize);
		}
		catch (RuntimeException ex) {
			if (!isConstraintViolation(ex)) {
				throw ex;
			}
			
			return getProxy().reserveSequenceBlock(group, blockSize);
		}
	}
	
	protected ISequentialReceiptNumberGeneratorService getProxy() {
		return Context.getService(ISequentialReceiptNumberGeneratorService.class);
	}
	
	protected int getBlockSize() {
		String property = Context.getAdministrationService().getGlobalProperty(ModuleSettings.SEQUENCE_BLOCK_SIZE_PROPERTY);
		if (StringUtils.isNotBlank(property)) {
			try {
				int blockSize = Integer.parseInt(property.trim());
				if (blockSize >= 1) {
					return blockSize;
				}
				log.warn("Ignoring global property {}={}; the block size must be at least one.",
				    ModuleSettings.SEQUENCE_BLOCK_SIZE_PROPERTY, property);
			}
			catch (NumberFormatException ex) {
				log.warn("Ignoring non-numeric global property {}={}.", ModuleSettings.SEQUENCE_BLOCK_SIZE_PROPERTY,
				    property);
			}
		}
		
		return DEFAULT_SEQUENCE_BLOCK_SIZE;
	}
	
	// Invalidating before commit blocks concurrent refills on this transaction's row lock,
	// and a rollback would discard a still-valid pool
	private void invalidatePoolAfterCommit(String group) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				
				@Override
				public void afterCommit() {
					invalidatePool(group);
				}
			});
		} else {
			invalidatePool(group);
		}
	}
	
	void invalidatePool(String group) {
		if (group == null) {
			return;
		}
		
		SequencePool pool = pools.get(group);
		if (pool != null) {
			pool.invalidate();
			pools.remove(group, pool);
		}
	}
	
	private static boolean isConstraintViolation(Throwable ex) {
		Throwable t = ex;
		for (int depth = 0; t != null && depth < MAX_CAUSE_CHAIN_DEPTH; depth++) {
			if (t instanceof org.hibernate.exception.ConstraintViolationException
			        || t instanceof SQLIntegrityConstraintViolationException
			        || t instanceof DataIntegrityViolationException) {
				return true;
			}
			t = t.getCause();
		}
		
		return false;
	}
	
	private static final class SequencePool {
		
		private final Object refillLock = new Object();
		
		private int next = 1;
		
		private int max = 0;
		
		private boolean invalidated = false;
		
		synchronized boolean isInvalidated() {
			return invalidated;
		}
		
		synchronized void invalidate() {
			invalidated = true;
		}
		
		synchronized Integer tryTake() {
			if (invalidated || next > max) {
				return null;
			}
			
			return next++;
		}
		
		synchronized Integer refillAndTake(int first, int blockSize) {
			if (invalidated) {
				return null;
			}
			
			next = first;
			max = first + blockSize - 1;
			
			return next++;
		}
	}
}
