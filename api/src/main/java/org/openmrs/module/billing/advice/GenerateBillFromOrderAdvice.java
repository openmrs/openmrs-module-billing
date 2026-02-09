package org.openmrs.module.billing.advice;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Provider;
import org.openmrs.TestOrder;
import org.openmrs.User;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillExemptionService;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.BillableServiceService;
import org.openmrs.module.billing.api.CashPointService;
import org.openmrs.module.billing.api.ItemPriceService;
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
import org.openmrs.module.stockmanagement.api.StockManagementService;
import org.openmrs.module.stockmanagement.api.model.StockItem;
import org.springframework.aop.AfterReturningAdvice;

import javax.annotation.Nullable;
import javax.validation.constraints.Null;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class GenerateBillFromOrderAdvice implements AfterReturningAdvice {
	
	final BillService billService = Context.getService(BillService.class);
	
	final StockManagementService stockService = Context.getService(StockManagementService.class);
	
	final ItemPriceService priceService = Context.getService(ItemPriceService.class);
	
	final CashPointService cashPointService = Context.getService(CashPointService.class);
	
	final ExemptionRuleEngine exemptionRuleEngine = Context.getRegisteredComponent("ruleEngine", ExemptionRuleEngine.class);
	
	final BillExemptionService billExemptionService = Context.getService(BillExemptionService.class);
	
	/**
	 * This is called immediately an order is saved
	 */
	@Override
	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) {
		try {
			ProgramWorkflowService workflowService = Context.getProgramWorkflowService();
			if (method.getName().equals("saveOrder") && args.length > 0 && args[0] instanceof Order) {
				Order order = (Order) args[0];
				
				// Check if the order is a discontinuation, revision, or renewal
				if (order.getAction().equals(Order.Action.DISCONTINUE) || order.getAction().equals(Order.Action.REVISE)
				        || order.getAction().equals(Order.Action.RENEW)) {
					// Do nothing for these actions
					return;
				}
				
				Patient patient = order.getPatient();
				String cashierUUID = Context.getAuthenticatedUser().getUuid();
				
				if (order instanceof DrugOrder) {
					DrugOrder drugOrder = (DrugOrder) order;
					Integer drugID = drugOrder.getDrug() != null ? drugOrder.getDrug().getDrugId() : 0;
					double drugQuantity = drugOrder.getQuantity() != null ? drugOrder.getQuantity() : 0.0;
					List<StockItem> stockItems = stockService.getStockItemByDrug(drugID);
					
					if (!stockItems.isEmpty()) {
						// check from the list for all exemptions
						boolean isExempted = checkIfOrderIsExempted(workflowService, order, ExemptionType.COMMODITY);
						BillStatus lineItemStatus = isExempted ? BillStatus.EXEMPTED : BillStatus.PENDING;
						addBillItemToBill(order, patient, cashierUUID, stockItems.get(0), null, (int) drugQuantity,
						    order.getDateActivated(), lineItemStatus);
					}
				} else if (order instanceof TestOrder) {
					TestOrder testOrder = (TestOrder) order;
					BillableServiceSearch searchTemplate = new BillableServiceSearch();
					searchTemplate.setConceptUuid(testOrder.getConcept().getUuid());
					searchTemplate.setServiceStatus(BillableServiceStatus.ENABLED);
					
					BillableServiceService service = Context.getService(BillableServiceService.class);
					List<BillableService> searchResult = service.getBillableServices(searchTemplate, null);
					if (!searchResult.isEmpty()) {
						boolean isExempted = checkIfOrderIsExempted(workflowService, order, ExemptionType.SERVICE);
						BillStatus lineItemStatus = isExempted ? BillStatus.EXEMPTED : BillStatus.PENDING;
						addBillItemToBill(order, patient, cashierUUID, null, searchResult.get(0), 1,
						    order.getDateActivated(), lineItemStatus);
					}
				}
			}
		}
		catch (Exception e) {
			log.error("Error intercepting order before creation: {}", e.getMessage(), e);
		}
	}
	
	private boolean checkIfOrderIsExempted(ProgramWorkflowService workflowService, Order order,
	        ExemptionType exemptionType) {
		if (order == null || order.getConcept() == null) {
			return false;
		}
		List<BillExemption> exemptions = billExemptionService.getExemptionsByConcept(order.getConcept(), exemptionType,
		    false);
		
		if (exemptions == null || exemptions.isEmpty()) {
			return false;
		}
		
		Map<String, Object> variables = buildVariablesMap(order, workflowService);
		
		for (BillExemption exemption : exemptions) {
			if (exemptionRuleEngine.isExemptionApplicable(exemption, variables)) {
				return true;
			}
		}
		
		return false;
	}
	
	private Map<String, Object> buildVariablesMap(Order order, ProgramWorkflowService workflowService) {
		Map<String, Object> variables = new HashMap<>();
		
		Patient patient = order.getPatient();
		variables.put("patient", patient);
		// We cannot call getAge() method from Java Script
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
	
	/**
	 * Adds a bill item to the cashier module
	 *
	 * @param patient
	 * @param cashierUUID
	 */
	public void addBillItemToBill(Order order, Patient patient, String cashierUUID, StockItem stockitem,
	        BillableService service, Integer quantity, Date orderDate, BillStatus lineItemStatus) {
		try {
			// Search for a bill
			Bill activeBill = new Bill();
			activeBill.setPatient(patient);
			activeBill.setStatus(BillStatus.PENDING);
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
				//List<CashierItemPrice> matchingPrices = itemPrices.stream().filter(p -> p.getPaymentMode().getUuid().equals(fetchPatientPayment(order))).collect(Collectors.toList());
				// billLineItem.setPrice(matchingPrices.isEmpty() ? itemPrices.get(0).getPrice() : matchingPrices.get(0).getPrice());
				billLineItem.setPrice(itemPrices.get(0).getPrice());
			} else {
				if (stockitem != null && stockitem.getPurchasePrice() != null) {
					billLineItem.setPrice(stockitem.getPurchasePrice());
				} else {
					billLineItem.setPrice(BigDecimal.ZERO);
				}
			}
			billLineItem.setQuantity(quantity);
			billLineItem.setPaymentStatus(lineItemStatus);
			billLineItem.setLineItemOrder(0);
			billLineItem.setOrder(order);
			
			// Bill
			User user = Context.getAuthenticatedUser();
			List<Provider> providers = new ArrayList<>(Context.getProviderService().getProvidersByPerson(user.getPerson()));
			
			if (!providers.isEmpty()) {
				activeBill.setCashier(providers.get(0));
				List<CashPoint> cashPoints = cashPointService.getAllCashPoints(false);
				activeBill.setCashPoint(cashPoints.get(0));
				activeBill.addLineItem(billLineItem);
				activeBill.setStatus(BillStatus.PENDING);
				billService.saveBill(activeBill);
			} else {
				log.error("User is not a provider");
			}
			
		}
		catch (Exception ex) {
			log.error("Error sending the bill item: {}", ex.getMessage(), ex);
		}
	}
}
