package com.jtcool.wms.listener;

import com.jtcool.common.event.OrderCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrderEventListenerTest {

    @InjectMocks
    private OrderEventListener listener;

    @Test
    void testHandleOrderCreated() {
        OrderCreatedEvent event = new OrderCreatedEvent(this, 1L, "ORD001");

        assertDoesNotThrow(() -> listener.handleOrderCreated(event));
        assertEquals(1L, event.getOrderId());
        assertEquals("ORD001", event.getOrderNo());
    }
}
