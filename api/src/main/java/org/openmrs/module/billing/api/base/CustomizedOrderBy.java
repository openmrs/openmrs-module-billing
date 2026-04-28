/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.base;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Order;

/**
 * Allows for ordering hibernate queries by some customized sql (for example, a database function).
 * Adapted from: http://blog.hexican.com/2012/05/how-to-customize-hibernate-order-by/
 */
public class CustomizedOrderBy extends Order {
	
	private final String sqlExpression;
	
	public static Order asc(String sqlFormula) {
		if (!StringUtils.endsWith(sqlFormula, " asc")) {
			sqlFormula += " asc";
		}
		
		return new CustomizedOrderBy(sqlFormula);
	}
	
	public static Order desc(String sqlFormula) {
		if (!StringUtils.endsWith(sqlFormula, " desc")) {
			sqlFormula += " desc";
		}
		
		return new CustomizedOrderBy(sqlFormula);
	}
	
	protected CustomizedOrderBy(String sqlExpression) {
		super(sqlExpression, true);
		
		this.sqlExpression = sqlExpression;
	}
	
	public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
		return sqlExpression;
	}
	
	public String toString() {
		return sqlExpression;
	}
	
}
