/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.billing.api.db.hibernate;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.db.BillingExemptionCategoryDao;
import org.openmrs.module.billing.api.model.BillingExemptionCategory;
import org.openmrs.module.billing.api.model.ExemptionCategoryType;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.util.List;

/**
 * Tests for {@link HibernateBillingExemptionCategoryDao}
 */
public class HibernateBillingExemptionCategoryDaoTest extends BaseModuleContextSensitiveTest {
	
	public static final String BILLING_EXEMPTION_DATASET = TestConstants.BASE_DATASET_DIR + "BillingExemptionTest.xml";
	
	private BillingExemptionCategoryDao dao;
	
	@BeforeEach
	public void before() throws Exception {
		executeDataSet(TestConstants.CORE_DATASET2);
		executeDataSet(BILLING_EXEMPTION_DATASET);
		
		SessionFactory sessionFactory = Context.getRegisteredComponents(SessionFactory.class).get(0);
		dao = new HibernateBillingExemptionCategoryDao(sessionFactory);
	}
	
	/**
	 * @see BillingExemptionCategoryDao#getCategory(Integer)
	 */
	@Test
	public void getCategory_shouldReturnCategoryWhenValidIdIsProvided() {
		BillingExemptionCategory category = dao.getCategory(0);
		
		Assertions.assertNotNull(category);
		Assertions.assertEquals(Integer.valueOf(0), category.getExemptionCategoryId());
		Assertions.assertEquals(ExemptionCategoryType.SERVICE, category.getType());
		Assertions.assertEquals("CHILD_0_5", category.getExemptionKey());
		Assertions.assertEquals("Children 0-5 Service Exemption", category.getName());
		Assertions.assertFalse(category.getRetired());
	}
	
	/**
	 * @see BillingExemptionCategoryDao#getCategory(Integer)
	 */
	@Test
	public void getCategory_shouldReturnNullWhenInvalidIdIsProvided() {
		BillingExemptionCategory category = dao.getCategory(9999);
		
		Assertions.assertNull(category);
	}
	
	/**
	 * @see BillingExemptionCategoryDao#getCategoryByUuid(String)
	 */
	@Test
	public void getCategoryByUuid_shouldReturnCategoryWhenValidUuidIsProvided() {
		BillingExemptionCategory category = dao.getCategoryByUuid("550e8400-e29b-41d4-a716-446655440001");
		
		Assertions.assertNotNull(category);
		Assertions.assertEquals(Integer.valueOf(0), category.getExemptionCategoryId());
		Assertions.assertEquals("CHILD_0_5", category.getExemptionKey());
		Assertions.assertEquals(ExemptionCategoryType.SERVICE, category.getType());
	}
	
	/**
	 * @see BillingExemptionCategoryDao#getCategoryByUuid(String)
	 */
	@Test
	public void getCategoryByUuid_shouldReturnNullWhenInvalidUuidIsProvided() {
		BillingExemptionCategory category = dao.getCategoryByUuid("invalid-uuid-12345");
		
		Assertions.assertNull(category);
	}
	
	/**
	 * @see BillingExemptionCategoryDao#getCategoriesByType(ExemptionCategoryType)
	 */
	@Test
	public void getCategoriesByType_shouldReturnCategoriesOfSpecifiedType() {
		List<BillingExemptionCategory> categories = dao.getCategoriesByType(ExemptionCategoryType.SERVICE);
		
		Assertions.assertNotNull(categories);
		Assertions.assertEquals(3, categories.size());
		
		for (BillingExemptionCategory category : categories) {
			Assertions.assertEquals(ExemptionCategoryType.SERVICE, category.getType());
		}
	}
	
	/**
	 * @see BillingExemptionCategoryDao#getCategoriesByType(ExemptionCategoryType)
	 */
	@Test
	public void getCategoriesByType_shouldReturnEmptyListWhenNoCategoriesOfSpecifiedTypeExist() {
		List<BillingExemptionCategory> categories = dao.getCategoriesByType(ExemptionCategoryType.COMMODITY);
		
		Assertions.assertNotNull(categories);
		Assertions.assertEquals(1, categories.size());
	}
	
