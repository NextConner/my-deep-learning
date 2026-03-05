package com.example.aiagent.context;

/**
 * 租户上下文
 * 使用 ThreadLocal 存储当前线程的租户ID，实现线程安全的租户隔离
 */
public class TenantContext {
    
    /** 使用 ThreadLocal 存储租户ID，线程安全 */
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
    
    /**
     * 设置当前线程的租户ID
     * @param tenantId 租户ID
     */
    public static void setTenantId(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }
    
    /**
     * 获取当前线程的租户ID
     * @return 租户ID，如果没有设置则返回null
     */
    public static String getTenantId() {
        return CURRENT_TENANT.get();
    }
    
    /**
     * 清理当前线程的租户上下文
     * 必须在请求处理完成后调用，防止内存泄漏
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
