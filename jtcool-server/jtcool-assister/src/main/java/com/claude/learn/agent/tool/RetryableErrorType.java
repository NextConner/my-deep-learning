package com.claude.learn.agent.tool;

/**
 * 可重试的错误类型分类
 *
 * 根据错误类型决定是否重试和重试延迟
 *
 * 设计思想：
 * - RETRYABLE_WITH_BACKOFF：网络/系统级错误，适合等待后重试
 * - RETRYABLE_IMMEDIATE：超时/临时故障，立即重试通常有效
 * - NOT_RETRYABLE：业务/配置错误，重试无效
 *
 * 面试要点：
 * - 为什么区分？减少无意义的重试，提高成功率
 * - 如何判断错误类型？通过异常类型和错误码
 * - 是否需要指数退避？只在RETRYABLE_WITH_BACKOFF中使用
 */
public enum RetryableErrorType {
    /**
     * 可重试的临时错误 - 立即重试有效
     * 例如：LLM超时、网络连接超时
     */
    RETRYABLE_IMMEDIATE("timeout", "connection_reset", true, 0),

    /**
     * 可重试的限流错误 - 需要等待后重试（指数退避）
     * 例如：HTTP 429 Too Many Requests、数据库连接池满
     */
    RETRYABLE_WITH_BACKOFF("rate_limit", "429", true, 1000),

    /**
     * 不可重试的错误 - 重试无效
     * 例如：认证失败、参数错误、业务逻辑错误
     */
    NOT_RETRYABLE("auth_failed", "invalid_param", false, 0);

    private final String[] errorPatterns;
    private final boolean retryable;
    private final long backoffDelayMs;

    RetryableErrorType(String pattern1, String pattern2, boolean retryable, long backoffDelayMs) {
        this.errorPatterns = new String[]{pattern1, pattern2};
        this.retryable = retryable;
        this.backoffDelayMs = backoffDelayMs;
    }

    /**
     * 根据异常信息判断错误类型
     */
    public static RetryableErrorType classify(Exception e) {
        String message = e.getClass().getSimpleName() + ":" + e.getMessage();
        String lowerMessage = message.toLowerCase();

        // 判断超时错误
        if (e instanceof java.util.concurrent.TimeoutException ||
            e instanceof java.net.SocketTimeoutException ||
            lowerMessage.contains("timeout")) {
            return RETRYABLE_IMMEDIATE;
        }

        // 判断限流错误
        if (lowerMessage.contains("429") || lowerMessage.contains("rate_limit") ||
            lowerMessage.contains("too many requests")) {
            return RETRYABLE_WITH_BACKOFF;
        }

        // 判断网络错误
        if (e instanceof java.net.ConnectException ||
            e instanceof java.net.UnknownHostException ||
            lowerMessage.contains("connection")) {
            return RETRYABLE_IMMEDIATE;
        }

        // 判断不可重试错误
        if (lowerMessage.contains("auth") || lowerMessage.contains("unauthorized") ||
            lowerMessage.contains("invalid") || lowerMessage.contains("not found")) {
            return NOT_RETRYABLE;
        }

        // 其他错误默认不重试
        return NOT_RETRYABLE;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public long getBackoffDelayMs() {
        return backoffDelayMs;
    }
}
