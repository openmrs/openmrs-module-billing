package org.openmrs.module.billing.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Location;
import org.openmrs.module.billing.api.model.CashPoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CashPointTest {

    private CashPoint cashPoint;

    @BeforeEach
    public void setUp() {
        cashPoint = new CashPoint();
    }

    @Test
    public void shouldGetAndSetLocation() {
        Location location = new Location();
        location.setName("Test Location");
        
        cashPoint.setLocation(location);
        
        assertEquals(location, cashPoint.getLocation());
        assertEquals("Test Location", cashPoint.getLocation().getName());
    }

    @Test
    public void shouldGetAndSetId() {
        Integer id = 12345;
        
        cashPoint.setId(id);
        
        assertEquals(id, cashPoint.getId());
    }

    @Test
    public void shouldReturnNullForUnsetLocation() {
        assertNull(cashPoint.getLocation());
    }

    @Test
    public void shouldReturnNullForUnsetId() {
        assertNull(cashPoint.getId());
    }

    @Test
    public void shouldReturnNameForToString() {
        String name = "Cash Point Name";
        
        cashPoint.setName(name);
        
        assertEquals(name, cashPoint.toString());
    }
}
