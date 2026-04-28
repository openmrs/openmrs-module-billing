/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.base.entity.search;

import java.util.Date;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.openmrs.OpenmrsObject;

/**
 * Base template search class for {@link org.openmrs.OpenmrsObject} models.
 *
 * @param <T> The model class.
 */
public class BaseObjectTemplateSearch<T extends OpenmrsObject> {
	
	public static final long serialVersionUID = 0L;
	
	private T template;
	
	public BaseObjectTemplateSearch(T template) {
		this.template = template;
	}
	
	public T getTemplate() {
		return template;
	}
	
	public void setTemplate(T template) {
		this.template = template;
	}
	
	public void updateCriteria(Criteria criteria) {
	}
	
	protected Criterion createCriterion(String field, Object value, ComparisonType comparisonType) {
		ComparisonType comparison = comparisonType == null ? ComparisonType.EQUAL : comparisonType;
		
		Criterion result;
		switch (comparison) {
			case EQUAL:
				result = Restrictions.eq(field, value);
				break;
			case NOT_EQUAL:
				result = Restrictions.ne(field, value);
				break;
			case IS_NULL:
				result = Restrictions.isNull(field);
				break;
			case IS_NOT_NULL:
				result = Restrictions.isNotNull(field);
				break;
			default:
				throw new IllegalArgumentException();
		}
		
		return result;
	}
	
	protected Criterion createCriterion(String field, String value, StringComparisonType comparisonType) {
		StringComparisonType comparison = comparisonType == null ? StringComparisonType.EQUAL : comparisonType;
		
		Criterion result;
		switch (comparison) {
			case EQUAL:
				result = Restrictions.eq(field, value);
				break;
			case NOT_EQUAL:
				result = Restrictions.ne(field, value);
				break;
			case IS_NULL:
				result = Restrictions.isNull(field);
				break;
			case IS_NOT_NULL:
				result = Restrictions.isNotNull(field);
				break;
			case IS_EMPTY:
				result = Restrictions.isEmpty(field);
				break;
			case IS_NOT_EMPTY:
				result = Restrictions.isNotEmpty(field);
				break;
			case LIKE:
				result = Restrictions.ilike(field, value);
				break;
			default:
				throw new IllegalArgumentException();
		}
		
		return result;
	}
	
	protected Criterion createCriterion(String field, Date value, DateComparisonType comparisonType) {
		DateComparisonType comparison = comparisonType == null ? DateComparisonType.EQUAL : comparisonType;
		
		Criterion result;
		switch (comparison) {
			case EQUAL:
				result = Restrictions.eq(field, value);
				break;
			case NOT_EQUAL:
				result = Restrictions.ne(field, value);
				break;
			case IS_NULL:
				result = Restrictions.isNull(field);
				break;
			case IS_NOT_NULL:
				result = Restrictions.isNotNull(field);
				break;
			case GREATER_THAN:
				result = Restrictions.gt(field, value);
				break;
			case GREATER_THAN_EQUAL:
				result = Restrictions.ge(field, value);
				break;
			case LESS_THAN:
				result = Restrictions.lt(field, value);
				break;
			case LESS_THAN_EQUAL:
				result = Restrictions.le(field, value);
				break;
			default:
				throw new IllegalArgumentException();
		}
		
		return result;
	}
	
	/**
	 * Basic comparison types.
	 */
	public enum ComparisonType {
		EQUAL,
		NOT_EQUAL,
		IS_NULL,
		IS_NOT_NULL
	}
	
	/**
	 * String comparison types.
	 */
	public enum StringComparisonType {
		EQUAL,
		NOT_EQUAL,
		IS_NULL,
		IS_NOT_NULL,
		IS_EMPTY,
		IS_NOT_EMPTY,
		LIKE
	}
	
	/**
	 * Date comparison types.
	 */
	public enum DateComparisonType {
		EQUAL,
		NOT_EQUAL,
		IS_NULL,
		IS_NOT_NULL,
		GREATER_THAN,
		GREATER_THAN_EQUAL,
		LESS_THAN,
		LESS_THAN_EQUAL,
	}
}
