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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.TestOrder;
import org.openmrs.User;
import org.openmrs.VisitAttribute;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.IBillableItemsService;
import org.openmrs.module.billing.api.ICashPointService;
import org.openmrs.module.billing.api.ItemPriceService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.BillableServiceStatus;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.openmrs.module.billing.api.search.BillableServiceSearch;
import org.openmrs.module.stockmanagement.api.StockManagementService;
import org.openmrs.module.stockmanagement.api.model.StockItem;
import org.springframework.aop.MethodBeforeAdvice;

public class OrderCreationMethodBeforeAdvice implements MethodBeforeAdvice {
	
	private static final Log LOG = LogFactory.getLog(OrderCreationMethodBeforeAdvice.class);
	
	private static final String DISABLE_DRUG_ORDER_BILL_AUTO_CREATION = "billing.disableDrugOrderBillAutoCreation";
	
	OrderService orderService = Context.getOrderService();
	
	BillService billService = Context.getService(BillService.class);
	
	StockManagementService stockService = Context.getService(StockManagementService.class);
	
	ItemPriceService priceService = Context.getService(ItemPriceService.class);
	
	ICashPointService cashPointService = Context.getService(ICashPointService.class);
	
	@Override
	public void before(Method method, Object[] args, Object target) throws Throwable {
		try {
			// Extract the Order object from the arguments
			if (method.getName().equals("saveOrder") && args.length > 0 && args[0] instanceof Order) {
				Order order = (Order) args[0];
				if (!fetchPatientPayingCategory(order)) {
					return;
				}
				
				// Check if the order already exists by looking at the database
				if (orderService.getOrderByUuid(order.getUuid()) == null) {
					// This is a new order
					Patient patient = order.getPatient();
					String cashierUUID = Context.getAuthenticatedUser().getUuid();
					if (order instanceof DrugOrder) {
						// Check if drug order bill autocreation is disabled
						boolean disableAutoBillCreation = Boolean.parseBoolean(
						    Context.getAdministrationService().getGlobalProperty(DISABLE_DRUG_ORDER_BILL_AUTO_CREATION));
						if (disableAutoBillCreation) {
							return; // Skip drug order bill processing
						}
						DrugOrder drugOrder = (DrugOrder) order;
						Integer drugID = drugOrder.getDrug() != null ? drugOrder.getDrug().getDrugId() : 0;
						double drugQuantity = drugOrder.getQuantity() != null ? drugOrder.getQuantity() : 0.0;
						List<StockItem> stockItems = stockService.getStockItemByDrug(drugID);
						if (!stockItems.isEmpty()) {
							addBillItemToBill(order, patient, cashierUUID, stockItems.get(0), null, (int) drugQuantity,
							    order.getDateActivated());
						}
					} else if (order instanceof TestOrder) {
						TestOrder testOrder = (TestOrder) order;
						BillableService searchTemplate = new BillableService();
						searchTemplate.setConcept(testOrder.getConcept());
						searchTemplate.setServiceStatus(BillableServiceStatus.ENABLED);
						
						IBillableItemsService service = Context.getService(IBillableItemsService.class);
						List<BillableService> searchResult = service.findServices(new BillableServiceSearch(searchTemplate));
						if (!searchResult.isEmpty()) {
							addBillItemToBill(order, patient, cashierUUID, null, searchResult.get(0), 1,
							    order.getDateActivated());
							
						}
					}
				}
			}
		}
		catch (Exception e) {
			LOG.error(e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds a bill item to the cashier module
	 *
	 * @param patient
	 * @param cashierUUID
	 */
	public void addBillItemToBill(Order order, Patient patient, String cashierUUID, StockItem stockitem,
	        BillableService service, Integer quantity, Date orderDate) {
		try {
			// Search for a bill
			Bill activeBill = new Bill();
			activeBill.setPatient(patient);
			activeBill.setStatus(BillStatus.PENDING);
			
			// Bill Item
			BillLineItem billLineItem = new BillLineItem();
			List<CashierItemPrice> itemPrices = new ArrayList<>();
			if (stockitem != null) {
				billLineItem.setItem(stockitem);
				itemPrices = priceService.getItemPrice(stockitem);
			} else if (service != null) {
				billLineItem.setBillableService(service);
				itemPrices = priceService.getServicePrice(service);
			}
			
			if (!itemPrices.isEmpty()) {
				List<CashierItemPrice> matchingPrices = itemPrices.stream()
				        .filter(p -> p.getPaymentMode().getUuid().equals(fetchPatientPayment(order)))
				        .collect(Collectors.toList());
				billLineItem.setPrice(
				    matchingPrices.isEmpty() ? itemPrices.get(0).getPrice() : matchingPrices.get(0).getPrice());
			} else {
				billLineItem.setPrice(new BigDecimal("0.0"));
			}
			billLineItem.setQuantity(quantity);
			billLineItem.setPaymentStatus(BillStatus.PENDING);
			billLineItem.setLineItemOrder(0);
			
			// Bill
			User user = Context.getAuthenticatedUser();
			List<Provider> providers = new ArrayList<>(Context.getProviderService().getProvidersByPerson(user.getPerson()));
			
			if (!providers.isEmpty()) {
				activeBill.setCashier(providers.get(0));
				List<CashPoint> cashPoints = cashPointService.getAll();
				activeBill.setCashPoint(cashPoints.get(0));
				activeBill.addLineItem(billLineItem);
				activeBill.setStatus(BillStatus.PENDING);
				billService.saveBill(activeBill);
			}
			
		}
		catch (Exception ex) {
			LOG.error(ex);
			ex.printStackTrace();
		}
	}
	
	private String fetchPatientPayment(Order order) {
		String patientPayingMethod = "";
		Collection<VisitAttribute> visitAttributeList = order.getEncounter().getVisit().getActiveAttributes();
		
		for (VisitAttribute attribute : visitAttributeList) {
			if (attribute.getAttributeType().getUuid().equals("c39b684c-250f-4781-a157-d6ad7353bc90")
			        && !attribute.getVoided()) {
				patientPayingMethod = attribute.getValueReference();
			}
		}
		return patientPayingMethod;
	}
	
	private boolean fetchPatientPayingCategory(Order order) {
		boolean isPaying = false;
		Collection<VisitAttribute> visitAttributeList = order.getEncounter().getVisit().getActiveAttributes();
		
		for (VisitAttribute attribute : visitAttributeList) {
			if (attribute.getAttributeType().getUuid().equals("caf2124f-00a9-4620-a250-efd8535afd6d")
			        && attribute.getValueReference().equals("1c30ee58-82d4-4ea4-a8c1-4bf2f9dfc8cf")) {
				return true;
			}
		}
		
		return isPaying;
	}
}
