/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.billing.api.SequentialReceiptNumberGeneratorService;
import org.openmrs.module.billing.api.base.entity.db.hibernate.BaseHibernateRepository;
import org.openmrs.module.billing.api.model.GroupSequence;
import org.openmrs.module.billing.api.model.SequentialReceiptNumberGeneratorModel;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data service implementation class for {@link SequentialReceiptNumberGeneratorModel}s.
 */
@Transactional
public class SequentialReceiptNumberGeneratorServiceImpl extends BaseOpenmrsService implements SequentialReceiptNumberGeneratorService {
	
	private BaseHibernateRepository repository;
	
	public void setRepository(BaseHibernateRepository repository) {
		this.repository = repository;
	}
	
	@Override
	@Transactional(readOnly = true)
	public SequentialReceiptNumberGeneratorModel getOnly() {
		List<SequentialReceiptNumberGeneratorModel> records = repository.select(SequentialReceiptNumberGeneratorModel.class);
		
		if (!records.isEmpty()) {
			return records.get(0);
		} else {
			return new SequentialReceiptNumberGeneratorModel();
		}
	}
	
	@Override
	@Transactional
	public SequentialReceiptNumberGeneratorModel save(SequentialReceiptNumberGeneratorModel model) {
		if (model == null) {
			throw new IllegalArgumentException("The model to save must be defined.");
		}
		return repository.save(model);
	}
	
	@Override
	@Transactional
	public int reserveNextSequence(String group) {
		// Get the sequence
		GroupSequence sequence = getSequence(group);
		if (sequence == null) {
			// Sequence not found so create it
			sequence = new GroupSequence();
			sequence.setGroup(group);
			sequence.setValue(1);
		} else {
			// Increment the value
			sequence.setValue(sequence.getValue() + 1);
		}
		
		// Store the sequence and save the updated or new sequence
		int result = sequence.getValue();
		saveSequence(sequence);
		
		return result;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<GroupSequence> getSequences() {
		return repository.select(GroupSequence.class);
	}
	
	@Override
	@Transactional(readOnly = true)
	public GroupSequence getSequence(String group) {
		if (group == null) {
			throw new IllegalArgumentException("The group must be defined.");
		}
		
		Criteria criteria = repository.createCriteria(GroupSequence.class);
		criteria.add(Restrictions.eq("group", group));
		
		return repository.selectSingle(GroupSequence.class, criteria);
	}
	
	@Override
	@Transactional
	public GroupSequence saveSequence(GroupSequence sequence) {
		if (sequence == null) {
			throw new IllegalArgumentException("The sequence to save must be defined.");
		}
		
		return repository.save(sequence);
	}
	
	@Override
	@Transactional
	public void purgeSequence(GroupSequence sequence) {
		if (sequence == null) {
			throw new IllegalArgumentException("The sequence to purge must be defined.");
		}
		
		repository.delete(sequence);
	}
	
}
