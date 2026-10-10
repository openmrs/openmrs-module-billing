/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.base.entity.db.hibernate;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.LockModeType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * A small, JPA criteria based replacement for the legacy Hibernate {@code Criteria} API (removed in
 * Hibernate 6) used by the generic entity data services. It collects restrictions, orderings,
 * paging and locking settings for a single root entity and turns them into a JPA
 * {@link CriteriaQuery}, or into a row count query using the same restrictions.
 *
 * @param <E> The root entity type.
 */
public class EntityCriteria<E> {
	
	/**
	 * Builds a {@link Predicate} against the query root.
	 */
	@FunctionalInterface
	public interface Restriction {
		
		Predicate toPredicate(CriteriaBuilder cb, Root<?> root);
	}
	
	/**
	 * Builds an {@link Order} against the query root.
	 */
	@FunctionalInterface
	public interface SortOrder {
		
		Order toOrder(CriteriaBuilder cb, Root<?> root);
	}
	
	/**
	 * The string matching modes supported by {@link #ilike(String, String, MatchMode)}.
	 */
	public enum MatchMode {
		EXACT,
		START,
		END,
		ANYWHERE
	}
	
	private final Class<E> entityClass;
	
	private final List<Restriction> restrictions = new ArrayList<>();
	
	private final List<SortOrder> orders = new ArrayList<>();
	
	private Integer firstResult;
	
	private Integer maxResults;
	
	private Integer fetchSize;
	
	private LockModeType lockMode;
	
	public EntityCriteria(Class<E> entityClass) {
		this.entityClass = entityClass;
	}
	
	public Class<E> getEntityClass() {
		return entityClass;
	}
	
	public EntityCriteria<E> add(Restriction restriction) {
		if (restriction != null) {
			restrictions.add(restriction);
		}
		return this;
	}
	
	public EntityCriteria<E> addOrder(SortOrder order) {
		if (order != null) {
			orders.add(order);
		}
		return this;
	}
	
	public Integer getFirstResult() {
		return firstResult;
	}
	
	public EntityCriteria<E> setFirstResult(Integer firstResult) {
		this.firstResult = firstResult;
		return this;
	}
	
	public Integer getMaxResults() {
		return maxResults;
	}
	
	public EntityCriteria<E> setMaxResults(Integer maxResults) {
		this.maxResults = maxResults;
		return this;
	}
	
	public Integer getFetchSize() {
		return fetchSize;
	}
	
	public EntityCriteria<E> setFetchSize(Integer fetchSize) {
		this.fetchSize = fetchSize;
		return this;
	}
	
	public LockModeType getLockMode() {
		return lockMode;
	}
	
	public EntityCriteria<E> setLockMode(LockModeType lockMode) {
		this.lockMode = lockMode;
		return this;
	}
	
	/**
	 * Creates the select query for the root entity, with the restrictions and orderings applied.
	 */
	public CriteriaQuery<E> toQuery(CriteriaBuilder cb) {
		CriteriaQuery<E> query = cb.createQuery(entityClass);
		Root<E> root = query.from(entityClass);
		query.select(root);
		query.where(toPredicates(cb, root));
		
		List<Order> jpaOrders = new ArrayList<>(orders.size());
		for (SortOrder order : orders) {
			jpaOrders.add(order.toOrder(cb, root));
		}
		if (!jpaOrders.isEmpty()) {
			query.orderBy(jpaOrders);
		}
		
		return query;
	}
	
	/**
	 * Creates a row count query for the root entity, with the restrictions applied.
	 */
	public CriteriaQuery<Long> toCountQuery(CriteriaBuilder cb) {
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<E> root = query.from(entityClass);
		query.select(cb.count(root));
		query.where(toPredicates(cb, root));
		
		return query;
	}
	
	private Predicate[] toPredicates(CriteriaBuilder cb, Root<?> root) {
		List<Predicate> predicates = new ArrayList<>(restrictions.size());
		for (Restriction restriction : restrictions) {
			predicates.add(restriction.toPredicate(cb, root));
		}
		return predicates.toArray(new Predicate[0]);
	}
	
	@SuppressWarnings("unchecked")
	private static <T> Path<T> path(Root<?> root, String property) {
		Path<?> path = root;
		for (String part : property.split("\\.")) {
			path = path.get(part);
		}
		return (Path<T>) path;
	}
	
	public static Restriction eq(String property, Object value) {
		return (cb, root) -> cb.equal(path(root, property), value);
	}
	
	public static Restriction isNull(String property) {
		return (cb, root) -> cb.isNull(path(root, property));
	}
	
	public static <Y extends Comparable<? super Y>> Restriction ge(String property, Y value) {
		return (cb, root) -> cb.greaterThanOrEqualTo(EntityCriteria.<Y> path(root, property), value);
	}
	
	public static <Y extends Comparable<? super Y>> Restriction le(String property, Y value) {
		return (cb, root) -> cb.lessThanOrEqualTo(EntityCriteria.<Y> path(root, property), value);
	}
	
	public static <Y extends Comparable<? super Y>> Restriction between(String property, Y low, Y high) {
		return (cb, root) -> cb.between(EntityCriteria.<Y> path(root, property), low, high);
	}
	
	/**
	 * Case-insensitive like, with the value wrapped in wildcards according to the match mode.
	 */
	public static Restriction ilike(String property, String value, MatchMode matchMode) {
		return (cb, root) -> {
			Expression<String> lowered = cb.lower(EntityCriteria.<String> path(root, property));
			String pattern = value == null ? null : value.toLowerCase();
			if (pattern != null) {
				switch (matchMode == null ? MatchMode.EXACT : matchMode) {
					case START:
						pattern = pattern + "%";
						break;
					case END:
						pattern = "%" + pattern;
						break;
					case ANYWHERE:
						pattern = "%" + pattern + "%";
						break;
					default:
						break;
				}
			}
			return cb.like(lowered, pattern);
		};
	}
	
	public static Restriction and(Restriction... restrictions) {
		return (cb, root) -> {
			Predicate[] predicates = new Predicate[restrictions.length];
			for (int i = 0; i < restrictions.length; i++) {
				predicates[i] = restrictions[i].toPredicate(cb, root);
			}
			return cb.and(predicates);
		};
	}
	
	public static Restriction or(Restriction... restrictions) {
		return (cb, root) -> {
			Predicate[] predicates = new Predicate[restrictions.length];
			for (int i = 0; i < restrictions.length; i++) {
				predicates[i] = restrictions[i].toPredicate(cb, root);
			}
			return cb.or(predicates);
		};
	}
	
	public static SortOrder asc(String property) {
		return (cb, root) -> cb.asc(path(root, property));
	}
	
	public static SortOrder desc(String property) {
		return (cb, root) -> cb.desc(path(root, property));
	}
}
