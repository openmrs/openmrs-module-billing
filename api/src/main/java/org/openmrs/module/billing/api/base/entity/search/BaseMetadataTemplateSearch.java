/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.base.entity.search;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.openmrs.OpenmrsMetadata;

/**
 * Base template search class for {@link org.openmrs.OpenmrsMetadata} models.
 *
 * @param <T> The model class.
 */
public class BaseMetadataTemplateSearch<T extends OpenmrsMetadata> extends BaseAuditableTemplateSearch<T> {
	
	public static final long serialVersionUID = 0L;
	
	private StringComparisonType nameComparisonType;
	
	private StringComparisonType descriptionComparisonType;
	
	private StringComparisonType retireReasonComparisonType;
	
	private DateComparisonType dateRetiredComparisonType;
	
	private Boolean includeRetired;
	
	public BaseMetadataTemplateSearch(T template) {
		this(template, StringComparisonType.EQUAL, null);
	}
	
	public BaseMetadataTemplateSearch(T template, Boolean includeRetired) {
		this(template, StringComparisonType.EQUAL, includeRetired);
	}
	
	public BaseMetadataTemplateSearch(T template, StringComparisonType nameComparisonType, Boolean includeRetired) {
		super(template);
		
		this.nameComparisonType = nameComparisonType;
		this.includeRetired = includeRetired;
		this.descriptionComparisonType = StringComparisonType.EQUAL;
		this.retireReasonComparisonType = StringComparisonType.EQUAL;
		this.dateRetiredComparisonType = DateComparisonType.EQUAL;
	}
	
	public StringComparisonType getNameComparisonType() {
		return nameComparisonType;
	}
	
	public void setNameComparisonType(StringComparisonType nameComparisonType) {
		this.nameComparisonType = nameComparisonType;
	}
	
	public StringComparisonType getDescriptionComparisonType() {
		return descriptionComparisonType;
	}
	
	public void setDescriptionComparisonType(StringComparisonType descriptionComparisonType) {
		this.descriptionComparisonType = descriptionComparisonType;
	}
	
	public StringComparisonType getRetireReasonComparisonType() {
		return retireReasonComparisonType;
	}
	
	public void setRetireReasonComparisonType(StringComparisonType retireReasonComparisonType) {
		this.retireReasonComparisonType = retireReasonComparisonType;
	}
	
	public DateComparisonType getDateRetiredComparisonType() {
		return dateRetiredComparisonType;
	}
	
	public void setDateRetiredComparisonType(DateComparisonType dateRetiredComparisonType) {
		this.dateRetiredComparisonType = dateRetiredComparisonType;
	}
	
	public Boolean getIncludeRetired() {
		return includeRetired;
	}
	
	public void setIncludeRetired(Boolean includeRetired) {
		this.includeRetired = includeRetired;
	}
	
	@Override
	public void updateCriteria(Criteria criteria) {
		super.updateCriteria(criteria);
		
		T t = getTemplate();
		if (t.getName() != null) {
			criteria.add(createCriterion("name", t.getName(), nameComparisonType));
		}
		if (t.getDescription() != null) {
			criteria.add(createCriterion("description", t.getName(), nameComparisonType));
		}
		
		if (includeRetired != null) {
			if (!includeRetired) {
				criteria.add(Restrictions.eq("retired", false));
			}
		} else if (t.getRetired() != null) {
			criteria.add(Restrictions.eq("retired", t.getRetired()));
		}
		
		if (t.getRetiredBy() != null) {
			criteria.add(Restrictions.eq("retiredBy", t.getRetiredBy()));
		}
		if (t.getDateRetired() != null) {
			criteria.add(createCriterion("dateRetired", t.getDateRetired(), dateRetiredComparisonType));
		}
		if (t.getRetireReason() != null) {
			criteria.add(createCriterion("retireReason", t.getRetireReason(), retireReasonComparisonType));
		}
	}
}
