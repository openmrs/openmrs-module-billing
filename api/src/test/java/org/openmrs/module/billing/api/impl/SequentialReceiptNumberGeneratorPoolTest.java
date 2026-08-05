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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.LockAcquisitionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.billing.api.ISequentialReceiptNumberGeneratorService;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Tests the in-memory pool distribution of
 * {@link SequentialReceiptNumberGeneratorServiceImpl#reserveNextSequence(String)} without a
 * database: the block reservation proxy is replaced with an atomic in-memory counter.
 */
public class SequentialReceiptNumberGeneratorPoolTest {
	
	private static final int BLOCK_SIZE = SequentialReceiptNumberGeneratorServiceImpl.DEFAULT_SEQUENCE_BLOCK_SIZE;
	
	private ISequentialReceiptNumberGeneratorService blockReserver;
	
	private SequentialReceiptNumberGeneratorServiceImpl service;
	
	@Before
	public void before() {
		blockReserver = mock(ISequentialReceiptNumberGeneratorService.class);
		
		ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();
		when(blockReserver.reserveSequenceBlock(anyString(), anyInt())).thenAnswer(invocation -> {
			String group = invocation.getArgument(0);
			int blockSize = invocation.getArgument(1);
			
			return counters.computeIfAbsent(group, g -> new AtomicInteger()).getAndAdd(blockSize) + 1;
		});
		
		service = new SequentialReceiptNumberGeneratorServiceImpl() {
			
			@Override
			protected ISequentialReceiptNumberGeneratorService getProxy() {
				return blockReserver;
			}
			
			@Override
			protected int getBlockSize() {
				return BLOCK_SIZE;
			}
		};
	}
	
	@Test
	public void reserveNextSequence_shouldHandOutUniqueGaplessValuesUnderConcurrency() throws Exception {
		final int threadCount = 20;
		final int callsPerThread = 500;
		final int total = threadCount * callsPerThread;
		
		final ConcurrentLinkedQueue<Integer> results = new ConcurrentLinkedQueue<>();
		final ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();
		final CountDownLatch start = new CountDownLatch(1);
		
		List<Thread> threads = new ArrayList<>();
		for (int i = 0; i < threadCount; i++) {
			Thread thread = new Thread(() -> {
				try {
					start.await();
					for (int call = 0; call < callsPerThread; call++) {
						results.add(service.reserveNextSequence("main"));
						service.reserveNextSequence("other");
					}
				}
				catch (Throwable t) {
					failures.add(t);
				}
			});
			threads.add(thread);
			thread.start();
		}
		
		start.countDown();
		for (Thread thread : threads) {
			thread.join(60000);
		}
		
		Assert.assertTrue("Worker threads failed: " + failures, failures.isEmpty());
		Assert.assertEquals(total, results.size());
		
		Set<Integer> unique = new HashSet<>(results);
		Assert.assertEquals("Duplicate sequence values were handed out", total, unique.size());
		Assert.assertTrue(unique.contains(1));
		Assert.assertTrue(unique.contains(total));
		
		verify(blockReserver, times(total / BLOCK_SIZE)).reserveSequenceBlock("main", BLOCK_SIZE);
	}
	
	@Test
	public void reserveNextSequence_shouldRetryOnceWhenBlockReservationHitsAConstraintViolation() {
		when(blockReserver.reserveSequenceBlock("race", BLOCK_SIZE))
		        .thenThrow(new DataIntegrityViolationException("duplicate", new ConstraintViolationException("duplicate",
		                new SQLIntegrityConstraintViolationException(), "cashier_seq_group_sequence")))
		        .thenReturn(1);
		
		Assert.assertEquals(1, service.reserveNextSequence("race"));
		
		verify(blockReserver, times(2)).reserveSequenceBlock("race", BLOCK_SIZE);
	}
	
	@Test
	public void reserveNextSequence_shouldRetryOnceWhenBlockReservationHitsADeadlock() {
		when(blockReserver.reserveSequenceBlock("deadlock", BLOCK_SIZE))
		        .thenThrow(new CannotAcquireLockException("deadlock",
		                new LockAcquisitionException("deadlock", new SQLException("Deadlock found", "40001", 1213))))
		        .thenReturn(1);
		
		Assert.assertEquals(1, service.reserveNextSequence("deadlock"));
		
		verify(blockReserver, times(2)).reserveSequenceBlock("deadlock", BLOCK_SIZE);
	}
	
	@Test
	public void reserveNextSequence_shouldNotRetryOnOtherExceptions() {
		when(blockReserver.reserveSequenceBlock("failing", BLOCK_SIZE)).thenThrow(new IllegalStateException("boom"));
		
		try {
			service.reserveNextSequence("failing");
			Assert.fail("Expected IllegalStateException");
		}
		catch (IllegalStateException expected) {}
		
		verify(blockReserver, times(1)).reserveSequenceBlock("failing", BLOCK_SIZE);
	}
	
	@Test
	public void reserveNextSequence_shouldDiscardABlockReservedWhileThePoolWasInvalidated() {
		final AtomicInteger calls = new AtomicInteger();
		when(blockReserver.reserveSequenceBlock("invalidated", BLOCK_SIZE)).thenAnswer(invocation -> {
			if (calls.incrementAndGet() == 1) {
				service.invalidatePool("invalidated");
				return 1;
			}
			return 501;
		});
		
		Assert.assertEquals(501, service.reserveNextSequence("invalidated"));
		
		verify(blockReserver, times(2)).reserveSequenceBlock("invalidated", BLOCK_SIZE);
	}
	
	@Test
	public void reserveNextSequence_shouldThrowIllegalArgumentExceptionIfTheGroupIsNull() {
		try {
			service.reserveNextSequence(null);
			Assert.fail("Expected IllegalArgumentException");
		}
		catch (IllegalArgumentException expected) {}
		
		verify(blockReserver, never()).reserveSequenceBlock(anyString(), anyInt());
	}
}
