package com.jtcool.wms.listener;

import com.jtcool.common.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    @Async
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("收到订单创建事件: orderId={}, orderNo={}", event.getOrderId(), event.getOrderNo());
        // TODO: 处理库存扣减逻辑
    }
}
