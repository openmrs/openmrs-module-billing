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
import org.openmrs.module.billing.api.SequentialReceiptNumberGenerator;

/**
 * Model class that represents the settings for the {@link SequentialReceiptNumberGenerator}.
 */
public class SequentialReceiptNumberGeneratorModel extends BaseOpenmrsObject {
	
	private static final long serialVersionUID = 0L;
	
	public static final String DEFAULT_SEPARATOR = "-";
	
	public static final String DEFAULT_CASHIER_PREFIX = "P";
	
	public static final String DEFAULT_CASH_POINT_PREFIX = "CP";
	
	public static final int DEFAULT_SEQUENCE_PADDING = 4;
	
	private Integer id;
	
	private SequentialReceiptNumberGenerator.GroupingType groupingType;
	
	private SequentialReceiptNumberGenerator.SequenceType sequenceType;
	
	private String separator;
	
	private String cashierPrefix;
	
	private String cashPointPrefix;
	
	private int sequencePadding;
	
	private boolean includeCheckDigit;
	
	public SequentialReceiptNumberGeneratorModel() {
		groupingType = SequentialReceiptNumberGenerator.GroupingType.NONE;
		sequenceType = SequentialReceiptNumberGenerator.SequenceType.COUNTER;
		separator = DEFAULT_SEPARATOR;
		cashierPrefix = DEFAULT_CASHIER_PREFIX;
		cashPointPrefix = DEFAULT_CASH_POINT_PREFIX;
		sequencePadding = DEFAULT_SEQUENCE_PADDING;
		includeCheckDigit = true;
	}
	
	@Override
	public Integer getId() {
		return this.id;
	}
	
	@Override
	public void setId(Integer id) {
		this.id = id;
	}
	
	public SequentialReceiptNumberGenerator.GroupingType getGroupingType() {
		return groupingType;
	}
	
	public void setGroupingType(SequentialReceiptNumberGenerator.GroupingType groupingType) {
		this.groupingType = groupingType;
	}
	
	public SequentialReceiptNumberGenerator.SequenceType getSequenceType() {
		return sequenceType;
	}
	
	public void setSequenceType(SequentialReceiptNumberGenerator.SequenceType sequenceType) {
		this.sequenceType = sequenceType;
	}
	
	public String getSeparator() {
		return separator;
	}
	
	public void setSeparator(String separator) {
		this.separator = separator;
		
		if (this.separator == null) {
			this.separator = "";
		}
	}
	
	public String getCashierPrefix() {
		return cashierPrefix;
	}
	
	public void setCashierPrefix(String cashierPrefix) {
		this.cashierPrefix = cashierPrefix;
	}
	
	public String getCashPointPrefix() {
		return cashPointPrefix;
	}
	
	public void setCashPointPrefix(String cashPointPrefix) {
		this.cashPointPrefix = cashPointPrefix;
	}
	
	public int getSequencePadding() {
		return sequencePadding;
	}
	
	public void setSequencePadding(int sequencePadding) {
		this.sequencePadding = sequencePadding;
		
		if (this.sequencePadding <= 0) {
			this.sequencePadding = 1;
		}
	}
	
	public boolean getIncludeCheckDigit() {
		return includeCheckDigit;
	}
	
	public void setIncludeCheckDigit(boolean includeCheckDigit) {
		this.includeCheckDigit = includeCheckDigit;
	}
}
