# V2 & V3 优化说明

## 目录
1. V2 优化：根据工具类型和错误类型调整策略
2. V3 优化：基于执行统计的动态调整
3. 新增类和接口
4. 配置示例

---

## V2 优化详解

### 1. 根据工具类型调整重试策略

**问题场景**：
```
不同工具有不同的特性：
- searchPolicy（查询）：可重试，查不到数据是业务逻辑
- sendEmail（发送邮件）：不能重试，可能重复发送
- getWeather（外部API）：可重试，API可能暂时故障
```

**V1 问题**：
```
所有工具使用相同的重试策略 (retryTimes=1, timeout=15s)
- 查询工具可以用2次重试提高成功率
- 但发送邮件再试一次可能导致重复发送
```

**V2 解决方案**：

通过 `ToolRetryStrategy` 类为每个工具定制参数：

```java
// ToolRetryStrategy.forTool(toolName)
switch (toolName) {
    case "searchPolicy" -> new ToolRetryStrategy("searchPolicy", 2, 15000L, true);
    case "sendEmail" -> new ToolRetryStrategy("sendEmail", 0, 10000L, false);
    // ...
}
```

**关键参数**：
- `maxRetryTimes`：该工具最多重试次数
- `timeoutMs`：该工具的超时时间
- `allowDuplicateExecution`：是否允许重复执行（有副作用的操作应为 false）

---

### 2. 根据错误类型调整重试决策

**错误分类** - `RetryableErrorType` 枚举：

```java
public enum RetryableErrorType {
    /**
     * 临时性错误 - 立即重试有效
     * 例如：超时、连接重置
     */
    RETRYABLE_IMMEDIATE("timeout", "connection_reset", true, 0),

    /**
     * 限流错误 - 需要等待后重试（指数退避）
     * 例如：HTTP 429、资源池耗尽
     */
    RETRYABLE_WITH_BACKOFF("rate_limit", "429", true, 1000),

    /**
     * 不可重试的错误
     * 例如：认证失败、参数错误
     */
    NOT_RETRYABLE("auth_failed", "invalid_param", false, 0);
}
```

**工作流程**：

```java
try {
    result = executeWithTimeout(toolName, arguments, toolTimeout);
} catch (Exception e) {
    RetryableErrorType errorType = RetryableErrorType.classify(e);
    
    if (!errorType.isRetryable()) {
        // 不可重试错误，直接返回失败
        return ToolExecutionResult.failure(...);
    }
    
    if (errorType == RetryableErrorType.RETRYABLE_WITH_BACKOFF) {
        // 限流错误，应用指数退避
        long backoffMs = 1000 * Math.pow(2, attempt);
        Thread.sleep(backoffMs);
    }
    
    // 继续重试
}
```

**优势**：
- ✅ 不浪费时间重试不可重试的错误
- ✅ 对限流错误应用指数退避
- ✅ 提高整体成功率和系统效率

---

### 3. 方法缓存优化

**问题**：每次调用工具都要用反射遍历所有方法

**V2 解决**：

```java
private final Map<String, Method> methodCache = new ConcurrentHashMap<>();

private void initMethodCache() {
    Method[] methods = agentTools.getClass().getMethods();
    for (Method method : methods) {
        if (method.isAnnotationPresent(Tool.class)) {
            methodCache.put(method.getName(), method);
        }
    }
}

private String executeTool(String toolName, String arguments) throws Exception {
    Method method = methodCache.get(toolName);  // ← 快速查找
    if (method == null) {
        // cache miss，重新查找并缓存
    }
}
```

**性能提升**：
- O(n) 查询 → O(1) 查询（缓存命中）
- 对于 10 个工具，每次避免 10 次方法遍历

---

## V3 优化详解

### 执行统计 - `ToolExecutionStats`

**收集的指标**：
```
- totalExecutions：总执行次数
- successCount：成功次数
- failureCount：失败次数
- timeoutCount：超时次数
- totalLatencyMs：总耗时
- maxLatencyMs：最大耗时
- minLatencyMs：最小耗时
```

**计算的指标**：
```java
double successRate = success / total * 100;
double failureRate = (failure + timeout) / total * 100;
double avgLatency = totalLatency / total;
```

---

### 自动告警 - `ToolExecutionMonitor`

**告警条件**：

```java
public boolean needsAlert() {
    // 成功率 < 95% 且 至少执行了10次
    return getSuccessRate() < 95.0 && totalExecutions >= 10;
}
```

**告警示例**：
```
WARN: Tool 'searchPolicy' has low success rate: 89.5% (failures: 1, timeouts: 10)
```

---

### 自动建议超时调整

**场景**：某工具的最大耗时接近超时时间，导致频繁超时

**自动建议**：

```java
public boolean shouldIncreaseTimeout(long currentTimeoutMs) {
    // 如果最大耗时 > 当前超时的80%，且超时次数 > 5
    return maxLatencyMs > currentTimeoutMs * 0.8 && timeoutCount > 5;
}

public long getRecommendedTimeoutMs(long currentTimeoutMs) {
    if (shouldIncreaseTimeout(currentTimeoutMs)) {
        return (long) (currentTimeoutMs * 1.2);  // 增加20%
    }
    return currentTimeoutMs;
}
```

