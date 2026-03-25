package com.jtcool.oms.domain.enums;

import java.util.Arrays;

/**
 * 订单状态枚举
 */
public enum OrderStatusEnum {
    PLACED("PLACED", "已下单", new String[]{"SALES_CONFIRMED"}),
    SALES_CONFIRMED("SALES_CONFIRMED", "销售确认", new String[]{"ORDER_REVIEWED"}),
    ORDER_REVIEWED("ORDER_REVIEWED", "订单审核", new String[]{"WAREHOUSE_CONFIRMED"}),
    WAREHOUSE_CONFIRMED("WAREHOUSE_CONFIRMED", "仓库确认", new String[]{"OUTBOUND_REGISTERED"}),
    OUTBOUND_REGISTERED("OUTBOUND_REGISTERED", "登记出库", new String[]{"SHIPMENT_CONFIRMED"}),
    SHIPMENT_CONFIRMED("SHIPMENT_CONFIRMED", "确认发货", new String[]{"CUSTOMER_RECEIVED"}),
    CUSTOMER_RECEIVED("CUSTOMER_RECEIVED", "客户签收", new String[]{});

    private final String code;
    private final String description;
    private final String[] allowedNextStates;

    OrderStatusEnum(String code, String description, String[] allowedNextStates) {
        this.code = code;
        this.description = description;
        this.allowedNextStates = allowedNextStates;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean canTransitionTo(String nextStatus) {
        return Arrays.asList(allowedNextStates).contains(nextStatus);
    }

    public static OrderStatusEnum fromCode(String code) {
        for (OrderStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status: " + code);
    }
}
