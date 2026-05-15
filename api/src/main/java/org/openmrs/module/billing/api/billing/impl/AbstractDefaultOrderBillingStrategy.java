/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.billing.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Provider;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.module.billing.api.BillExemptionService;
import org.openmrs.module.billing.api.BillLineItemService;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.CashPointService;
import org.openmrs.module.billing.api.billing.BillingResult;
import org.openmrs.module.billing.api.evaluator.ExemptionRuleEngine;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillExemption;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillLineItemStatus;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.api.model.ExemptionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Default implementation base class that provides shared logic for bill creation, line item
 * voiding, exemption checking, and idempotency. Subclasses implement
 * {@link #createBillLineItem(Order)} to build the line item specific to their order type.
 * <p>
 * Strategies that need completely custom behavior should extend
 * {@link AbstractOrderBillingStrategy} directly instead.
 */
@Slf4j
@Setter(onMethod_ = @Autowired)
public abstract class AbstractDefaultOrderBillingStrategy extends AbstractOrderBillingStrategy {
	
	protected BillService billService;
	
	protected BillLineItemService billLineItemService;
	
	protected CashPointService cashPointService;
	
	protected BillExemptionService billExemptionService;
	
	@Qualifier("ruleEngine")
	protected ExemptionRuleEngine exemptionRuleEngine;
	
	protected ProgramWorkflowService programWorkflowService;
	
	protected PlatformTransactionManager transactionManager;
	
	{
		setSupportedActions(EnumSet.of(Order.Action.NEW, Order.Action.RENEW, Order.Action.REVISE, Order.Action.DISCONTINUE));
	}
	
	@Override
	protected BillingResult handleNewOrder(Order order) {
		return createBillIfAbsent(order);
	}
	
	@Override
	protected BillingResult handleRenewOrder(Order order) {
		return createBillIfAbsent(order);
	}
	
	@Override
	protected BillingResult handleRevisedOrder(Order order) {
		TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
		return txTemplate.execute(status -> {
			voidPreviousLineItem(order, "Order revised");
			return createBillIfAbsent(order);
		});
	}
	
	@Override
	protected BillingResult handleDiscontinuedOrder(Order order) {
		voidPreviousLineItem(order, "Order discontinued");
		return BillingResult.discontinued();
	}
	
	protected void voidPreviousLineItem(Order order, String reason) {
		Order previousOrder = order.getPreviousOrder();
		if (previousOrder == null) {
			log.warn("No previous order found for {} order: {}", order.getAction(), order.getUuid());
			return;
		}
		
		BillLineItem existingLineItem = billLineItemService.getBillLineItemByOrder(previousOrder);
		if (existingLineItem == null) {
			log.warn("No bill line item found for previous order: {}", previousOrder.getUuid());
			return;
		}
		
		billLineItemService.voidBillLineItem(existingLineItem, reason);
	}
	
	/**
	 * Create the order-type-specific bill line item. Called by the default bill creation pipeline.
	 *
	 * @param order the order to create a line item for
	 * @return the line item, or empty if the order should not be billed
	 */
	protected abstract Optional<BillLineItem> createBillLineItem(Order order);
	
	protected BillingResult createBillIfAbsent(Order order) {
		BillLineItem existingLineItem = billLineItemService.getBillLineItemByOrder(order);
		if (existingLineItem != null) {
			log.info("Bill line item already exists for order: {}, skipping duplicate bill creation", order.getUuid());
			return BillingResult.skipped("Duplicate — bill already exists");
		}
		
		Optional<BillLineItem> lineItemOpt = createBillLineItem(order);
		if (!lineItemOpt.isPresent()) {
			return BillingResult.skipped("No billable item found for order");
		}
		
		return createBill(order.getPatient(), lineItemOpt.get(), order);
	}
	
	protected BillingResult createBill(Patient patient, BillLineItem lineItem, Order order) {
		Provider cashier = resolveCashier(order);
		if (cashier == null) {
			log.error("Cannot resolve cashier for order: {}", order.getUuid());
			return BillingResult.skipped("Cannot resolve cashier");
		}
		
		CashPoint cashPoint = resolveCashPoint();
		if (cashPoint == null) {
			log.error("Cannot resolve cash point for order: {}", order.getUuid());
			return BillingResult.skipped("Cannot resolve cash point");
		}
		
		Bill bill = new Bill();
		bill.setPatient(patient);
		bill.setStatus(BillStatus.PENDING);
		bill.setCashier(cashier);
		bill.setCashPoint(cashPoint);
		bill.addLineItem(lineItem);
		
		Bill savedBill = billService.saveBill(bill);
		return BillingResult.created(savedBill);
	}
	
	// resolveCashier() and resolveCashPoint() are inherited from the interface
	// and must be implemented by concrete strategy classes.
	
	/**
	 * Create a bill line item with the common fields populated. Subclasses should set the type-specific
	 * fields (item or billable service) on the returned line item.
	 */
	protected BillLineItem createLineItem(BigDecimal price, int quantity, BillLineItemStatus paymentStatus, Order order) {
		BillLineItem lineItem = new BillLineItem();
		lineItem.setPrice(price);
		lineItem.setQuantity(quantity);
		lineItem.setStatus(paymentStatus);
		lineItem.setLineItemOrder(0);
		lineItem.setOrder(order);
		return lineItem;
	}
	
	protected boolean checkIfOrderIsExempted(Order order, ExemptionType exemptionType) {
		if (order == null || order.getConcept() == null) {
			return false;
		}
		
		List<BillExemption> exemptions = billExemptionService.getExemptionsByConcept(order.getConcept(), exemptionType,
		    false);
		if (exemptions == null || exemptions.isEmpty()) {
			return false;
		}
		
		Map<String, Object> variables = buildExemptionVariables(order);
		
		for (BillExemption exemption : exemptions) {
			if (exemptionRuleEngine.isExemptionApplicable(exemption, variables)) {
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
		
		List<PatientProgram> programs = programWorkflowService.getPatientPrograms(patient, null, null, null, new Date(),
		    null, false);
		List<String> activePrograms = programs.stream().filter(PatientProgram::getActive)
		        .map(pp -> pp.getProgram().getName()).collect(Collectors.toList());
		variables.put("activePrograms", activePrograms);
		
		return variables;
	}
}