**工作流程**：
1. 监控系统定期检查所有工具统计
2. 发现超时频繁 → 生成建议
3. 运维人员验证后修改配置
4. 无需重启应用，只需修改 application.yml

---

### 监控接口 - `ToolExecutionController`

**新增 REST 接口**：

```
GET /api/tools/health
  返回：是否健康、告警数量、详细统计

GET /api/tools/dashboard
  返回：所有工具的实时指标（可用于仪表板）

GET /api/tools/recommendations
  返回：建议的配置调整

POST /api/tools/{toolName}/reset-stats
  重置特定工具的统计（用于测试）
```

**示例响应**：

```json
GET /api/tools/health
{
  "healthy": false,
  "alertCount": 2,
  "statistics": {
    "searchPolicy": {
      "totalExecutions": 150,
      "successCount": 140,
      "failureCount": 5,
      "timeoutCount": 5,
      "successRate": 93.33%,
      "avgLatencyMs": 2500
    },
    "sendEmail": {
      "totalExecutions": 50,
      "successCount": 50,
      "failureCount": 0,
      "timeoutCount": 0,
      "successRate": 100%,
      "avgLatencyMs": 3000
    }
  }
}
```

```json
GET /api/tools/recommendations
{
  "searchPolicy": {
    "currentTimeout": 15000,
    "recommendedTimeout": 18000,
    "reason": "High max latency: 14500ms",
    "maxLatency": 14500,
    "avgLatency": 2500
  }
}
```

---

## 新增类和接口

### 1. `RetryableErrorType.java`
错误的分类，决定是否可重试和是否需要指数退避

### 2. `ToolRetryStrategy.java`
为每个工具定义重试策略（重试次数、超时时间、副作用标记）

### 3. `ToolExecutionStats.java`
记录每个工具的执行统计信息

### 4. `ToolExecutionMonitor.java`
监控所有工具的健康状态、生成告警和建议

### 5. `ToolExecutionController.java`
REST API 端点，暴露监控和建议数据

---

## 配置示例

### application.yml

```yaml
agent:
  runtime:
    maxSteps: 3
    # V1：全局超时时间（可被V2的工具级策略覆盖）
    stepTimeoutMs: 15000
    retryTimes: 1
    includeTraceInResponse: true

# V2/V3：监控告警配置
monitoring:
  enabled: true
  sla:
    minSuccessRate: 95.0  # 95% SLA
    alertThresholdExecutions: 10  # 至少执行10次才告警
  
  # 工具特定配置（可选，会覆盖globally default）
  tools:
    searchPolicy:
      timeoutMs: 15000  # 查询可以更宽松
      maxRetries: 2
      allowDuplicate: true
    sendEmail:
      timeoutMs: 10000  # 邮件更严格的超时
      maxRetries: 0  # 不重试
      allowDuplicate: false
```

---

## 面试要点汇总

### V2 的价值：
1. **根据工具类型调整** - 查询 vs 操作需要不同策略
2. **根据错误类型调整** - 超时 vs 认证失败的处理不同
3. **指数退避** - 对限流错误有效，对超时无效
4. **方法缓存** - 避免重复的反射查找

### V3 的价值：
1. **自动监控** - 实时发现问题工具
2. **自动告警** - 不用人工观察日志
3. **配置建议** - 数据驱动的优化决策
4. **API 暴露** - 集成到监控仪表板

### 生产使用建议：
1. ✅ 先独立监控一段时间（不自动调整）
2. ✅ 收集足够多的数据（至少 1 周）
3. ✅ 对每个建议进行人工审查
4. ✅ 逐步启用自动调整（先从非关键工具开始）
5. ✅ 保持回滚能力（改坏了能快速恢复）

---

## 测试验证

### 单元测试建议：
```java
@Test
void testRetryableErrorType() {
    Exception timeoutException = new TimeoutException("timeout");
    assertEquals(RetryableErrorType.RETRYABLE_IMMEDIATE, 
        RetryableErrorType.classify(timeoutException));
}

@Test
void testToolRetryStrategy() {
    ToolRetryStrategy strategy = ToolRetryStrategy.forTool("sendEmail");
    assertEquals(0, strategy.getMaxRetryTimes());  // 不重试
    assertFalse(strategy.isAllowDuplicateExecution());
}

@Test
void testToolExecutionStats() {
    ToolExecutionStats stats = new ToolExecutionStats("test");
    stats.recordSuccess(1000);
    stats.recordTimeout(5000);
    assertEquals(50.0, stats.getSuccessRate());
}
```

### 集成测试建议：
```java
@Test
void testEndToEndWithMonitoring() throws Exception {
    // 执行多个请求
    for (int i = 0; i < 20; i++) {
        toolExecutionWrapper.execute("searchPolicy", "{}");
    }
    
    // 检查监控数据
    Map<String, Object> health = toolExecutionMonitor.checkHealth();
    assertTrue((Boolean) health.get("healthy"));
}
```
