/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.billing.advice;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Order;
import org.springframework.aop.AfterReturningAdvice;

public class OrderCreationAfterReturningAdvice implements AfterReturningAdvice {
	
	private static final Log LOG = LogFactory.getLog(OrderCreationAfterReturningAdvice.class);
	
	/**
	 * This is called immediately an order is saved
	 */
	@Override
	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
		// This advice will be executed after the saveOrder method is successfully called
		try {
			// Extract the Order object from the arguments
			if (method.getName().equals("saveOrder") && args.length > 0 && args[0] instanceof Order) {
				Order order = (Order) args[0];
				LOG.debug("Order successfully saved: " + order.getOrderId());
			}
		}
		catch (Exception e) {
			LOG.error(e);
			e.printStackTrace();
		}
	}
	
}
