package org.openmrs.module.billing.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.module.billing.api.SequentialReceiptNumberGenerator;
import org.openmrs.module.billing.api.SequentialReceiptNumberGenerator.GroupingType;
import org.openmrs.module.billing.api.SequentialReceiptNumberGenerator.SequenceType;
import org.openmrs.module.billing.api.model.SequentialReceiptNumberGeneratorModel;

import static org.junit.jupiter.api.Assertions.*;

class SequentialReceiptNumberGeneratorModelTest {
    
    private SequentialReceiptNumberGeneratorModel model;
    
    @BeforeEach
    void setUp() {
        model = new SequentialReceiptNumberGeneratorModel();
    }
    
    @Test
    void testDefaultConstructorValues() {
        assertEquals(GroupingType.NONE, model.getGroupingType());
        assertEquals(SequenceType.COUNTER, model.getSequenceType());
        assertEquals(SequentialReceiptNumberGeneratorModel.DEFAULT_SEPARATOR, model.getSeparator());
        assertEquals(SequentialReceiptNumberGeneratorModel.DEFAULT_CASHIER_PREFIX, model.getCashierPrefix());
        assertEquals(SequentialReceiptNumberGeneratorModel.DEFAULT_CASH_POINT_PREFIX, model.getCashPointPrefix());
        assertEquals(SequentialReceiptNumberGeneratorModel.DEFAULT_SEQUENCE_PADDING, model.getSequencePadding());
        assertTrue(model.getIncludeCheckDigit());
    }
    
    @Test
    void testSetAndGetId() {
        Integer id = 123;
        model.setId(id);
        assertEquals(id, model.getId());
    }
    
    @Test
    void testSetAndGetGroupingType() {
        GroupingType groupingType = GroupingType.CASHIER;
        model.setGroupingType(groupingType);
        assertEquals(groupingType, model.getGroupingType());
    }
    
    @Test
    void testSetAndGetSequenceType() {
        SequenceType sequenceType = SequenceType.DATE_COUNTER;
        model.setSequenceType(sequenceType);
        assertEquals(sequenceType, model.getSequenceType());
    }
    
    @Test
    void testSetAndGetSeparator() {
        String separator = "-";
        model.setSeparator(separator);
        assertEquals(separator, model.getSeparator());
        
        model.setSeparator(null);
        assertEquals("", model.getSeparator());
    }
    
    @Test
    void testSetAndGetCashierPrefix() {
        String cashierPrefix = "P";
        model.setCashierPrefix(cashierPrefix);
        assertEquals(cashierPrefix, model.getCashierPrefix());
    }
    
    @Test
    void testSetAndGetCashPointPrefix() {
        String cashPointPrefix = "CP";
        model.setCashPointPrefix(cashPointPrefix);
        assertEquals(cashPointPrefix, model.getCashPointPrefix());
    }
    
    @Test
    void testSetAndGetSequencePadding() {
        int padding = 5;
        model.setSequencePadding(padding);
        assertEquals(padding, model.getSequencePadding());
        
        model.setSequencePadding(0);
        assertEquals(1, model.getSequencePadding());
    }
    
    @Test
    void testSetAndGetIncludeCheckDigit() {
        boolean includeCheckDigit = false;
        model.setIncludeCheckDigit(includeCheckDigit);
        assertFalse(model.getIncludeCheckDigit());
    }
}