	/**
	 * @see BillingExemptionCategoryDao#getCategoryByTypeAndKey(ExemptionCategoryType, String)
	 */
	@Test
	public void getCategoryByTypeAndKey_shouldReturnCategoryWhenValidTypeAndKeyAreProvided() {
		BillingExemptionCategory category = dao.getCategoryByTypeAndKey(ExemptionCategoryType.SERVICE, "CHILD_0_5");
		
		Assertions.assertNotNull(category);
		Assertions.assertEquals(Integer.valueOf(0), category.getExemptionCategoryId());
		Assertions.assertEquals(ExemptionCategoryType.SERVICE, category.getType());
		Assertions.assertEquals("CHILD_0_5", category.getExemptionKey());
	}
	
	/**
	 * @see BillingExemptionCategoryDao#getCategoryByTypeAndKey(ExemptionCategoryType, String)
	 */
	@Test
	public void getCategoryByTypeAndKey_shouldReturnNullWhenNoCategoryMatchesTypeAndKey() {
		BillingExemptionCategory category = dao.getCategoryByTypeAndKey(ExemptionCategoryType.SERVICE, "NONEXISTENT_KEY");
		
		Assertions.assertNull(category);
	}
	
	/**
	 * @see BillingExemptionCategoryDao#getCategoryByTypeAndKey(ExemptionCategoryType, String)
	 */
	@Test
	public void getCategoryByTypeAndKey_shouldReturnNullWhenCategoryIsRetired() {
		BillingExemptionCategory category = dao.getCategoryByTypeAndKey(ExemptionCategoryType.SERVICE, "RETIRED_CATEGORY");
		
		Assertions.assertNull(category);
	}
	
	/**
	 * @see BillingExemptionCategoryDao#save(BillingExemptionCategory)
	 */
	@Test
	public void save_shouldSaveNewCategorySuccessfully() {
		BillingExemptionCategory newCategory = new BillingExemptionCategory();
		newCategory.setType(ExemptionCategoryType.SERVICE);
		newCategory.setExemptionKey("NEW_CATEGORY");
		newCategory.setName("New Test Category");
		newCategory.setDescription("Test category description");
		newCategory.setRetired(false);
		
		BillingExemptionCategory savedCategory = dao.save(newCategory);
		
		Assertions.assertNotNull(savedCategory);
		Assertions.assertNotNull(savedCategory.getExemptionCategoryId());
		
		BillingExemptionCategory retrievedCategory = dao.getCategory(savedCategory.getExemptionCategoryId());
		Assertions.assertNotNull(retrievedCategory);
		Assertions.assertEquals("NEW_CATEGORY", retrievedCategory.getExemptionKey());
		Assertions.assertEquals("New Test Category", retrievedCategory.getName());
	}
	
	/**
	 * @see BillingExemptionCategoryDao#save(BillingExemptionCategory)
	 */
	@Test
	public void save_shouldUpdateExistingCategorySuccessfully() {
		BillingExemptionCategory category = dao.getCategory(0);
		Assertions.assertNotNull(category);
		
		category.setName("Updated Category Name");
		category.setDescription("Updated description");
		
		dao.save(category);
		
		BillingExemptionCategory updatedCategory = dao.getCategory(0);
		Assertions.assertNotNull(updatedCategory);
		Assertions.assertEquals("Updated Category Name", updatedCategory.getName());
		Assertions.assertEquals("Updated description", updatedCategory.getDescription());
	}
	
	/**
	 * @see BillingExemptionCategoryDao#getAll(boolean)
	 */
	@Test
	public void getAll_shouldReturnAllCategoriesWhenIncludeRetiredIsTrue() {
		List<BillingExemptionCategory> categories = dao.getAll(true);
		
		Assertions.assertNotNull(categories);
		Assertions.assertEquals(4, categories.size());
	}
	
	/**
	 * @see BillingExemptionCategoryDao#getAll(boolean)
	 */
	@Test
	public void getAll_shouldReturnOnlyNonRetiredCategoriesWhenIncludeRetiredIsFalse() {
		List<BillingExemptionCategory> categories = dao.getAll(false);
		
		Assertions.assertNotNull(categories);
		Assertions.assertEquals(3, categories.size());
		
		for (BillingExemptionCategory category : categories) {
			Assertions.assertFalse(category.getRetired());
		}
	}
}
