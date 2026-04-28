/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.model;

import org.openmrs.BaseOpenmrsObject;

/**
 * Model class that represent the Group Sequence
 */
public class GroupSequence extends BaseOpenmrsObject {
	
	private static final long serialVersionUID = 0L;
	
	private Integer groupSequenceId;
	
	private String group;
	
	private int value;
	
	@Override
	public Integer getId() {
		return groupSequenceId;
	}
	
	@Override
	public void setId(Integer id) {
		groupSequenceId = id;
	}
	
	public String getGroup() {
		return group;
	}
	
	public void setGroup(String group) {
		this.group = group;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
}
