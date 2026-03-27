package com.jtcool.common.utils;

/**
 * 敏感数据脱敏工具
 */
public class DataMaskUtils {

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 18) return idCard;
        return idCard.substring(0, 6) + "********" + idCard.substring(14);
    }

    public static String maskBankCard(String bankCard) {
        if (bankCard == null || bankCard.length() < 16) return bankCard;
        return bankCard.substring(0, 4) + "********" + bankCard.substring(12);
    }
}
