// 我的代码现在：
// 所有错误都重试1次（简单粗暴）

// 更高级的做法：根据错误类型决定是否重试
public enum RetryableError {
    TIMEOUT,                    // ✅ 可重试
    NETWORK_ERROR,              // ✅ 可重试（网络抖动）
    RATE_LIMIT_429,             // ✅ 可重试（加延迟）
    LLM_NOT_FOUND,              // ❌ 不可重试
    INVALID_PARAMETERS,         // ❌ 不可重试
    AUTHENTICATION_FAILED,      // ❌ 不可重试
}