package com.claude.learn.service.cache;

public interface TokenCacheStrategy {

    /**
     * 获取用户今日已用 Token 数
     */
    int getUsed(String username);

    /**
     * 增加用户 Token 消耗
     */
    void increment(String username, int tokens);

    /**
     * 获取用户每日限额
     */
    int getLimit(String username);

    /**
     * 设置用户每日限额
     */
    void setLimit(String username, int limit);

    /**
     * 重置用户今日消耗（每日定时重置）
     */
    void reset(String username);

}
