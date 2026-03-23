package com.jtcool.common.enums;

public enum OrderStatusEnum {
    CREATED("CREATED", "已下单"),
    SALES_CONFIRMED("SALES_CONFIRMED", "销售确认"),
    AUDITED("AUDITED", "订单审核通过"),
    WAREHOUSE_CONFIRMED("WAREHOUSE_CONFIRMED", "仓库确认"),
    OUT_REGISTERED("OUT_REGISTERED", "登记出库"),
    SHIPPED("SHIPPED", "确认发货"),
    RECEIVED("RECEIVED", "客户签收"),
    REJECTED("REJECTED", "已拒绝"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String desc;

    OrderStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static OrderStatusEnum fromCode(String code) {
        for (OrderStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
