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
package org.openmrs.module.billing.api.billing.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Provider;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillExemptionService;
import org.openmrs.module.billing.api.BillLineItemService;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.CashPointService;
import org.openmrs.module.billing.api.billing.OrderBillingStrategy;
import org.openmrs.module.billing.api.evaluator.ExemptionRuleEngine;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillExemption;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.api.model.ExemptionType;
import org.springframework.core.Ordered;

/**
 * Base class for billing strategies that provides shared logic for bill creation, line item
 * voiding, and exemption checking. Subclasses implement {@link #handleNewOrder(Order)} to create
 * line items specific to their order type.
 */
@Slf4j
public abstract class AbstractOrderBillingStrategy implements OrderBillingStrategy {
	
	@Override
	public Optional<Bill> generateBill(Order order) {
		try {
			switch (order.getAction()) {
				case NEW:
					BillLineItemService itemService = Context.getService(BillLineItemService.class);
					BillLineItem existingLineItem = itemService.getBillLineItemByOrder(order);
					if (existingLineItem != null) {
						log.info("Bill line item already exists for order: {}, skipping duplicate bill creation",
						    order.getUuid());
						return Optional.of(existingLineItem.getBill());
					}
					return handleNewOrder(order).flatMap(lineItem -> saveBill(order.getPatient(), lineItem, order));
				case REVISE:
					return handleRevisedOrder(order);
				case DISCONTINUE:
					handleDiscontinuedOrder(order);
					return Optional.empty();
				default:
					return Optional.empty();
			}
		}
		catch (Exception e) {
			log.error("Error processing order (action={}): {}", order.getAction(), e.getMessage(), e);
			return Optional.empty();
		}
	}
	
	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}
	
	/**
	 * Whether the order's action is one that this base class handles (NEW, REVISE, DISCONTINUE).
	 * Subclasses can call this from {@link #supports(Order)} to avoid repeating the action check.
	 */
	protected boolean isSupportedAction(Order order) {
		Order.Action action = order.getAction();
		return action == Order.Action.NEW || action == Order.Action.REVISE || action == Order.Action.DISCONTINUE;
	}
	
	protected abstract Optional<BillLineItem> handleNewOrder(Order order);
	
	protected Optional<Bill> handleRevisedOrder(Order order) {
		voidPreviousLineItem(order, "Order revised");
		return handleNewOrder(order).flatMap(lineItem -> saveBill(order.getPatient(), lineItem, order));
	}
	
	protected void handleDiscontinuedOrder(Order order) {
		voidPreviousLineItem(order, "Order discontinued");
	}
	
	protected void voidPreviousLineItem(Order order, String reason) {
		Order previousOrder = order.getPreviousOrder();
		if (previousOrder == null) {
			log.warn("No previous order found for {} order: {}", order.getAction(), order.getUuid());
			return;
		}
		
		BillLineItemService lineItemService = Context.getService(BillLineItemService.class);
		BillLineItem existingLineItem = lineItemService.getBillLineItemByOrder(previousOrder);
		if (existingLineItem == null) {
			log.warn("No bill line item found for previous order: {}", previousOrder.getUuid());
			return;
		}
		
		existingLineItem.setVoided(true);
		existingLineItem.setVoidReason(reason);
		existingLineItem.setDateVoided(new Date());
		existingLineItem.setVoidedBy(order.getCreator());
		
		BillService billService = Context.getService(BillService.class);
		billService.saveBill(existingLineItem.getBill());
	}
	
	protected Optional<Bill> saveBill(Patient patient, BillLineItem lineItem, Order order) {
		BillService billService = Context.getService(BillService.class);
		
		Provider cashier = resolveCashier(order);
		if (cashier == null) {
			log.error("Cannot resolve cashier for order: {}", order.getUuid());
			return Optional.empty();
		}
		
		CashPoint cashPoint = resolveCashPoint();
		if (cashPoint == null) {
			log.error("Cannot resolve cash point for order: {}", order.getUuid());
			return Optional.empty();
		}
		
		Bill bill = new Bill();
		bill.setPatient(patient);
		bill.setStatus(BillStatus.PENDING);
		bill.setCashier(cashier);
		bill.setCashPoint(cashPoint);
		bill.addLineItem(lineItem);
		
		Bill savedBill = billService.saveBill(bill);
		return Optional.of(savedBill);
	}
	
	/**
	 * Resolve the provider to set as the cashier on the bill. Defaults to the order's orderer.
	 * Subclasses can override to use a different resolution strategy.
	 */
	protected Provider resolveCashier(Order order) {
		return order.getOrderer();
	}
	
	/**
	 * Resolve the cash point for the bill. Defaults to the first available cash point. Subclasses can
	 * override, e.g., to resolve by encounter location.
	 */
	protected CashPoint resolveCashPoint() {
		CashPointService cashPointService = Context.getService(CashPointService.class);
		List<CashPoint> cashPoints = cashPointService.getAllCashPoints(false);
		return cashPoints.isEmpty() ? null : cashPoints.get(0);
	}
	
	/**
	 * Create a bill line item with the common fields populated. Subclasses should set the type-specific
	 * fields (item or billable service) and the price on the returned line item.
	 */
	protected BillLineItem createLineItem(BigDecimal price, int quantity, BillStatus paymentStatus, Order order) {
		BillLineItem lineItem = new BillLineItem();
		lineItem.setPrice(price);
		lineItem.setQuantity(quantity);
		lineItem.setPaymentStatus(paymentStatus);
		lineItem.setLineItemOrder(0);
		lineItem.setOrder(order);
		return lineItem;
	}
	
	protected boolean checkIfOrderIsExempted(Order order, ExemptionType exemptionType) {
		if (order == null || order.getConcept() == null) {
			return false;
		}
		
		BillExemptionService exemptionService = Context.getService(BillExemptionService.class);
		List<BillExemption> exemptions = exemptionService.getExemptionsByConcept(order.getConcept(), exemptionType, false);
		if (exemptions == null || exemptions.isEmpty()) {
			return false;
		}
		
		ExemptionRuleEngine ruleEngine = Context.getRegisteredComponent("ruleEngine", ExemptionRuleEngine.class);
		Map<String, Object> variables = buildExemptionVariables(order);
		
		for (BillExemption exemption : exemptions) {
			if (ruleEngine.isExemptionApplicable(exemption, variables)) {
				return true;
			}
		}
		
		return false;
	}
	
	protected Map<String, Object> buildExemptionVariables(Order order) {
		Map<String, Object> variables = new HashMap<>();
		
		Patient patient = order.getPatient();
		variables.put("patient", patient);
		if (patient != null) {
			variables.put("patientAge", patient.getAge());
		}
		
		Map<String, Object> orderData = new HashMap<>();
		orderData.put("uuid", order.getUuid());
		if (order.getConcept() != null) {
			orderData.put("conceptId", order.getConcept().getConceptId());
		}
		variables.put("order", orderData);
		
		List<PatientProgram> programs = Context.getProgramWorkflowService().getPatientPrograms(patient, null, null, null,
		    new Date(), null, false);
		List<String> activePrograms = programs.stream().filter(PatientProgram::getActive)
		        .map(pp -> pp.getProgram().getName()).collect(Collectors.toList());
		variables.put("activePrograms", activePrograms);
		
		return variables;
	}
}
