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
import java.util.ArrayList;
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
import org.openmrs.TestOrder;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillExemptionService;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.BillableServiceService;
import org.openmrs.module.billing.api.CashPointService;
import org.openmrs.module.billing.api.ItemPriceService;
import org.openmrs.module.billing.api.billing.OrderBillingStrategy;
import org.openmrs.module.billing.api.evaluator.ExemptionRuleEngine;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillExemption;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.BillableServiceStatus;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.openmrs.module.billing.api.model.ExemptionType;
import org.openmrs.module.billing.api.search.BillableServiceSearch;

/**
 * Default billing strategy for {@link TestOrder}s. Creates a bill line item based on the billable
 * service linked to the test order's concept. Checks exemption rules to determine if the line item
 * should be marked as EXEMPTED or PENDING.
 */
@Slf4j
public class TestOrderBillingStrategy implements OrderBillingStrategy {
	
	@Override
	public boolean supports(Order order) {
		if (!(order instanceof TestOrder)) {
			return false;
		}
		Order.Action action = order.getAction();
		return action == Order.Action.NEW;
	}
	
	@Override
	public Optional<Bill> generateBill(Order order) {
		try {
			TestOrder testOrder = (TestOrder) order;
			Patient patient = order.getPatient();
			
			BillableServiceService serviceService = Context.getService(BillableServiceService.class);
			BillableServiceSearch searchTemplate = new BillableServiceSearch();
			searchTemplate.setConceptUuid(testOrder.getConcept().getUuid());
			searchTemplate.setServiceStatus(BillableServiceStatus.ENABLED);
			
			List<BillableService> searchResult = serviceService.getBillableServices(searchTemplate, null);
			if (searchResult.isEmpty()) {
				log.debug("No billable service found for concept: {}", testOrder.getConcept().getUuid());
				return Optional.empty();
			}
			
			boolean isExempted = checkIfOrderIsExempted(order, ExemptionType.SERVICE);
			BillStatus lineItemStatus = isExempted ? BillStatus.EXEMPTED : BillStatus.PENDING;
			
			BillableService billableService = searchResult.get(0);
			BillLineItem lineItem = createLineItem(billableService, lineItemStatus, order);
			
			return saveBill(patient, lineItem, order);
		}
		catch (Exception e) {
			log.error("Error generating bill for test order: {}", e.getMessage(), e);
			return Optional.empty();
		}
	}
	
	protected BillLineItem createLineItem(BillableService billableService, BillStatus lineItemStatus, Order order) {
		ItemPriceService priceService = Context.getService(ItemPriceService.class);
		
		BillLineItem lineItem = new BillLineItem();
		lineItem.setBillableService(billableService);
		
		List<CashierItemPrice> itemPrices = priceService.getServicePrice(billableService);
		if (!itemPrices.isEmpty()) {
			lineItem.setPrice(itemPrices.get(0).getPrice());
		} else {
			lineItem.setPrice(BigDecimal.ZERO);
		}
		
		lineItem.setQuantity(1);
		lineItem.setPaymentStatus(lineItemStatus);
		lineItem.setLineItemOrder(0);
		lineItem.setOrder(order);
		
		return lineItem;
	}
	
	protected Optional<Bill> saveBill(Patient patient, BillLineItem lineItem, Order order) {
		BillService billService = Context.getService(BillService.class);
		CashPointService cashPointService = Context.getService(CashPointService.class);
		
		List<Provider> providers = new ArrayList<>(
		        Context.getProviderService().getProvidersByPerson(order.getCreator().getPerson()));
		
		if (providers.isEmpty()) {
			log.error("Order creator is not a provider, cannot create bill for order: {}", order.getUuid());
			return Optional.empty();
		}
		
		List<CashPoint> cashPoints = cashPointService.getAllCashPoints(false);
		if (cashPoints.isEmpty()) {
			log.error("No cash points configured, cannot create bill for order: {}", order.getUuid());
			return Optional.empty();
		}
		
		Bill bill = new Bill();
		bill.setPatient(patient);
		bill.setStatus(BillStatus.PENDING);
		bill.setCashier(providers.get(0));
		bill.setCashPoint(cashPoints.get(0));
		bill.addLineItem(lineItem);
		
		Bill savedBill = billService.saveBill(bill);
		return Optional.of(savedBill);
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
	
	private Map<String, Object> buildExemptionVariables(Order order) {
		Map<String, Object> variables = new HashMap<>();
		ProgramWorkflowService workflowService = Context.getProgramWorkflowService();
		
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
		
		List<PatientProgram> programs = workflowService.getPatientPrograms(patient, null, null, null, new Date(), null,
		    false);
		List<String> activePrograms = programs.stream().filter(PatientProgram::getActive)
		        .map(pp -> pp.getProgram().getName()).collect(Collectors.toList());
		variables.put("activePrograms", activePrograms);
		
		return variables;
	}
}
