package com.jtcool.framework.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class BusinessMetrics {

    private final Counter orderCreateCounter;
    private final Counter orderCreateFailureCounter;
    private final Counter inventoryUpdateCounter;
    private final Timer aiQueryTimer;

    public BusinessMetrics(MeterRegistry registry) {
        this.orderCreateCounter = Counter.builder("order.create.total")
                .description("订单创建总数")
                .register(registry);

        this.orderCreateFailureCounter = Counter.builder("order.create.failure")
                .description("订单创建失败数")
                .register(registry);

        this.inventoryUpdateCounter = Counter.builder("inventory.update.total")
                .description("库存变动次数")
                .register(registry);

        this.aiQueryTimer = Timer.builder("ai.query.duration")
                .description("AI查询响应时间")
                .register(registry);
    }

    public void incrementOrderCreate() {
        orderCreateCounter.increment();
    }

    public void incrementOrderCreateFailure() {
        orderCreateFailureCounter.increment();
    }

    public void incrementInventoryUpdate() {
        inventoryUpdateCounter.increment();
    }

    public Timer.Sample startAiQueryTimer() {
        return Timer.start();
    }

    public void recordAiQuery(Timer.Sample sample) {
        sample.stop(aiQueryTimer);
    }
}
