/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.impl;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.TestConstants;
import org.openmrs.module.billing.api.PaymentModeAttributeTypeService;
import org.openmrs.module.billing.api.impl.PaymentModeAttributeTypeServiceImpl;
import org.openmrs.module.billing.api.model.PaymentModeAttributeType;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link PaymentModeAttributeTypeServiceImpl}.
 */
public class PaymentModeAttributeTypeServiceImplTest extends BaseModuleContextSensitiveTest {

    private PaymentModeAttributeTypeService service;

    @BeforeEach
    public void setup() {
        service = Context.getService(PaymentModeAttributeTypeService.class);
        executeDataSet(TestConstants.CORE_DATASET2);
        executeDataSet(TestConstants.BASE_DATASET_DIR + "PaymentModeTest.xml");
    }

    /**
     * @see PaymentModeAttributeTypeServiceImpl#getPaymentModeAttributeType(Integer)
     */
    @Test
    public void getPaymentModeAttributeType_shouldReturnAttributeTypeWithSpecifiedId() {
        PaymentModeAttributeType type = service.getPaymentModeAttributeType(0);
        assertNotNull(type);
        assertEquals(0, type.getId());
    }

    /**
     * @see PaymentModeAttributeTypeServiceImpl#getPaymentModeAttributeType(Integer)
     */
    @Test
    public void getPaymentModeAttributeType_shouldReturnNullIfIdIsNull() {
        PaymentModeAttributeType type = service.getPaymentModeAttributeType(null);
        assertNull(type);
    }

    /**
     * @see PaymentModeAttributeTypeServiceImpl#getPaymentModeAttributeTypeByUuid(String)
     */
    @Test
    public void getPaymentModeAttributeTypeByUuid_shouldReturnAttributeTypeWithSpecifiedUuid() {
        PaymentModeAttributeType type = service.getPaymentModeAttributeTypeByUuid("4028814B39B565A20139B56712DD0003");
        assertNotNull(type);
        assertEquals("4028814B39B565A20139B56712DD0003", type.getUuid());
    }

    /**
     * @see PaymentModeAttributeTypeServiceImpl#getPaymentModeAttributeTypeByUuid(String)
     */
    @Test
    public void getPaymentModeAttributeTypeByUuid_shouldReturnNullIfUuidIsNull() {
        PaymentModeAttributeType type = service.getPaymentModeAttributeTypeByUuid(null);
        assertNull(type);
    }

    /**
     * @see PaymentModeAttributeTypeServiceImpl#getPaymentModeAttributeTypeByUuid(String)
     */
    @Test
    public void getPaymentModeAttributeTypeByUuid_shouldReturnNullIfUuidIsEmpty() {
        PaymentModeAttributeType type = service.getPaymentModeAttributeTypeByUuid("");
        assertNull(type);
    }

    /**
     * @see PaymentModeAttributeTypeServiceImpl#getAllPaymentModeAttributeTypes(boolean)
     */
    @Test
    public void getAllPaymentModeAttributeTypes_shouldReturnNonRetiredTypes() {
        List<PaymentModeAttributeType> types = service.getAllPaymentModeAttributeTypes(false);
        assertNotNull(types);
        assertFalse(types.isEmpty());
        assertEquals(3, types.size());
        for (PaymentModeAttributeType type : types) {
            assertFalse(type.getRetired());
        }
    }

    /**
     * @see PaymentModeAttributeTypeServiceImpl#getAllPaymentModeAttributeTypes(boolean)
     */
    @Test
    public void getAllPaymentModeAttributeTypes_shouldReturnAllIncludingRetired() {
        List<PaymentModeAttributeType> types = service.getAllPaymentModeAttributeTypes(true);
        assertNotNull(types);
        assertEquals(3, types.size());
    }

    /**
     * @see PaymentModeAttributeTypeServiceImpl#savePaymentModeAttributeType(PaymentModeAttributeType)
     */
    @Test
    public void savePaymentModeAttributeType_shouldThrowIfNull() {
        assertThrows(IllegalArgumentException.class, () -> service.savePaymentModeAttributeType(null));
    }

    /**
     * @see PaymentModeAttributeTypeServiceImpl#purgePaymentModeAttributeType(PaymentModeAttributeType)
     */
    @Test
    public void purgePaymentModeAttributeType_shouldDeleteAttributeType() {
        PaymentModeAttributeType type = service.getPaymentModeAttributeType(0);
        assertNotNull(type);

        service.purgePaymentModeAttributeType(type);
        Context.flushSession();

        assertNull(service.getPaymentModeAttributeType(0));
    }

    /**
     * @see PaymentModeAttributeTypeServiceImpl#purgePaymentModeAttributeType(PaymentModeAttributeType)
     */
    @Test
    public void purgePaymentModeAttributeType_shouldThrowIfNull() {
        assertThrows(IllegalArgumentException.class, () -> service.purgePaymentModeAttributeType(null));
    }
}
