package org.openmrs.module.billing.api.model;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.module.billing.api.evaluator.ScriptType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "bill_exemption_rule")
public class BillExemptionRule extends BaseOpenmrsData {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "rule_id")
	private Integer ruleId;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "script_type", nullable = false)
	private ScriptType scriptType;
	
	@Column(name = "script", nullable = false)
	private String script;
	
	@ManyToOne
	@JoinColumn(name = "exemption_id")
	private BillExemption billExemption;
	
	@Override
	public Integer getId() {
		return getRuleId();
	}
	
	@Override
	public void setId(Integer id) {
		setRuleId(id);
	}
	
	public Integer getRuleId() {
		return ruleId;
	}
	
	public ScriptType getScriptType() {
		return scriptType;
	}
	
	public void setScriptType(ScriptType scriptType) {
		this.scriptType = scriptType;
	}
	
	public void setRuleId(Integer ruleId) {
		this.ruleId = ruleId;
	}
	
	public String getScript() {
		return script;
	}
	
	public void setScript(String script) {
		this.script = script;
	}
	
	public BillExemption getBillingExemption() {
		return billExemption;
	}
	
	public void setBillingExemption(BillExemption billExemption) {
		this.billExemption = billExemption;
	}
}
