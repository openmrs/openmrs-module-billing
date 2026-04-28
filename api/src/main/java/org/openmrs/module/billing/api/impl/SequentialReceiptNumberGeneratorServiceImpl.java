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
import org.openmrs.module.billing.api.ISequentialReceiptNumberGeneratorService;
import org.openmrs.module.billing.api.base.entity.impl.BaseObjectDataServiceImpl;
import org.openmrs.module.billing.api.model.GroupSequence;
import org.openmrs.module.billing.api.model.SequentialReceiptNumberGeneratorModel;
import org.openmrs.module.billing.api.security.BasicEntityAuthorizationPrivileges;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data service implementation class for {@link SequentialReceiptNumberGeneratorModel}s.
 */
@Transactional
public class SequentialReceiptNumberGeneratorServiceImpl extends BaseObjectDataServiceImpl<SequentialReceiptNumberGeneratorModel, BasicEntityAuthorizationPrivileges> implements ISequentialReceiptNumberGeneratorService {
	
	@Override
	protected BasicEntityAuthorizationPrivileges getPrivileges() {
		// No authorization required
		return null;
	}
	
	@Override
	protected void validate(SequentialReceiptNumberGeneratorModel entity) {
	}
	
	@Override
	@Transactional(readOnly = true)
	public SequentialReceiptNumberGeneratorModel getOnly() {
		List<SequentialReceiptNumberGeneratorModel> records = getAll();
		
		if (!records.isEmpty()) {
			return records.get(0);
		} else {
			return new SequentialReceiptNumberGeneratorModel();
		}
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
		return getRepository().select(GroupSequence.class);
	}
	
	@Override
	@Transactional(readOnly = true)
	public GroupSequence getSequence(String group) {
		if (group == null) {
			throw new IllegalArgumentException("The group must be defined.");
		}
		
		Criteria criteria = getRepository().createCriteria(GroupSequence.class);
		criteria.add(Restrictions.eq("group", group));
		
		return getRepository().selectSingle(GroupSequence.class, criteria);
	}
	
	@Override
	@Transactional
	public GroupSequence saveSequence(GroupSequence sequence) {
		if (sequence == null) {
			throw new NullPointerException("The sequence to save must be defined.");
		}
		
		return getRepository().save(sequence);
	}
	
	@Override
	@Transactional
	public void purgeSequence(GroupSequence sequence) {
		if (sequence == null) {
			throw new NullPointerException("The sequence to purge must be defined.");
		}
		
		getRepository().delete(sequence);
	}
}
