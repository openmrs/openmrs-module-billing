/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.base.entity.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Criteria;
import org.hibernate.Version;
import org.hibernate.criterion.Projection;
import org.hibernate.transform.ResultTransformer;

/**
 * Wrapper class to handle differences between Hibernate 3 and 4 for the CriteriaImpl class.
 */
@Slf4j
public class CriteriaImplWrapper {
	
	public static final long serialVersionUID = 0L;
	
	private static final String VERSION_3_CRITERIA_CLASS = "org.hibernate.impl.CriteriaImpl";
	
	private static final String VERSION_4_CRITERIA_CLASS = "org.hibernate.internal.CriteriaImpl";
	
	private static final String PROJECTION_METHOD_NAME = "getProjection";
	
	private static final String RESULT_TRANSFORMER_METHOD_NAME = "getResultTransformer";
	
	private static Method getProjectionMethod;
	
	private static Method getResultTransformerMethod;
	
	private final Object impl;
	
	static {
		Class<?> cls;
		
		try {
			log.debug("Hibernate version: {}", Version.getVersionString());
			if (org.hibernate.Version.getVersionString().startsWith("3.")) {
				cls = Class.forName(VERSION_3_CRITERIA_CLASS);
			} else {
				cls = Class.forName(VERSION_4_CRITERIA_CLASS);
			}
			
			getProjectionMethod = cls.getMethod(PROJECTION_METHOD_NAME);
			getResultTransformerMethod = cls.getMethod(RESULT_TRANSFORMER_METHOD_NAME);
		}
		catch (ClassNotFoundException ex) {
			getProjectionMethod = null;
			getResultTransformerMethod = null;
			
			log.error("Could not determine hibernate version.", ex);
		}
		catch (NoSuchMethodException e) {
			getProjectionMethod = null;
			getResultTransformerMethod = null;
			
			log.error("Could not get CriteriaImpl method.", e);
		}
	}
	
	public CriteriaImplWrapper(Criteria criteria) {
		impl = criteria;
	}
	
	public Projection getProjection() {
		if (getProjectionMethod != null) {
			try {
				return (Projection) getProjectionMethod.invoke(impl);
			}
			catch (IllegalAccessException e) {
				log.error("Could not get CriteriaImpl Projection", e);
			}
			catch (InvocationTargetException e) {
				log.error("Could not get CriteriaImpl Projection", e);
			}
			
		}
		
		return null;
	}
	
	public ResultTransformer getResultTransformer() {
		if (getResultTransformerMethod != null) {
			try {
				return (ResultTransformer) getResultTransformerMethod.invoke(impl);
			}
			catch (IllegalAccessException e) {
				log.error("Could not get CriteriaImpl ResultTransformer", e);
			}
			catch (InvocationTargetException e) {
				log.error("Could not get CriteriaImpl ResultTransformer", e);
			}
		}
		
		return null;
	}
}
