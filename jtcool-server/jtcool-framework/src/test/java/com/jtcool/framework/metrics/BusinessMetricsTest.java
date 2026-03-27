package com.jtcool.framework.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessMetricsTest {

    private BusinessMetrics metrics;
    private MeterRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = new BusinessMetrics(registry);
    }

    @Test
    void testIncrementOrderCreate() {
        metrics.incrementOrderCreate();
        assertEquals(1.0, registry.counter("order.create.total").count());
    }

    @Test
    void testIncrementOrderCreateFailure() {
        metrics.incrementOrderCreateFailure();
        assertEquals(1.0, registry.counter("order.create.failure").count());
    }

    @Test
    void testIncrementInventoryUpdate() {
        metrics.incrementInventoryUpdate();
        assertEquals(1.0, registry.counter("inventory.update.total").count());
    }

    @Test
    void testAiQueryTimer() {
        var sample = metrics.startAiQueryTimer();
        assertNotNull(sample);
        metrics.recordAiQuery(sample);
        assertTrue(registry.timer("ai.query.duration").count() > 0);
    }
}
