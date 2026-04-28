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

import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.Concept;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "bill_exemption")
public class BillExemption extends BaseOpenmrsMetadata {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "exemption_id")
	private Integer exemptionId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "concept_id", nullable = false)
	private Concept concept;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "exemption_type", nullable = false)
	private ExemptionType exemptionType;
	
	@OneToMany(mappedBy = "billExemption", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<BillExemptionRule> rules;
	
	@Override
	public Integer getId() {
		return exemptionId;
	}
	
	@Override
	public void setId(Integer exemptionId) {
		this.exemptionId = exemptionId;
	}
	
	public Integer getExemptionId() {
		return exemptionId;
	}
	
	public void setExemptionId(Integer exemptionId) {
		this.exemptionId = exemptionId;
	}
	
	public Concept getConcept() {
		return concept;
	}
	
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	public ExemptionType getExemptionType() {
		return exemptionType;
	}
	
	public void setExemptionType(ExemptionType exemptionType) {
		this.exemptionType = exemptionType;
	}
	
	public List<BillExemptionRule> getRules() {
		return rules;
	}
	
	public void setRules(List<BillExemptionRule> rules) {
		this.rules = rules;
	}
}
