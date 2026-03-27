package com.jtcool.common.event;

import org.springframework.context.ApplicationEvent;

public class OrderCreatedEvent extends ApplicationEvent {

    private final Long orderId;
    private final String orderNo;

    public OrderCreatedEvent(Object source, Long orderId, String orderNo) {
        super(source);
        this.orderId = orderId;
        this.orderNo = orderNo;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getOrderNo() {
        return orderNo;
    }
}
