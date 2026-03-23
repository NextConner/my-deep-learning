package com.jtcool.common.enums;

public enum BillTypeEnum {
    IN_PURCHASE("IN_PURCHASE", "采购入库"),
    IN_RETURN("IN_RETURN", "退货入库"),
    IN_OTHER("IN_OTHER", "其他入库"),
    OUT_SALES("OUT_SALES", "销售出库"),
    OUT_RETURN("OUT_RETURN", "退货出库"),
    OUT_LOSS("OUT_LOSS", "损耗出库"),
    OUT_OTHER("OUT_OTHER", "其他出库");

    private final String code;
    private final String desc;

    BillTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static BillTypeEnum fromCode(String code) {
        for (BillTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
