package org.openmrs.module.billing.advice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Provider;
import org.openmrs.TestOrder;
import org.openmrs.User;
import org.openmrs.api.OrderService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillExemptionService;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.IBillableItemsService;
import org.openmrs.module.billing.api.ICashPointService;
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

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenerateBillFromOrderAdvice implements AfterReturningAdvice {
	
	private static final Log LOG = LogFactory.getLog(GenerateBillFromOrderAdvice.class);
	
	private static final String DISABLE_DRUG_ORDER_BILL_AUTO_CREATION = "billing.disableDrugOrderBillAutoCreation";
	
	OrderService orderService = Context.getOrderService();
	
	BillService billService = Context.getService(BillService.class);
	
	StockManagementService stockService = Context.getService(StockManagementService.class);
	
	ItemPriceService priceService = Context.getService(ItemPriceService.class);
	
	ICashPointService cashPointService = Context.getService(ICashPointService.class);
	
	ExemptionRuleEngine exemptionRuleEngine = Context.getRegisteredComponent("ruleEngine", ExemptionRuleEngine.class);
	
	BillExemptionService billExemptionService = Context.getService(BillExemptionService.class);
	
	/**
	 * This is called immediately an order is saved
	 */
	@Override
	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
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
						// check from the list for all exemptions
						boolean isExempted = checkIfOrderIsExempted(workflowService, order, ExemptionType.COMMODITY);
						BillStatus lineItemStatus = isExempted ? BillStatus.EXEMPTED : BillStatus.PENDING;
						addBillItemToBill(order, patient, cashierUUID, stockItems.get(0), null, (int) drugQuantity,
						    order.getDateActivated(), lineItemStatus);
					}
				} else if (order instanceof TestOrder) {
					TestOrder testOrder = (TestOrder) order;
					BillableService searchTemplate = new BillableService();
					searchTemplate.setConcept(testOrder.getConcept());
					searchTemplate.setServiceStatus(BillableServiceStatus.ENABLED);
					
					IBillableItemsService service = Context.getService(IBillableItemsService.class);
					List<BillableService> searchResult = service.findServices(new BillableServiceSearch(searchTemplate));
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
			LOG.error("Error intercepting order before creation: " + e.getMessage(), e);
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
					billLineItem.setPrice(new BigDecimal(0.0));
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
				List<CashPoint> cashPoints = cashPointService.getAll();
				activeBill.setCashPoint(cashPoints.get(0));
				activeBill.addLineItem(billLineItem);
				activeBill.setStatus(BillStatus.PENDING);
				billService.saveBill(activeBill);
			} else {
				LOG.error("User is not a provider");
			}
			
		}
		catch (Exception ex) {
			LOG.error("Error sending the bill item: " + ex.getMessage(), ex);
		}
	}
}
