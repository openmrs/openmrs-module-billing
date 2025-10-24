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
import org.openmrs.module.billing.api.BillingExemptionService;
import org.openmrs.module.billing.api.IBillService;
import org.openmrs.module.billing.api.IBillableItemsService;
import org.openmrs.module.billing.api.ICashPointService;
import org.openmrs.module.billing.api.ItemPriceService;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.BillableServiceStatus;
import org.openmrs.module.billing.api.model.BillingExemptionCategory;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.openmrs.module.billing.api.model.ExemptionCategoryType;
import org.openmrs.module.billing.api.search.BillableServiceSearch;
import org.openmrs.module.stockmanagement.api.StockManagementService;
import org.openmrs.module.stockmanagement.api.model.StockItem;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GenerateBillFromOrderAdvice implements AfterReturningAdvice {
	
	private static final Log LOG = LogFactory.getLog(GenerateBillFromOrderAdvice.class);
	
	OrderService orderService = Context.getOrderService();
	
	IBillService billService = Context.getService(IBillService.class);
	
	StockManagementService stockService = Context.getService(StockManagementService.class);
	
	ItemPriceService priceService = Context.getService(ItemPriceService.class);
	
	ICashPointService cashPointService = Context.getService(ICashPointService.class);
	
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
					DrugOrder drugOrder = (DrugOrder) order;
					Integer drugID = drugOrder.getDrug() != null ? drugOrder.getDrug().getDrugId() : 0;
					double drugQuantity = drugOrder.getQuantity() != null ? drugOrder.getQuantity() : 0.0;
					List<StockItem> stockItems = stockService.getStockItemByDrug(drugID);
					
					if (!stockItems.isEmpty()) {
						// check from the list for all exemptions
						boolean isExempted = checkIfOrderIsExempted(workflowService, order, ExemptionCategoryType.COMMODITY);
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
						boolean isExempted = checkIfOrderIsExempted(workflowService, order, ExemptionCategoryType.SERVICE);
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
	        ExemptionCategoryType exemptionType) {

        BillingExemptionService exemptionService = Context.getService(BillingExemptionService.class);

        if (order == null) {
            return false;
        }
        Integer conceptId = order.getConcept().getConceptId();

        Set<Integer> allExemptions = exemptionService.getExemptedConceptIds(exemptionType, "all");
        if (allExemptions.contains(conceptId)) {
            return true;
        }

        List<PatientProgram> programs = workflowService.getPatientPrograms(order.getPatient(), null, null, null, new Date(),
                null, false);

        for (PatientProgram patientProgram : programs) {
            if (patientProgram.getActive()) {
                String programKey = "program:" + patientProgram.getProgram().getName();
                Set<Integer> programExemptions = exemptionService.getExemptedConceptIds(exemptionType, programKey);
                if (programExemptions.contains(conceptId)) {
                    return true;
                }
            }
        }

        List<BillingExemptionCategory> ageCategories = exemptionService.getCategoriesByType(exemptionType)
                .stream()
                .filter(cat -> cat.getExemptionKey().startsWith("age"))
                .collect(Collectors.toList());

        Integer patientAge = order.getPatient().getAge();

        for (BillingExemptionCategory category : ageCategories) {
            if (matchesAgeCondition(patientAge, category.getExemptionKey())) {
                Set<Integer> ageExemptions = exemptionService.getExemptedConceptIds(exemptionType, category.getExemptionKey());
                if (ageExemptions.contains(conceptId)) {
                    return true;
                }
            }
        }
        return false;
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
				billService.save(activeBill);
			} else {
				LOG.error("User is not a provider");
			}
			
		}
		catch (Exception ex) {
			LOG.error("Error sending the bill item: " + ex.getMessage(), ex);
		}
	}

    private boolean matchesAgeCondition(Integer patientAge, String exemptionKey) {
        if (exemptionKey.startsWith("age<")) {
            int threshold = Integer.parseInt(exemptionKey.substring(4));
            return patientAge < threshold;
        } else if (exemptionKey.startsWith("age>")) {
            int threshold = Integer.parseInt(exemptionKey.substring(4));
            return patientAge > threshold;
        }
        return false;
    }
}
