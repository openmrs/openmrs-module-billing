package org.openmrs.module.billing.api.model;

import lombok.Getter;
import lombok.Setter;
import org.openmrs.BaseOpenmrsMetadata;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "billing_exemption_category")
public class BillingExemptionCategory extends BaseOpenmrsMetadata {
	
	@Id
	@Column(name = "exemption_category_id", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer exemptionCategoryId;
	
	@Column(name = "category_type")
	@Enumerated(EnumType.STRING)
	private ExemptionCategoryType type;
	
	@Column(name = "exemption_key", nullable = false)
	private String exemptionKey;
	
	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<BillingExemptionConcept> exemptedConcepts = new HashSet<>();
	
	@Override
	public Integer getId() {
		return exemptionCategoryId;
	}
	
	@Override
	public void setId(Integer id) {
		this.exemptionCategoryId = id;
	}
	
	public void addExemptedConcept(BillingExemptionConcept concept) {
		exemptedConcepts.add(concept);
		concept.setCategory(this);
	}
	
	public void removeExemptedConcept(BillingExemptionConcept concept) {
		exemptedConcepts.remove(concept);
		concept.setCategory(null);
	}
	
}
