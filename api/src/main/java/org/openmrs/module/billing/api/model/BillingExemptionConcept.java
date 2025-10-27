package org.openmrs.module.billing.api.model;

import lombok.Getter;
import lombok.Setter;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Setter
@Getter
@Entity
@Table(name = "billing_exemption_concept")
public class BillingExemptionConcept extends BaseOpenmrsData {
	
	@Id
	@Column(name = "exemption_concept_id", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer exemptionConceptId;
	
	@ManyToOne
	@JoinColumn(name = "exemption_category_id", nullable = false)
	private BillingExemptionCategory category;
	
	@ManyToOne
	@JoinColumn(name = "concept_id", nullable = false)
	private Concept concept;
	
	@Override
	public Integer getId() {
		return exemptionConceptId;
	}
	
	@Override
	public void setId(Integer id) {
		this.exemptionConceptId = id;
	}
}
