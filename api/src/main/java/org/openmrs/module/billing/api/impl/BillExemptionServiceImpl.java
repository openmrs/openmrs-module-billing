package org.openmrs.module.billing.api.impl;

import org.openmrs.Concept;
import org.openmrs.module.billing.api.BillExemptionService;
import org.openmrs.module.billing.api.db.BillExemptionDAO;
import org.openmrs.module.billing.api.model.BillExemption;
import org.openmrs.module.billing.api.model.ExemptionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("billing.billingExemptionService")
@Transactional
public class BillExemptionServiceImpl implements BillExemptionService {
	
	private final BillExemptionDAO billExemptionDAO;
	
	public BillExemptionServiceImpl(BillExemptionDAO billExemptionDAO) {
		this.billExemptionDAO = billExemptionDAO;
	}
	
	@Override
	public BillExemption save(BillExemption billExemption) {
		return billExemptionDAO.save(billExemption);
	}
	
	@Override
	public BillExemption getBillingExemptionById(Integer id) {
		return billExemptionDAO.getBillingExemptionById(id);
	}
	
	@Override
	public BillExemption getBillingExemptionByUuid(String uuid) {
		return billExemptionDAO.getBillingExemptionByUuid(uuid);
	}
	
	@Override
	public List<BillExemption> getExemptionsByConcept(Concept concept, ExemptionType itemType, boolean includeRetired) {
		return billExemptionDAO.getExemptionsByConcept(concept, itemType, includeRetired);
	}
	
	@Override
	public List<BillExemption> getExemptionsByItemType(ExemptionType itemType, boolean includeRetired) {
		return billExemptionDAO.getExemptionsByItemType(itemType, includeRetired);
	}
	
}
